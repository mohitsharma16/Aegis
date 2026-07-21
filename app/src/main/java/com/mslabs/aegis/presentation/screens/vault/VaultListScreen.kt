package com.mslabs.aegis.presentation.screens.vault

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.model.VaultItemType
import com.mslabs.aegis.presentation.components.AegisTextField
import com.mslabs.aegis.presentation.screens.detail.DetailViewModel
import com.mslabs.aegis.presentation.screens.detail.ItemDetailScreen
import kotlinx.coroutines.launch

/**
 * The vault's home surface.
 *
 * On a phone this is a list that pushes a full-screen detail; unfolded or on a tablet
 * the same scaffold shows list and detail side by side (US6). Nothing here hardcodes a
 * breakpoint - the navigator derives it from the current window (dg.md §2).
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VaultListScreen(
    vaultViewModel: VaultViewModel,
    detailViewModel: DetailViewModel,
    onOpenSecurityAudit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by vaultViewModel.uiState.collectAsStateWithLifecycle()
    val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    val selectedId = navigator.currentDestination
        ?.takeIf { it.pane == ListDetailPaneScaffoldRole.Detail }
        ?.contentKey

    // The pane is the source of truth for what is open; the ViewModel follows it.
    LaunchedEffect(selectedId) { detailViewModel.load(selectedId) }

    // A brand-new item gets a real id once saved - re-point the pane at it so a
    // subsequent fold or back gesture does not land on the dead "new" sentinel.
    LaunchedEffect(detailState.isSaved) {
        val savedId = detailState.item?.id
        if (detailState.isSaved && savedId != null) {
            if (selectedId == DetailViewModel.NEW_ITEM_ID) {
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, savedId)
            }
            detailViewModel.onSavedHandled()
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        modifier = modifier,
        listPane = {
            AnimatedPane {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Aegis") },
                            actions = {
                                TextButton(onClick = onOpenSecurityAudit) { Text("Security") }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    navigator.navigateTo(
                                        ListDetailPaneScaffoldRole.Detail,
                                        DetailViewModel.NEW_ITEM_ID,
                                    )
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.semantics {
                                    contentDescription = "Add vault item"
                                },
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                ) { innerPadding ->
                    VaultListPane(
                        uiState = uiState,
                        selectedId = selectedId,
                        onQueryChange = vaultViewModel::onQueryChange,
                        onFilterChange = vaultViewModel::onFilterChange,
                        onItemClick = { id ->
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }
        },
        detailPane = {
            AnimatedPane {
                ItemDetailScreen(
                    state = detailState,
                    onEdit = detailViewModel::onEdit,
                    onCancelEdit = detailViewModel::onCancelEdit,
                    onDraftChange = detailViewModel::onDraftChange,
                    onToggleReveal = detailViewModel::onToggleReveal,
                    onObscureSecret = detailViewModel::onObscureSecret,
                    onSave = detailViewModel::onSave,
                    onDelete = {
                        detailViewModel.onDelete()
                        scope.launch { navigator.navigateBack() }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultListPane(
    uiState: VaultUiState,
    selectedId: String?,
    onQueryChange: (String) -> Unit,
    onFilterChange: (VaultItemType?) -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AegisTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            label = "Search vault",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VaultItemType.entries.forEach { type ->
                FilterChip(
                    selected = uiState.filter == type,
                    onClick = { onFilterChange(type) },
                    label = { Text(type.displayName) },
                )
            }
        }

        when {
            uiState.isEmptyVault -> EmptyMessage("Your vault is empty.\nTap + to add your first item.")
            uiState.hasNoMatches -> EmptyMessage("No items match that search.")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = uiState.items, key = { it.id }) { item ->
                    VaultRow(
                        item = item,
                        isSelected = item.id == selectedId,
                        onClick = { onItemClick(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultRow(
    item: DecryptedVaultItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Subtitle only ever shows identifiers, never the secret.
            val subtitle = item.username ?: item.websiteUrl ?: item.type.displayName
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private val VaultItemType.displayName: String
    get() = when (this) {
        VaultItemType.LOGIN -> "Logins"
        VaultItemType.WIFI -> "Wi-Fi"
        VaultItemType.SECURE_NOTE -> "Notes"
        VaultItemType.PASSKEY -> "Passkeys"
    }
