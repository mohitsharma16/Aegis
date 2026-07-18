package com.mslabs.aegis.presentation.screens.vault

import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow

class VaultViewModel(
    private val repository: VaultRepository,
) {
    fun loadItems(): Flow<List<DecryptedVaultItem>> = repository.getVaultItems()
}
