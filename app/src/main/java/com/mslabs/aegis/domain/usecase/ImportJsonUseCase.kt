package com.mslabs.aegis.domain.usecase

import com.mslabs.aegis.domain.backup.VaultBackupCodec
import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.security.BackupCryptoManager
import javax.inject.Inject

/**
 * The restore half of BYOC backups (US2): decrypts an exported payload with the
 * password it was written under and returns the items it contained.
 *
 * Throws BackupDecryptionException for a wrong password or damaged file, and
 * BackupFormatException when the plaintext is not a backup this build understands.
 */
class ImportJsonUseCase @Inject constructor(
    private val backupCryptoManager: BackupCryptoManager,
) {

    operator fun invoke(
        payload: String,
        password: String,
    ): List<DecryptedVaultItem> {
        require(password.isNotBlank()) { "Backup password cannot be blank." }

        return VaultBackupCodec.decode(backupCryptoManager.decrypt(payload, password))
    }
}
