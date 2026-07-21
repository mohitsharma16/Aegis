package com.mslabs.aegis.di

import android.content.Context
import com.mslabs.aegis.billing.BillingManager
import com.mslabs.aegis.billing.PremiumPreferences
import com.nulabinc.zxcvbn.Zxcvbn
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bindings for types the security and monetization layers own but cannot construct
 * themselves. KeystoreManager, TotpGenerator and PasswordAuditor all carry @Inject
 * constructors, so Hilt builds those without help.
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    /** Expensive to construct - it parses its frequency dictionaries eagerly. */
    @Provides
    @Singleton
    fun provideZxcvbn(): Zxcvbn = Zxcvbn()

    @Provides
    @Singleton
    fun providePremiumPreferences(
        @ApplicationContext context: Context,
    ): PremiumPreferences = PremiumPreferences(context)

    @Provides
    @Singleton
    fun provideBillingManager(
        premiumPreferences: PremiumPreferences,
    ): BillingManager = BillingManager(premiumPreferences)
}
