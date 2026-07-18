package com.mslabs.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mslabs.aegis.data.local.entity.CredentialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialDao {

    @Query("SELECT * FROM credentials WHERE deletedAt IS NULL ORDER BY title ASC")
    fun getActiveCredentialsFlow(): Flow<List<CredentialEntity>>

    @Query("SELECT * FROM credentials WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashedCredentialsFlow(): Flow<List<CredentialEntity>>

    @Query("SELECT * FROM credentials WHERE id = :id LIMIT 1")
    suspend fun getCredentialById(id: String): CredentialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: CredentialEntity)

    @Update
    suspend fun updateCredential(credential: CredentialEntity)

    @Query("UPDATE credentials SET deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteCredential(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM credentials WHERE id = :id")
    suspend fun hardDeleteCredential(id: String)

    @Query("DELETE FROM credentials WHERE deletedAt IS NOT NULL AND deletedAt < :thirtyDaysAgo")
    suspend fun purgeOldTrash(thirtyDaysAgo: Long)
}