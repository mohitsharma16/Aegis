package com.mslabs.aegis.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mslabs.aegis.presentation.screens.detail.DetailViewModel
import com.mslabs.aegis.presentation.screens.detail.ItemDetailScreen
import com.mslabs.aegis.presentation.screens.vault.VaultListScreen
import com.mslabs.aegis.presentation.screens.vault.VaultViewModel

object AppRoutes {
    const val VAULT_LIST = "vault_list"
    const val ITEM_DETAIL = "item_detail/{itemId}"
    const val SECURITY_AUDIT = "security_audit"
    const val PREMIUM_UNLOCK = "premium_unlock"

    const val ITEM_ID_ARG = "itemId"

    fun itemDetail(itemId: String): String = "item_detail/$itemId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.VAULT_LIST,
    ) {
        vaultListDestination(navController)
        itemDetailDestination(navController)
        securityAuditDestination(navController)
        premiumUnlockDestination(navController)
    }
}

private fun NavGraphBuilder.vaultListDestination(
    navController: NavController,
) {
    composable(AppRoutes.VAULT_LIST) {
        VaultListScreen(
            vaultViewModel = hiltViewModel<VaultViewModel>(),
            detailViewModel = hiltViewModel<DetailViewModel>(),
            onOpenSecurityAudit = { navController.navigate(AppRoutes.SECURITY_AUDIT) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Standalone detail route.
 *
 * The vault list already hosts a detail pane, so this exists for entry points that
 * arrive with an id and no list behind them - Autofill and the Credential Manager in
 * Phase 4, plus deep links.
 */
private fun NavGraphBuilder.itemDetailDestination(
    navController: NavController,
) {
    composable(
        route = AppRoutes.ITEM_DETAIL,
        arguments = listOf(navArgument(AppRoutes.ITEM_ID_ARG) { type = NavType.StringType }),
    ) { backStackEntry ->
        val viewModel = hiltViewModel<DetailViewModel>()
        val itemId = backStackEntry.arguments?.getString(AppRoutes.ITEM_ID_ARG).orEmpty()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(itemId) { viewModel.load(itemId) }

        ItemDetailScreen(
            state = state,
            onEdit = viewModel::onEdit,
            onCancelEdit = viewModel::onCancelEdit,
            onDraftChange = viewModel::onDraftChange,
            onToggleReveal = viewModel::onToggleReveal,
            onObscureSecret = viewModel::onObscureSecret,
            onSave = viewModel::onSave,
            onDelete = {
                viewModel.onDelete()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun NavGraphBuilder.securityAuditDestination(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
) {
    composable(AppRoutes.SECURITY_AUDIT) {
        PlaceholderScreen("Security audit arrives in Phase 5.")
    }
}

private fun NavGraphBuilder.premiumUnlockDestination(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
) {
    composable(AppRoutes.PREMIUM_UNLOCK) {
        PlaceholderScreen("Premium unlock arrives in Phase 6.")
    }
}

@Composable
private fun PlaceholderScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
