package com.mslabs.aegis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mslabs.aegis.data.local.dao.AuditDao
import com.mslabs.aegis.data.local.dao.CredentialDao
import com.mslabs.aegis.data.local.entity.AuditResultEntity
import com.mslabs.aegis.data.local.entity.CredentialEntity

@Database(
    entities = [CredentialEntity::class, AuditResultEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun credentialDao(): CredentialDao

    abstract fun auditDao(): AuditDao

    companion object {
        const val DATABASE_NAME = "aegis_offline_vault.db"
    }
}
