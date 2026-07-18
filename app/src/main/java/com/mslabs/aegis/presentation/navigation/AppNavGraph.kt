package com.mslabs.aegis.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mslabs.aegis.presentation.screens.detail.DetailViewModel
import com.mslabs.aegis.presentation.screens.premium.PremiumScreen
import com.mslabs.aegis.presentation.screens.security.SecurityViewModel
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
        val viewModel = hiltViewModel<VaultViewModel>()
        VaultListRoute(
            navController = navController,
            viewModel = viewModel,
        )
    }
}

private fun NavGraphBuilder.itemDetailDestination(
    navController: NavController,
) {
    composable(
        route = AppRoutes.ITEM_DETAIL,
        arguments = listOf(navArgument(AppRoutes.ITEM_ID_ARG) { type = NavType.StringType }),
    ) { backStackEntry ->
        val viewModel = hiltViewModel<DetailViewModel>()
        ItemDetailRoute(
            navController = navController,
            viewModel = viewModel,
            itemId = backStackEntry.arguments?.getString(AppRoutes.ITEM_ID_ARG).orEmpty(),
        )
    }
}

private fun NavGraphBuilder.securityAuditDestination(
    navController: NavController,
) {
    composable(AppRoutes.SECURITY_AUDIT) {
        val viewModel = hiltViewModel<SecurityViewModel>()
        SecurityAuditRoute(
            navController = navController,
            viewModel = viewModel,
        )
    }
}

private fun NavGraphBuilder.premiumUnlockDestination(
    navController: NavController,
) {
    composable(AppRoutes.PREMIUM_UNLOCK) {
        PremiumUnlockRoute(navController = navController)
    }
}

@Composable
private fun VaultListRoute(
    navController: NavController,
    viewModel: VaultViewModel,
) {
    Text(text = "Vault")
}

@Composable
private fun ItemDetailRoute(
    navController: NavController,
    viewModel: DetailViewModel,
    itemId: String,
) {
    Text(text = "Item detail")
}

@Composable
private fun SecurityAuditRoute(
    navController: NavController,
    viewModel: SecurityViewModel,
) {
    Text(text = "Security audit")
}

@Composable
private fun PremiumUnlockRoute(
    navController: NavController,
) {
    val screen = PremiumScreen(isPremium = false)
    Text(text = screen.priceLabel)
}
