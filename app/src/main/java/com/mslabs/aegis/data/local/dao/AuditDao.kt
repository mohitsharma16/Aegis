package com.mslabs.aegis.data.local.dao

import com.mslabs.aegis.data.local.entity.AuditResultEntity

interface AuditDao {
    fun getAll(): List<AuditResultEntity>

    fun upsert(result: AuditResultEntity)
}
