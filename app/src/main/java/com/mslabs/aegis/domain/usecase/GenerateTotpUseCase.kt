package com.mslabs.aegis.domain.usecase

import com.mslabs.aegis.security.TotpGenerator

class GenerateTotpUseCase(
    private val totpGenerator: TotpGenerator,
) {
    operator fun invoke(secret: String, timestampMillis: Long): String {
        return totpGenerator.generate(secret, timestampMillis)
    }
}
