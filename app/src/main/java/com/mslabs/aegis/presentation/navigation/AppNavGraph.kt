package com.mslabs.aegis.presentation.navigation

enum class AppRoute {
    VAULT,
    DETAIL,
    SECURITY,
    PREMIUM,
}

class AppNavGraph {
    fun startDestination(): AppRoute = AppRoute.VAULT
}
