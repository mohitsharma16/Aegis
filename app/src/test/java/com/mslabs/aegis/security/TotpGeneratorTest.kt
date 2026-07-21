package com.mslabs.aegis.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * RFC 6238 Appendix B reference vectors.
 *
 * The published table uses the ASCII seed "12345678901234567890", which is
 * "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ" in Base32, and prints 8-digit SHA-1 codes.
 */
class TotpGeneratorTest {

    private val generator = TotpGenerator()
    private val rfcSecret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"

    @Test
    fun `matches RFC 6238 eight digit vectors`() {
        val vectors = mapOf(
            59L to "94287082",
            1_111_111_109L to "07081804",
            1_111_111_111L to "14050471",
            1_234_567_890L to "89005924",
            2_000_000_000L to "69279037",
            20_000_000_000L to "65353130",
        )

        vectors.forEach { (epochSeconds, expected) ->
            assertEquals(
                "TOTP at T=$epochSeconds",
                expected,
                generator.generate(rfcSecret, epochSeconds * 1_000L, digits = 8),
            )
        }
    }

    @Test
    fun `six digit codes are the low order digits of the RFC vectors`() {
        assertEquals("287082", generator.generate(rfcSecret, 59_000L))
        assertEquals("081804", generator.generate(rfcSecret, 1_111_111_109_000L))
        assertEquals("050471", generator.generate(rfcSecret, 1_111_111_111_000L))
    }

    @Test
    fun `code is stable across the whole 30 second window and rolls at the boundary`() {
        val atStart = generator.generate(rfcSecret, 1_111_111_080_000L)
        val atEnd = generator.generate(rfcSecret, 1_111_111_109_999L)
        val afterRollover = generator.generate(rfcSecret, 1_111_111_110_000L)

        assertEquals(atStart, atEnd)
        assertEquals("081804", atEnd)
        assertEquals("050471", afterRollover)
    }

    @Test
    fun `codes are left padded to the requested length`() {
        // T=1111111109 produces 07081804 - the leading zero must survive.
        assertEquals(8, generator.generate(rfcSecret, 1_111_111_109_000L, digits = 8).length)
        assertEquals('0', generator.generate(rfcSecret, 1_111_111_109_000L, digits = 8).first())
    }

    @Test
    fun `base32 decoding follows RFC 4648 and tolerates padding and case`() {
        assertEquals("foobar", generator.decodeBase32("MZXW6YTBOI======").decodeToString())
        assertEquals("foobar", generator.decodeBase32("mzxw6ytboi").decodeToString())
        assertEquals("foobar", generator.decodeBase32("MZXW 6YTB OI").decodeToString())
    }

    @Test
    fun `rejects malformed secrets`() {
        assertThrows(IllegalArgumentException::class.java) { generator.decodeBase32("  ") }
        // '1' and '8' are not in the Base32 alphabet.
        assertThrows(IllegalArgumentException::class.java) { generator.decodeBase32("ABC18") }
    }

    @Test
    fun `rejects nonsensical parameters`() {
        assertThrows(IllegalArgumentException::class.java) {
            generator.generate(rfcSecret, 59_000L, digits = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            generator.generate(rfcSecret, 59_000L, periodSeconds = 0)
        }
    }
}
