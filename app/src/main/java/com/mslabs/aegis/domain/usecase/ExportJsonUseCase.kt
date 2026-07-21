package com.mslabs.aegis.domain.usecase

import com.mslabs.aegis.domain.backup.VaultBackupCodec
import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.security.BackupCryptoManager
import javax.inject.Inject

/**
 * Serialises the vault and encrypts it under a user-supplied backup password.
 *
 * Stays pure - it returns the ciphertext rather than touching a Uri - so the whole
 * BYOC pipeline is unit-testable. BackupFileDataSource performs the SAF write.
 */
class ExportJsonUseCase @Inject constructor(
    private val backupCryptoManager: BackupCryptoManager,
) {

    operator fun invoke(
        items: List<DecryptedVaultItem>,
        password: String,
    ): String {
        require(password.isNotBlank()) { "Backup password cannot be blank." }

        return backupCryptoManager.encrypt(VaultBackupCodec.encode(items), password)
    }
}
