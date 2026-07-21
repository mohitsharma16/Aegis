package com.mslabs.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * One audit verdict per credential, written by SecurityAuditWorker.
 *
 * Only the derived verdict is persisted - never the password, and never the hash
 * used to detect duplicates (sdd.md §5: audit hashes stay in volatile memory).
 */
@Entity(
    tableName = "audit_results",
    foreignKeys = [
        ForeignKey(
            entity = CredentialEntity::class,
            parentColumns = ["id"],
            childColumns = ["credentialId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class AuditResultEntity(
    @PrimaryKey val credentialId: String,
    /** zxcvbn score, 0 (worst) to 4 (best). */
    val entropyScore: Int,
    val isDuplicate: Boolean,
    val auditedAtEpochMillis: Long,
)
