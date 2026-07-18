package com.mslabs.aegis.di

import com.mslabs.aegis.data.local.AppDatabase
import com.mslabs.aegis.data.local.dao.AuditDao
import com.mslabs.aegis.data.local.dao.CredentialDao

class DatabaseModule(
    val database: AppDatabase,
    val credentialDao: CredentialDao,
    val auditDao: AuditDao,
)
