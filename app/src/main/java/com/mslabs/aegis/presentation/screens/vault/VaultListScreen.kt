package com.mslabs.aegis.presentation.screens.vault

import com.mslabs.aegis.domain.model.DecryptedVaultItem

data class VaultListScreen(
    val items: List<DecryptedVaultItem>,
    val searchQuery: String = "",
)
