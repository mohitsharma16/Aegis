package com.mslabs.aegis.security

import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/** Thrown when a backup cannot be decrypted: wrong password, or a corrupted file. */
class BackupDecryptionException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Crypto for "Bring Your Own Cloud" backups (tdd.md §3).
 *
 * The hardware Keystore key cannot leave this device, so a backup meant to be restored
 * on a different phone is encrypted with a key derived from a user-supplied password
 * via PBKDF2 instead. Output layout, then Base64-encoded:
 *
 *     [version: 1][salt: 16][iv: 12][ciphertext + GCM tag]
 *
 * The version byte is what lets a future build raise the iteration count or swap the
 * KDF while still reading backups written today.
 */
@Singleton
class BackupCryptoManager @Inject constructor() {

    fun encrypt(plaintext: String, password: String): String {
        require(password.isNotBlank()) { "Backup password cannot be blank." }

        val salt = randomBytes(SALT_BYTES)
        val iv = randomBytes(IV_BYTES)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        val ciphertext = cipher.doFinal(plaintext.encodeToByteArray())

        return Base64.getEncoder()
            .encodeToString(byteArrayOf(FORMAT_VERSION) + salt + iv + ciphertext)
    }

    fun decrypt(payload: String, password: String): String {
        require(password.isNotBlank()) { "Backup password cannot be blank." }

        val bytes = try {
            Base64.getDecoder().decode(payload.trim())
        } catch (e: IllegalArgumentException) {
            throw BackupDecryptionException("Backup file is not valid Base64.", e)
        }

        if (bytes.size <= HEADER_BYTES) {
            throw BackupDecryptionException("Backup file is truncated.")
        }
        if (bytes[0] != FORMAT_VERSION) {
            throw BackupDecryptionException(
                "Unsupported backup format version ${bytes[0].toInt()}; this build reads version $FORMAT_VERSION.",
            )
        }

        val salt = bytes.copyOfRange(1, 1 + SALT_BYTES)
        val iv = bytes.copyOfRange(1 + SALT_BYTES, HEADER_BYTES)
        val ciphertext = bytes.copyOfRange(HEADER_BYTES, bytes.size)

        return try {
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION).apply {
                init(
                    Cipher.DECRYPT_MODE,
                    deriveKey(password, salt),
                    GCMParameterSpec(GCM_TAG_BITS, iv),
                )
            }
            cipher.doFinal(ciphertext).decodeToString()
        } catch (e: GeneralSecurityException) {
            // GCM authentication cannot distinguish a wrong password from a tampered
            // file, and saying so precisely would leak which one it was.
            throw BackupDecryptionException("Incorrect password, or the backup file is damaged.", e)
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_BITS)
        val keyBytes = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, AES_ALGORITHM)
    }

    private fun randomBytes(size: Int): ByteArray = ByteArray(size).also { secureRandom.nextBytes(it) }

    private companion object {
        private const val FORMAT_VERSION: Byte = 1
        private const val AES_ALGORITHM = "AES"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS = 120_000
        private const val AES_KEY_BITS = 256
        private const val GCM_TAG_BITS = 128
        private const val SALT_BYTES = 16
        private const val IV_BYTES = 12
        private const val HEADER_BYTES = 1 + SALT_BYTES + IV_BYTES
        private val secureRandom = SecureRandom()
    }
}
