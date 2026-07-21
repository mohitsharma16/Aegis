package com.mslabs.aegis.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import dagger.Provides

@Qualifier
@Retention(BINARY)
annotation class IoDispatcher

/**
 * Global singletons that belong to no single layer.
 *
 * Note there is no Context binding here: Hilt already provides Context via
 * @ApplicationContext, and re-providing it would be an ambiguous duplicate.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
