package com.mslabs.aegis.security

import java.nio.ByteBuffer
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.math.pow

class TotpGenerator @Inject constructor() {

    fun generate(
        secret: String,
        timestampMillis: Long = System.currentTimeMillis(),
        digits: Int = DEFAULT_DIGITS,
        periodSeconds: Long = DEFAULT_PERIOD_SECONDS,
    ): String {
        require(digits > 0) { "TOTP digits must be greater than zero." }
        require(periodSeconds > 0) { "TOTP period must be greater than zero." }

        val key = decodeBase32(secret)
        val counter = timestampMillis / MILLIS_PER_SECOND / periodSeconds
        val counterBytes = ByteBuffer.allocate(COUNTER_BYTE_SIZE).putLong(counter).array()
        val mac = Mac.getInstance(HMAC_SHA1).apply {
            init(SecretKeySpec(key, HMAC_SHA1))
        }

        val hash = mac.doFinal(counterBytes)
        val offset = hash.last().toInt() and DYNAMIC_TRUNCATION_MASK
        val binaryCode = ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)
        val otp = binaryCode % 10.0.pow(digits).toInt()

        return otp.toString().padStart(digits, '0')
    }

    fun decodeBase32(secret: String): ByteArray {
        val normalizedSecret = secret
            .uppercase(Locale.US)
            .filter { it != '=' && !it.isWhitespace() }

        require(normalizedSecret.isNotBlank()) { "TOTP secret cannot be blank." }

        var buffer = 0
        var bitsLeft = 0
        val decodedBytes = mutableListOf<Byte>()

        normalizedSecret.forEach { char ->
            val value = BASE32_ALPHABET.indexOf(char)
            require(value >= 0) { "Invalid Base32 character: $char" }

            buffer = (buffer shl BASE32_BITS_PER_CHAR) or value
            bitsLeft += BASE32_BITS_PER_CHAR

            while (bitsLeft >= Byte.SIZE_BITS) {
                decodedBytes += ((buffer shr (bitsLeft - Byte.SIZE_BITS)) and 0xFF).toByte()
                bitsLeft -= Byte.SIZE_BITS
            }
        }

        return decodedBytes.toByteArray()
    }

    private companion object {
        private const val HMAC_SHA1 = "HmacSHA1"
        private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        private const val BASE32_BITS_PER_CHAR = 5
        private const val COUNTER_BYTE_SIZE = 8
        private const val DYNAMIC_TRUNCATION_MASK = 0x0F
        private const val DEFAULT_DIGITS = 6
        private const val DEFAULT_PERIOD_SECONDS = 30L
        private const val MILLIS_PER_SECOND = 1_000L
    }
}
