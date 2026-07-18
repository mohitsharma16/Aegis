package com.mslabs.aegis.billing

import android.content.Context

class PremiumPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isPremium(): Boolean = preferences.getBoolean(KEY_IS_PREMIUM, false)

    fun setPremium(isPremium: Boolean) {
        preferences.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "premium_preferences"
        const val KEY_IS_PREMIUM = "is_premium"
    }
}
