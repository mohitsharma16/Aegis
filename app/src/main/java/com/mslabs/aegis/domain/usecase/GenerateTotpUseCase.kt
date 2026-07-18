package com.mslabs.aegis.domain.usecase

import com.mslabs.aegis.security.TotpGenerator
import javax.inject.Inject

data class TotpCode(
    val code: String,
    val remainingSeconds: Int,
)

class GenerateTotpUseCase @Inject constructor(
    private val totpGenerator: TotpGenerator,
) {
    operator fun invoke(
        secret: String,
        timestampMillis: Long = System.currentTimeMillis(),
    ): TotpCode {
        val elapsedSeconds = timestampMillis / MILLIS_PER_SECOND
        val remainingSeconds = (PERIOD_SECONDS - (elapsedSeconds % PERIOD_SECONDS)).toInt()

        return TotpCode(
            code = totpGenerator.generate(secret, timestampMillis),
            remainingSeconds = remainingSeconds,
        )
    }

    private companion object {
        private const val PERIOD_SECONDS = 30L
        private const val MILLIS_PER_SECOND = 1_000L
    }
}
