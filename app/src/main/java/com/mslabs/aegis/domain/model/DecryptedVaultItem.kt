package com.mslabs.aegis.domain.model

data class DecryptedVaultItem(
    val id: String,
    val type: VaultItemType,
    val title: String,
    val username: String?,
    val secret: String?,
    val notes: String?,
    val updatedAtEpochMillis: Long,
)
