package com.mslabs.aegis.domain.repository

import com.mslabs.aegis.domain.model.DecryptedVaultItem

interface VaultRepository {
    fun getVaultItems(): List<DecryptedVaultItem>

    fun getVaultItem(id: String): DecryptedVaultItem?

    fun saveVaultItem(item: DecryptedVaultItem)

    fun deleteVaultItem(id: String)
}
