package com.mslabs.aegis.presentation.screens.vault

import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.repository.VaultRepository

class VaultViewModel(
    private val repository: VaultRepository,
) {
    fun loadItems(): List<DecryptedVaultItem> = repository.getVaultItems()
}
