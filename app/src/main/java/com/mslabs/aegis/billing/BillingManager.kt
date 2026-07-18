package com.mslabs.aegis.billing

class BillingManager(
    private val premiumPreferences: PremiumPreferences,
) {
    fun isPremium(): Boolean = premiumPreferences.isPremium()

    fun markPremiumPurchased() {
        premiumPreferences.setPremium(true)
    }
}
