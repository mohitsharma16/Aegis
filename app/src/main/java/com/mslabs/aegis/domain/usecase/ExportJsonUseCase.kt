package com.mslabs.aegis.domain.usecase

import com.mslabs.aegis.domain.model.DecryptedVaultItem

class ExportJsonUseCase {
    operator fun invoke(items: List<DecryptedVaultItem>): String {
        TODO("Serialize and encrypt the export payload.")
    }
}
