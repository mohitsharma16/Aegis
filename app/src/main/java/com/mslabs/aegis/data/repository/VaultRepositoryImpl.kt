package com.mslabs.aegis.data.repository

import com.mslabs.aegis.data.local.dao.CredentialDao
import com.mslabs.aegis.data.local.entity.CredentialEntity
import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.repository.VaultRepository
import com.mslabs.aegis.security.KeystoreManager
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VaultRepositoryImpl @Inject constructor(
    private val credentialDao: CredentialDao,
    private val keystoreManager: KeystoreManager,
) : VaultRepository {

    override fun getVaultItems(): Flow<List<DecryptedVaultItem>> {
        return credentialDao.getActiveCredentialsFlow()
            .map { credentials -> credentials.map { it.toDecryptedVaultItem() } }
    }

    override suspend fun getVaultItem(id: String): DecryptedVaultItem? {
        return credentialDao.getCredentialById(id)?.toDecryptedVaultItem()
    }

    override suspend fun saveVaultItem(item: DecryptedVaultItem) {
        throw UnsupportedOperationException(
            "Saving vault items requires per-field IV storage or a combined encrypted payload."
        )
    }

    override suspend fun deleteVaultItem(id: String) {
        credentialDao.softDeleteCredential(id)
    }

    private fun CredentialEntity.toDecryptedVaultItem(): DecryptedVaultItem {
        return DecryptedVaultItem(
            id = id,
            type = itemType,
            title = title,
            username = encryptedUsername.decryptOrNull(iv),
            secret = encryptedPassword.decryptOrNull(iv),
            notes = encryptedNotes.decryptOrNull(iv),
            updatedAtEpochMillis = updatedAt,
        )
    }

    private fun ByteArray?.decryptOrNull(iv: ByteArray): String? {
        if (this == null || iv.isEmpty()) return null
        return keystoreManager.decrypt(this, iv).decodeToString()
    }
}
