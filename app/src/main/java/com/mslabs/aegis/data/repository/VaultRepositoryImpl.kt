package com.mslabs.aegis.data.repository

import com.mslabs.aegis.data.local.dao.CredentialDao
import com.mslabs.aegis.data.local.entity.CredentialEntity
import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.repository.VaultRepository
import com.mslabs.aegis.di.IoDispatcher
import com.mslabs.aegis.security.KeystoreManager
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VaultRepositoryImpl @Inject constructor(
    private val credentialDao: CredentialDao,
    private val keystoreManager: KeystoreManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VaultRepository {

    // Every mapping below is a Keystore round trip per field. flowOn/withContext keep
    // that work off the main thread - a vault of any size would jank the list otherwise.

    override fun getVaultItems(): Flow<List<DecryptedVaultItem>> {
        return credentialDao.getActiveCredentialsFlow()
            .map { credentials -> credentials.map { it.toDecryptedVaultItem() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getVaultItem(id: String): DecryptedVaultItem? {
        return withContext(ioDispatcher) {
            credentialDao.getCredentialById(id)?.toDecryptedVaultItem()
        }
    }

    override suspend fun saveVaultItem(item: DecryptedVaultItem) {
        withContext(ioDispatcher) {
            // insertCredential is REPLACE, which rewrites the whole row. Carry over the
            // columns the UI never sends so editing an item does not reset its creation
            // date, drop its passkey blob, or silently resurrect it from the trash.
            val existing = credentialDao.getCredentialById(item.id)

            credentialDao.insertCredential(
                CredentialEntity(
                    id = item.id,
                    title = item.title,
                    itemType = item.type,
                    encryptedUsername = item.username.encryptSafe(),
                    encryptedPassword = item.secret.encryptSafe(),
                    encryptedNotes = item.notes.encryptSafe(),
                    encryptedTotpSecret = item.totpSecret.encryptSafe(),
                    encryptedPasskeyData = existing?.encryptedPasskeyData,
                    encryptedWebsiteUrl = item.websiteUrl.encryptSafe(),
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    deletedAt = existing?.deletedAt,
                )
            )
        }
    }

    override suspend fun deleteVaultItem(id: String) {
        withContext(ioDispatcher) { credentialDao.softDeleteCredential(id) }
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
            websiteUrl = encryptedWebsiteUrl.decryptSafe(),
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
