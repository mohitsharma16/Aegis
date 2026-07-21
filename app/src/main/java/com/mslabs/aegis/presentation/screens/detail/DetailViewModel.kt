package com.mslabs.aegis.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.model.VaultItemType
import com.mslabs.aegis.domain.repository.VaultRepository
import com.mslabs.aegis.domain.usecase.GenerateTotpUseCase
import com.mslabs.aegis.domain.usecase.TotpCode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** The in-progress edits, kept separate from the saved item so Cancel is trivial. */
data class ItemDraft(
    val title: String = "",
    val username: String = "",
    val secret: String = "",
    val notes: String = "",
    val websiteUrl: String = "",
    val totpSecret: String = "",
    val type: VaultItemType = VaultItemType.LOGIN,
) {
    val canSave: Boolean get() = title.isNotBlank()
}

data class DetailUiState(
    val item: DecryptedVaultItem? = null,
    val draft: ItemDraft = ItemDraft(),
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val isNewItem: Boolean = false,
    val isSecretRevealed: Boolean = false,
    val totp: TotpCode? = null,
    val isSaved: Boolean = false,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: VaultRepository,
    private val generateTotp: GenerateTotpUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var loadedItemId: String? = null
    private var totpJob: kotlinx.coroutines.Job? = null

    /**
     * Called from the detail pane, which re-composes with a new id whenever the user
     * taps a different row. Re-loading the same id is a no-op so an unrelated
     * recomposition cannot silently discard in-progress edits.
     */
    fun load(itemId: String?) {
        if (itemId == loadedItemId) return
        loadedItemId = itemId
        totpJob?.cancel()

        if (itemId.isNullOrBlank()) {
            _uiState.value = DetailUiState()
            return
        }

        if (itemId == NEW_ITEM_ID) {
            _uiState.value = DetailUiState(isEditing = true, isNewItem = true)
            return
        }

        _uiState.value = DetailUiState(isLoading = true)
        viewModelScope.launch {
            val item = repository.getVaultItem(itemId)
            _uiState.value = DetailUiState(
                item = item,
                draft = item.toDraft(),
                isLoading = false,
            )
            startTotpTicker(item?.totpSecret)
        }
    }

    fun onEdit() = _uiState.update { it.copy(isEditing = true) }

    fun onCancelEdit() = _uiState.update {
        if (it.isNewItem) {
            DetailUiState()
        } else {
            it.copy(isEditing = false, draft = it.item.toDraft(), isSecretRevealed = false)
        }
    }

    fun onDraftChange(transform: (ItemDraft) -> ItemDraft) =
        _uiState.update { it.copy(draft = transform(it.draft)) }

    fun onToggleReveal() = _uiState.update { it.copy(isSecretRevealed = !it.isSecretRevealed) }

    /** Re-masks the secret; called when the window loses focus (sdd.md §4). */
    fun onObscureSecret() = _uiState.update { it.copy(isSecretRevealed = false) }

    fun onSave() {
        val state = _uiState.value
        if (!state.draft.canSave) return

        viewModelScope.launch {
            val merged = (state.item ?: DecryptedVaultItem(title = state.draft.title))
                .copy(
                    title = state.draft.title.trim(),
                    type = state.draft.type,
                    username = state.draft.username.trim().takeIf { it.isNotBlank() },
                    secret = state.draft.secret.takeIf { it.isNotBlank() },
                    notes = state.draft.notes.takeIf { it.isNotBlank() },
                    websiteUrl = state.draft.websiteUrl.trim().takeIf { it.isNotBlank() },
                    totpSecret = state.draft.totpSecret.trim().takeIf { it.isNotBlank() },
                )

            repository.saveVaultItem(merged)
            loadedItemId = merged.id
            _uiState.update {
                it.copy(
                    item = merged,
                    draft = merged.toDraft(),
                    isEditing = false,
                    isNewItem = false,
                    isSecretRevealed = false,
                    isSaved = true,
                )
            }
            startTotpTicker(merged.totpSecret)
        }
    }

    fun onDelete() {
        val id = _uiState.value.item?.id ?: return
        viewModelScope.launch {
            repository.deleteVaultItem(id)
            loadedItemId = null
            totpJob?.cancel()
            _uiState.value = DetailUiState()
        }
    }

    fun onSavedHandled() = _uiState.update { it.copy(isSaved = false) }

    private fun startTotpTicker(secret: String?) {
        totpJob?.cancel()
        if (secret.isNullOrBlank()) {
            _uiState.update { it.copy(totp = null) }
            return
        }

        totpJob = viewModelScope.launch {
            while (isActive) {
                val code = runCatching { generateTotp(secret) }.getOrNull()
                _uiState.update { it.copy(totp = code) }
                if (code == null) return@launch // Malformed Base32 seed; stop retrying.
                delay(1_000)
            }
        }
    }

    private fun DecryptedVaultItem?.toDraft(): ItemDraft {
        if (this == null) return ItemDraft()
        return ItemDraft(
            title = title,
            username = username.orEmpty(),
            secret = secret.orEmpty(),
            notes = notes.orEmpty(),
            websiteUrl = websiteUrl.orEmpty(),
            totpSecret = totpSecret.orEmpty(),
            type = type,
        )
    }

    companion object {
        /** Sentinel id meaning "compose a brand new item in the detail pane". */
        const val NEW_ITEM_ID = "new"
    }
}
