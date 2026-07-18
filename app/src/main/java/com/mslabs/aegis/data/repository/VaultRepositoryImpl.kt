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
        credentialDao.insertCredential(
            CredentialEntity(
                id = item.id,
                title = item.title,
                itemType = item.type,
                encryptedUsername = item.username.encryptSafe(),
                encryptedPassword = item.secret.encryptSafe(),
                encryptedNotes = item.notes.encryptSafe(),
                encryptedTotpSecret = item.totpSecret.encryptSafe(),
                encryptedPasskeyData = null,
                iv = ByteArray(0),
                updatedAt = System.currentTimeMillis(),
            )
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
            username = encryptedUsername.decryptSafe(),
            secret = encryptedPassword.decryptSafe(),
            notes = encryptedNotes.decryptSafe(),
            totpSecret = encryptedTotpSecret.decryptSafe(),
            updatedAtEpochMillis = updatedAt,
        )
    }

    private fun String?.encryptSafe(): ByteArray? {
        if (this.isNullOrEmpty()) return null

        val (ciphertext, iv) = keystoreManager.encrypt(encodeToByteArray())
        require(iv.size <= UByte.MAX_VALUE.toInt()) { "IV length must fit in one byte." }

        return byteArrayOf(iv.size.toByte()) + iv + ciphertext
    }

    private fun ByteArray?.decryptSafe(): String? {
        if (this == null || isEmpty()) return null

        val ivLength = this[0].toInt() and 0xFF
        if (ivLength == 0 || size <= 1 + ivLength) return null

        val iv = copyOfRange(1, 1 + ivLength)
        val ciphertext = copyOfRange(1 + ivLength, size)

        return keystoreManager.decrypt(ciphertext, iv).decodeToString()
    }
}
