package com.mslabs.aegis.domain.repository

import com.mslabs.aegis.domain.model.DecryptedVaultItem
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun getVaultItems(): Flow<List<DecryptedVaultItem>>

    suspend fun getVaultItem(id: String): DecryptedVaultItem?

    suspend fun saveVaultItem(item: DecryptedVaultItem)

    suspend fun deleteVaultItem(id: String)
}
