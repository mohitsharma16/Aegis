package com.mslabs.aegis.workers

import com.mslabs.aegis.security.PasswordAuditor

class SecurityAuditWorker(
    private val passwordAuditor: PasswordAuditor,
) {
    fun audit(passwords: List<String>): List<Int> {
        TODO("Run the background password audit.")
    }
}
