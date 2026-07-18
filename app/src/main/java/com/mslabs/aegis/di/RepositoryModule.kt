package com.mslabs.aegis.di

import com.mslabs.aegis.data.repository.VaultRepositoryImpl
import com.mslabs.aegis.domain.repository.VaultRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVaultRepository(
        repository: VaultRepositoryImpl,
    ): VaultRepository
}
