package com.mslabs.aegis.domain.usecase

import com.mslabs.aegis.domain.model.DecryptedVaultItem

class ImportCsvUseCase {
    operator fun invoke(csvText: String): List<DecryptedVaultItem> {
        require(csvText.isNotBlank()) { "CSV content cannot be blank." }
        TODO("Parse supported password-manager CSV formats.")
    }
}
