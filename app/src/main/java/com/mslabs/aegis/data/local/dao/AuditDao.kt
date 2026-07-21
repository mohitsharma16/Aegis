package com.mslabs.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mslabs.aegis.data.local.entity.AuditResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {

    @Query("SELECT * FROM audit_results")
    fun getAuditResultsFlow(): Flow<List<AuditResultEntity>>

    @Query("SELECT * FROM audit_results WHERE credentialId = :credentialId LIMIT 1")
    suspend fun getAuditResult(credentialId: String): AuditResultEntity?

    @Upsert
    suspend fun upsertAuditResults(results: List<AuditResultEntity>)

    @Query("DELETE FROM audit_results")
    suspend fun clearAuditResults()
}
