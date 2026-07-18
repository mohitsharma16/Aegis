package com.mslabs.aegis.di

import com.mslabs.aegis.billing.BillingManager
import com.mslabs.aegis.security.KeystoreManager

class SecurityModule(
    val keystoreManager: KeystoreManager,
    val billingManager: BillingManager,
)
