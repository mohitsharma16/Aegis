package com.mslabs.aegis.domain.model

import java.util.UUID

data class DecryptedVaultItem(
    val id: String = UUID.randomUUID().toString(),
    val type: VaultItemType = VaultItemType.LOGIN,
    val title: String,
    val username: String? = null,
    val secret: String? = null,
    val notes: String? = null,
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),
    val totpSecret: String? = null,
    val websiteUrl: String? = null,
)
