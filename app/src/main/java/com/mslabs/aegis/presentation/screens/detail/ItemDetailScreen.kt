package com.mslabs.aegis.presentation.screens.detail

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.PersistableBundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.mslabs.aegis.domain.model.VaultItemType
import com.mslabs.aegis.presentation.components.AegisButton
import com.mslabs.aegis.presentation.components.AegisSecondaryButton
import com.mslabs.aegis.presentation.components.AegisTextField
import com.mslabs.aegis.presentation.components.TotpCodeRow

/**
 * Views or edits a single vault item. Hosted both by the adaptive detail pane and by
 * the standalone ITEM_DETAIL route, so it takes plain state and callbacks rather than
 * reaching for a ViewModel itself.
 */
@Composable
fun ItemDetailScreen(
    state: DetailUiState,
    onEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDraftChange: ((ItemDraft) -> ItemDraft) -> Unit,
    onToggleReveal: () -> Unit,
    onObscureSecret: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // sdd.md §4: the moment this window stops being the top focus - split-screen,
    // recents, another app - any revealed password goes back behind its mask.
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { onObscureSecret() }

    when {
        state.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        state.item == null && !state.isNewItem -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Select an item to see its details.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        else -> ItemDetailContent(
            state = state,
            onEdit = onEdit,
            onCancelEdit = onCancelEdit,
            onDraftChange = onDraftChange,
            onToggleReveal = onToggleReveal,
            onSave = onSave,
            onDelete = onDelete,
            modifier = modifier,
        )
    }
}

@Composable
private fun ItemDetailContent(
    state: DetailUiState,
    onEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDraftChange: ((ItemDraft) -> ItemDraft) -> Unit,
    onToggleReveal: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val editable = state.isEditing

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = when {
                state.isNewItem -> "New item"
                else -> state.draft.title.ifBlank { "Untitled" }
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (state.totp != null && !editable) {
            TotpCodeRow(
                code = state.totp.code,
                secondsRemaining = state.totp.remainingSeconds,
                modifier = Modifier.fillMaxWidth(),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }

        AegisTextField(
            value = state.draft.title,
            onValueChange = { next -> onDraftChange { it.copy(title = next) } },
            label = "Title",
            modifier = Modifier.fillMaxWidth(),
            readOnly = !editable,
        )

        if (state.draft.type != VaultItemType.SECURE_NOTE) {
            AegisTextField(
                value = state.draft.username,
                onValueChange = { next -> onDraftChange { it.copy(username = next) } },
                label = if (state.draft.type == VaultItemType.WIFI) "Network" else "Username",
                modifier = Modifier.fillMaxWidth(),
                readOnly = !editable,
            )

            AegisTextField(
                value = state.draft.secret,
                onValueChange = { next -> onDraftChange { it.copy(secret = next) } },
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                isSecure = true,
                isRevealed = state.isSecretRevealed,
                onToggleReveal = onToggleReveal,
                readOnly = !editable,
            )

            AegisTextField(
                value = state.draft.websiteUrl,
                onValueChange = { next -> onDraftChange { it.copy(websiteUrl = next) } },
                label = "Website",
                modifier = Modifier.fillMaxWidth(),
                readOnly = !editable,
            )
        }

        AegisTextField(
            value = state.draft.notes,
            onValueChange = { next -> onDraftChange { it.copy(notes = next) } },
            label = "Notes",
            modifier = Modifier.fillMaxWidth(),
            readOnly = !editable,
            singleLine = false,
            minLines = 3,
        )

        if (editable) {
            AegisTextField(
                value = state.draft.totpSecret,
                onValueChange = { next -> onDraftChange { it.copy(totpSecret = next) } },
                label = "TOTP secret (Base32)",
                modifier = Modifier.fillMaxWidth(),
                isSecure = true,
                isRevealed = state.isSecretRevealed,
                onToggleReveal = onToggleReveal,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (editable) {
                AegisButton(
                    text = "Save",
                    onClick = onSave,
                    enabled = state.draft.canSave,
                )
                AegisSecondaryButton(text = "Cancel", onClick = onCancelEdit)
            } else {
                AegisButton(text = "Edit", onClick = onEdit)
                if (state.draft.secret.isNotBlank()) {
                    AegisSecondaryButton(
                        text = "Copy password",
                        onClick = { context.copySensitive(state.draft.secret) },
                    )
                }
            }
        }

        if (!editable && state.item != null) {
            AegisSecondaryButton(
                text = "Move to Recently Deleted",
                onClick = onDelete,
                isDestructive = true,
            )
        }
    }
}

/**
 * Copies with the Android 13+ sensitive flag (sdd.md §5) so the system suppresses the
 * clipboard preview toast and keeps the value out of clipboard history.
 */
private fun Context.copySensitive(value: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    val clip = ClipData.newPlainText("", value).apply {
        description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
    clipboard.setPrimaryClip(clip)
}
