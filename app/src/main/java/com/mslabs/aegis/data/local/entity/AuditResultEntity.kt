package com.mslabs.aegis.data.local.entity

data class AuditResultEntity(
    val credentialId: String,
    val entropyScore: Int,
    val isDuplicate: Boolean,
    val auditedAtEpochMillis: Long,
)
