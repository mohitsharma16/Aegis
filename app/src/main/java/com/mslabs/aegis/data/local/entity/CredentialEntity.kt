package com.mslabs.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mslabs.aegis.domain.model.VaultItemType
import java.util.UUID

@Entity(tableName = "credentials")
data class CredentialEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val title: String,
    val itemType: VaultItemType,

    val encryptedUsername: ByteArray?,
    val encryptedPassword: ByteArray?,
    val encryptedNotes: ByteArray?,
    val encryptedTotpSecret: ByteArray?,
    val encryptedPasskeyData: ByteArray?,

    val iv: ByteArray,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CredentialEntity
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}