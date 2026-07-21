package com.mslabs.aegis.di

import android.content.Context
import androidx.room.Room
import com.mslabs.aegis.data.local.AppDatabase
import com.mslabs.aegis.data.local.dao.AuditDao
import com.mslabs.aegis.data.local.dao.CredentialDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).build()
    }

    @Provides
    fun provideCredentialDao(database: AppDatabase): CredentialDao {
        return database.credentialDao()
    }

    @Provides
    fun provideAuditDao(database: AppDatabase): AuditDao {
        return database.auditDao()
    }
}
