package com.mslabs.aegis.domain.usecase

import com.mslabs.aegis.domain.model.DecryptedVaultItem
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class ExportJsonUseCase @Inject constructor() {

    operator fun invoke(
        items: List<DecryptedVaultItem>,
        password: String,
    ): String {
        require(password.isNotBlank()) { "Backup password cannot be blank." }

        val salt = secureRandomBytes(SALT_BYTES)
        val iv = secureRandomBytes(IV_BYTES)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        val ciphertext = cipher.doFinal(items.toJson().encodeToByteArray())

        return Base64.getEncoder().encodeToString(salt + iv + ciphertext)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_BITS)
        val keyBytes = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, AES_ALGORITHM)
    }

    private fun secureRandomBytes(size: Int): ByteArray {
        return ByteArray(size).also { secureRandom.nextBytes(it) }
    }

    private fun List<DecryptedVaultItem>.toJson(): String {
        return joinToString(prefix = "[", postfix = "]") { it.toJson() }
    }

    private fun DecryptedVaultItem.toJson(): String {
        return buildString {
            append("{")
            appendJsonField("id", id)
            append(",")
            appendJsonField("type", type.name)
            append(",")
            appendJsonField("title", title)
            append(",")
            appendJsonField("username", username)
            append(",")
            appendJsonField("secret", secret)
            append(",")
            appendJsonField("notes", notes)
            append(",")
            appendJsonField("totpSecret", totpSecret)
            append(",")
            appendJsonField("websiteUrl", websiteUrl)
            append(",")
            append("\"updatedAtEpochMillis\":")
            append(updatedAtEpochMillis)
            append("}")
        }
    }

    private fun StringBuilder.appendJsonField(name: String, value: String?) {
        append("\"")
        append(name)
        append("\":")
        if (value == null) {
            append("null")
        } else {
            append("\"")
            append(value.escapeJson())
            append("\"")
        }
    }

    private fun String.escapeJson(): String {
        return buildString {
            this@escapeJson.forEach { char ->
                when (char) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (char.code < JSON_CONTROL_CHAR_LIMIT) {
                            append("\\u")
                            append(char.code.toString(16).padStart(4, '0'))
                        } else {
                            append(char)
                        }
                    }
                }
            }
        }
    }

    private companion object {
        private const val AES_ALGORITHM = "AES"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS = 120_000
        private const val AES_KEY_BITS = 256
        private const val GCM_TAG_BITS = 128
        private const val SALT_BYTES = 16
        private const val IV_BYTES = 12
        private const val JSON_CONTROL_CHAR_LIMIT = 0x20
        private val secureRandom = SecureRandom()
    }
}
