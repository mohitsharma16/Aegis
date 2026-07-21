package com.mslabs.aegis.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** testing_strategy.md §1: encrypt with a password, decrypt with the same one, fail with a wrong one. */
class BackupCryptoManagerTest {

    private val crypto = BackupCryptoManager()
    private val plaintext = """{"version":1,"items":[{"secret":"hunter2"}]}"""
    private val password = "correct horse battery staple"

    @Test
    fun `round trips through the same password`() {
        val payload = crypto.encrypt(plaintext, password)

        assertEquals(plaintext, crypto.decrypt(payload, password))
    }

    @Test
    fun `wrong password is rejected`() {
        val payload = crypto.encrypt(plaintext, password)

        assertThrows(BackupDecryptionException::class.java) {
            crypto.decrypt(payload, "correct horse battery stapl")
        }
    }

    @Test
    fun `ciphertext never contains the plaintext`() {
        val payload = crypto.encrypt(plaintext, password)

        assertFalse(payload.contains("hunter2"))
    }

    @Test
    fun `same input twice produces different ciphertext`() {
        // A fresh salt and IV each time - identical output would leak that two backups
        // hold the same vault, and reusing a GCM nonce would be a hard break.
        val first = crypto.encrypt(plaintext, password)
        val second = crypto.encrypt(plaintext, password)

        assertNotEquals(first, second)
        assertEquals(plaintext, crypto.decrypt(first, password))
        assertEquals(plaintext, crypto.decrypt(second, password))
    }

    @Test
    fun `tampered ciphertext is rejected`() {
        val payload = crypto.encrypt(plaintext, password)
        val bytes = java.util.Base64.getDecoder().decode(payload)
        bytes[bytes.size - 1] = (bytes[bytes.size - 1].toInt() xor 0x01).toByte()
        val tampered = java.util.Base64.getEncoder().encodeToString(bytes)

        assertThrows(BackupDecryptionException::class.java) { crypto.decrypt(tampered, password) }
    }

    @Test
    fun `unsupported format version is reported clearly`() {
        val bytes = java.util.Base64.getDecoder().decode(crypto.encrypt(plaintext, password))
        bytes[0] = 99
        val future = java.util.Base64.getEncoder().encodeToString(bytes)

        val error = assertThrows(BackupDecryptionException::class.java) {
            crypto.decrypt(future, password)
        }
        assertTrue(error.message!!.contains("version"))
    }

    @Test
    fun `garbage input fails cleanly rather than crashing`() {
        assertThrows(BackupDecryptionException::class.java) { crypto.decrypt("not base64 !!", password) }
        assertThrows(BackupDecryptionException::class.java) { crypto.decrypt("QQ==", password) }
    }

    @Test
    fun `blank passwords are refused on both sides`() {
        assertThrows(IllegalArgumentException::class.java) { crypto.encrypt(plaintext, "  ") }
        assertThrows(IllegalArgumentException::class.java) { crypto.decrypt("QQ==", "") }
    }
}
