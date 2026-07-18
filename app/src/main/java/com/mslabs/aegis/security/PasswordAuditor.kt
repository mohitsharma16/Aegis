package com.mslabs.aegis.security

import com.nulabinc.zxcvbn.Zxcvbn
import javax.inject.Inject
import javax.inject.Singleton

data class PasswordAuditResult(
    val score: Int,
    val crackTimeEstimate: String,
    val crackTimeSeconds: Double,
)

@Singleton
class PasswordAuditor @Inject constructor(
    private val zxcvbn: Zxcvbn = Zxcvbn(),
) {

    fun audit(password: String): PasswordAuditResult {
        val strength = zxcvbn.measure(password)

        return PasswordAuditResult(
            score = strength.score,
            crackTimeEstimate = strength.crackTimesDisplay.offlineSlowHashing1e4perSecond,
            crackTimeSeconds = strength.crackTimeSeconds.offlineSlowHashing1e4perSecond,
        )
    }

    fun score(password: String): Int {
        return audit(password).score
    }
}
