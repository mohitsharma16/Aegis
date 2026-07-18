package com.mslabs.aegis.presentation.screens.security

import com.mslabs.aegis.security.PasswordAuditor

class SecurityViewModel(
    private val passwordAuditor: PasswordAuditor,
) {
    fun score(password: String): Int = passwordAuditor.score(password)
}
