package com.mslabs.aegis.presentation.screens.detail

import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.repository.VaultRepository

class DetailViewModel(
    private val repository: VaultRepository,
) {
    suspend fun save(item: DecryptedVaultItem) {
        repository.saveVaultItem(item)
    }
}
