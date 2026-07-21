package com.mslabs.aegis.domain.backup

import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.model.VaultItemType

/** Thrown when decrypted backup content is not a vault backup we can read. */
class BackupFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Encodes and decodes the plaintext side of a backup.
 *
 * Hand-rolled rather than kotlinx.serialization: the serialization compiler plugin is
 * version-locked to the Kotlin release, and pulling it in is a build-level change we
 * do not need for a payload this small. The parser below is strict - it rejects
 * trailing content rather than silently ignoring it.
 *
 * Envelope:
 * ```
 * {"version":1,"exportedAt":1700000000000,"items":[{...}]}
 * ```
 */
object VaultBackupCodec {

    const val FORMAT_VERSION = 1

    fun encode(
        items: List<DecryptedVaultItem>,
        exportedAtEpochMillis: Long = System.currentTimeMillis(),
    ): String = buildString {
        append("{\"version\":").append(FORMAT_VERSION)
        append(",\"exportedAt\":").append(exportedAtEpochMillis)
        append(",\"items\":[")
        items.forEachIndexed { index, item ->
            if (index > 0) append(',')
            appendItem(item)
        }
        append("]}")
    }

    fun decode(json: String): List<DecryptedVaultItem> {
        val root = try {
            JsonParser(json).parseDocument()
        } catch (e: BackupFormatException) {
            throw e
        } catch (e: Exception) {
            throw BackupFormatException("Backup content is not valid JSON.", e)
        }

        val obj = root as? Map<*, *>
            ?: throw BackupFormatException("Backup root must be a JSON object.")

        val version = (obj["version"] as? Number)?.toInt()
            ?: throw BackupFormatException("Backup is missing a version field.")
        if (version != FORMAT_VERSION) {
            throw BackupFormatException(
                "Unsupported backup content version $version; this build reads version $FORMAT_VERSION.",
            )
        }

        val items = obj["items"] as? List<*>
            ?: throw BackupFormatException("Backup is missing an items array.")

        return items.map { element ->
            val item = element as? Map<*, *>
                ?: throw BackupFormatException("Each backup item must be a JSON object.")
            item.toVaultItem()
        }
    }

    private fun Map<*, *>.toVaultItem(): DecryptedVaultItem {
        val title = string("title")
            ?: throw BackupFormatException("A backup item is missing its title.")

        return DecryptedVaultItem(
            id = string("id") ?: java.util.UUID.randomUUID().toString(),
            type = string("type")?.toVaultItemType() ?: VaultItemType.LOGIN,
            title = title,
            username = string("username"),
            secret = string("secret"),
            notes = string("notes"),
            totpSecret = string("totpSecret"),
            websiteUrl = string("websiteUrl"),
            updatedAtEpochMillis = (this["updatedAtEpochMillis"] as? Number)?.toLong()
                ?: System.currentTimeMillis(),
        )
    }

    private fun Map<*, *>.string(key: String): String? = this[key] as? String

    /** Unknown types restore as LOGIN rather than failing the whole import. */
    private fun String.toVaultItemType(): VaultItemType =
        VaultItemType.entries.firstOrNull { it.name == this } ?: VaultItemType.LOGIN

    private fun StringBuilder.appendItem(item: DecryptedVaultItem) {
        append('{')
        appendField("id", item.id)
        append(',')
        appendField("type", item.type.name)
        append(',')
        appendField("title", item.title)
        append(',')
        appendField("username", item.username)
        append(',')
        appendField("secret", item.secret)
        append(',')
        appendField("notes", item.notes)
        append(',')
        appendField("totpSecret", item.totpSecret)
        append(',')
        appendField("websiteUrl", item.websiteUrl)
        append(",\"updatedAtEpochMillis\":").append(item.updatedAtEpochMillis)
        append('}')
    }

    private fun StringBuilder.appendField(name: String, value: String?) {
        append('"').append(name).append("\":")
        if (value == null) append("null") else appendQuoted(value)
    }

