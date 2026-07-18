package com.mslabs.aegis.data.repository

import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.repository.VaultRepository

class VaultRepositoryImpl : VaultRepository {
    override fun getVaultItems(): List<DecryptedVaultItem> {
        TODO("Connect Room entities to decrypted domain models.")
    }

    override fun getVaultItem(id: String): DecryptedVaultItem? {
        TODO("Load and decrypt a single vault item.")
    }

    override fun saveVaultItem(item: DecryptedVaultItem) {
        TODO("Encrypt and persist a vault item.")
    }

    override fun deleteVaultItem(id: String) {
        TODO("Delete or soft-delete a vault item.")
    }
}
