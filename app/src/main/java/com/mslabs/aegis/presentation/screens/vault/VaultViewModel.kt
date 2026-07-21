package com.mslabs.aegis.presentation.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.model.VaultItemType
import com.mslabs.aegis.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VaultUiState(
    val items: List<DecryptedVaultItem> = emptyList(),
    val query: String = "",
    val filter: VaultItemType? = null,
    val isLoading: Boolean = true,
) {
    val isEmptyVault: Boolean get() = !isLoading && items.isEmpty() && query.isBlank() && filter == null
    val hasNoMatches: Boolean get() = !isLoading && items.isEmpty() && (query.isNotBlank() || filter != null)
}

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: VaultRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow<VaultItemType?>(null)

    private val _selectedItemId = MutableStateFlow<String?>(null)
    val selectedItemId: StateFlow<String?> = _selectedItemId.asStateFlow()

    val uiState: StateFlow<VaultUiState> =
        combine(repository.getVaultItems(), query, filter) { items, query, filter ->
            VaultUiState(
                items = items.matching(query, filter),
                query = query,
                filter = filter,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            // Survive a fold/unfold or rotation without re-decrypting the whole vault,
            // but drop the subscription once the user has genuinely left.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VaultUiState(),
        )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: VaultItemType?) {
        filter.value = if (filter.value == value) null else value
    }

    fun onItemSelected(itemId: String?) {
        _selectedItemId.value = itemId
    }

    fun onDeleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteVaultItem(itemId)
            if (_selectedItemId.value == itemId) _selectedItemId.value = null
        }
    }

    /**
     * Searches only the fields a user would recognise. The secret and TOTP seed are
     * deliberately excluded - matching on them would let anyone with the unlocked
     * device confirm a password by guessing it in the search box.
     */
    private fun List<DecryptedVaultItem>.matching(
        query: String,
        typeFilter: VaultItemType?,
    ): List<DecryptedVaultItem> {
        val trimmed = query.trim()

        return filter { item -> typeFilter == null || item.type == typeFilter }
            .filter { item ->
                trimmed.isBlank() ||
                    item.title.contains(trimmed, ignoreCase = true) ||
                    item.username?.contains(trimmed, ignoreCase = true) == true ||
                    item.websiteUrl?.contains(trimmed, ignoreCase = true) == true
            }
    }
}