    private fun StringBuilder.appendQuoted(value: String) {
        append('"')
        value.forEach { char ->
            when (char) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else ->
                    if (char.code < 0x20) {
                        append("\\u").append(char.code.toString(16).padStart(4, '0'))
                    } else {
                        append(char)
                    }
            }
        }
        append('"')
    }
}

/** A minimal, strict recursive-descent JSON parser. */
private class JsonParser(private val source: String) {

    private var index = 0

    fun parseDocument(): Any? {
        val value = parseValue()
        skipWhitespace()
        if (index != source.length) {
            throw BackupFormatException("Unexpected trailing content at position $index.")
        }
        return value
    }

    private fun parseValue(): Any? {
        skipWhitespace()
        if (index >= source.length) throw BackupFormatException("Unexpected end of backup content.")

        return when (val char = source[index]) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> parseString()
            't' -> parseLiteral("true", true)
            'f' -> parseLiteral("false", false)
            'n' -> parseLiteral("null", null)
            else ->
                if (char == '-' || char.isDigit()) {
                    parseNumber()
                } else {
                    throw BackupFormatException("Unexpected character '$char' at position $index.")
                }
        }
    }

    private fun parseObject(): Map<String, Any?> {
        expect('{')
        val result = LinkedHashMap<String, Any?>()
        skipWhitespace()

        if (peek() == '}') {
            index++
            return result
        }

        while (true) {
            skipWhitespace()
            val key = parseString()
            skipWhitespace()
            expect(':')
            result[key] = parseValue()
            skipWhitespace()

            when (val char = next()) {
                ',' -> Unit
                '}' -> return result
                else -> throw BackupFormatException("Expected ',' or '}' but found '$char'.")
            }
        }
    }

    private fun parseArray(): List<Any?> {
        expect('[')
        val result = mutableListOf<Any?>()
        skipWhitespace()

        if (peek() == ']') {
            index++
            return result
        }

        while (true) {
            result += parseValue()
            skipWhitespace()

            when (val char = next()) {
                ',' -> Unit
                ']' -> return result
                else -> throw BackupFormatException("Expected ',' or ']' but found '$char'.")
            }
        }
    }

    private fun parseString(): String {
        expect('"')
        val builder = StringBuilder()

        while (true) {
            when (val char = next()) {
                '"' -> return builder.toString()
                '\\' -> builder.append(parseEscape())
                else -> builder.append(char)
            }
        }
    }

    private fun parseEscape(): Char = when (val char = next()) {
        '"' -> '"'
        '\\' -> '\\'
        '/' -> '/'
        'b' -> '\b'
        'f' -> '\u000C'
        'n' -> '\n'
        'r' -> '\r'
        't' -> '\t'
        'u' -> {
            if (index + 4 > source.length) {
                throw BackupFormatException("Truncated \\u escape at position $index.")
            }
            val hex = source.substring(index, index + 4)
            index += 4
            hex.toIntOrNull(16)?.toChar()
                ?: throw BackupFormatException("Invalid \\u escape '$hex'.")
        }
        else -> throw BackupFormatException("Invalid escape '\\$char'.")
    }

    private fun parseNumber(): Number {
        val start = index
        if (peek() == '-') index++
        while (index < source.length && (source[index].isDigit() || source[index] in ".eE+-")) index++

        val text = source.substring(start, index)
        return text.toLongOrNull()
            ?: text.toDoubleOrNull()
            ?: throw BackupFormatException("Invalid number '$text'.")
    }

    private fun <T> parseLiteral(literal: String, value: T): T {
        if (!source.startsWith(literal, index)) {
            throw BackupFormatException("Invalid literal at position $index.")
        }
        index += literal.length
        return value
    }

    private fun skipWhitespace() {
        while (index < source.length && source[index].isWhitespace()) index++
    }

    private fun peek(): Char =
        if (index < source.length) source[index] else throw BackupFormatException("Unexpected end of backup content.")

    private fun next(): Char =
        if (index < source.length) source[index++] else throw BackupFormatException("Unexpected end of backup content.")

    private fun expect(expected: Char) {
        val char = next()
        if (char != expected) {
            throw BackupFormatException("Expected '$expected' but found '$char' at position ${index - 1}.")
        }
    }
}
