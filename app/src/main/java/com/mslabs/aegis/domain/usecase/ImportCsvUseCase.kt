package com.mslabs.aegis.domain.usecase

import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.model.VaultItemType
import javax.inject.Inject

class ImportCsvUseCase @Inject constructor() {

    operator fun invoke(csvText: String): List<DecryptedVaultItem> {
        require(csvText.isNotBlank()) { "CSV content cannot be blank." }

        val rows = parseCsv(csvText)
            .filter { row -> row.any { it.isNotBlank() } }
        if (rows.isEmpty()) return emptyList()

        val headers = rows.first().map { it.trim().lowercase() }
        val dataRows = rows.drop(1)

        return dataRows.mapNotNull { row ->
            val values = headers.zip(row.padTo(headers.size)).toMap()
            val title = values.firstValue("name", "title", "item name", "label").orEmpty()
            val username = values.firstValue("username", "login", "email")
            val password = values.firstValue("password", "secret")
            val url = values.firstValue("url", "website", "website url", "login uri")
            val notes = values.firstValue("note", "notes", "comments")
            val totpSecret = values.firstValue("otp", "totp", "totp secret", "one-time password")

            if (title.isBlank() && username.isNullOrBlank() && password.isNullOrBlank()) {
                null
            } else {
                DecryptedVaultItem(
                    type = VaultItemType.LOGIN,
                    title = title.ifBlank { url ?: username ?: "Imported Login" },
                    username = username.nullIfBlank(),
                    secret = password.nullIfBlank(),
                    notes = notes.nullIfBlank(),
                    totpSecret = totpSecret.nullIfBlank(),
                    websiteUrl = url.nullIfBlank(),
                )
            }
        }
    }

    private fun parseCsv(csvText: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentValue = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < csvText.length) {
            val char = csvText[index]
            when {
                char == '"' && inQuotes && csvText.getOrNull(index + 1) == '"' -> {
                    currentValue.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    currentRow += currentValue.toString()
                    currentValue.clear()
                }
                (char == '\n' || char == '\r') && !inQuotes -> {
                    if (char == '\r' && csvText.getOrNull(index + 1) == '\n') index++
                    currentRow += currentValue.toString()
                    rows += currentRow.toList()
                    currentRow.clear()
                    currentValue.clear()
                }
                else -> currentValue.append(char)
            }
            index++
        }

        currentRow += currentValue.toString()
        if (currentRow.any { it.isNotEmpty() }) rows += currentRow

        return rows
    }

    private fun List<String>.padTo(size: Int): List<String> {
        return this + List((size - this.size).coerceAtLeast(0)) { "" }
    }

    private fun Map<String, String>.firstValue(vararg keys: String): String? {
        return keys.firstNotNullOfOrNull { key -> this[key]?.trim()?.takeIf { it.isNotBlank() } }
    }

    private fun String?.nullIfBlank(): String? {
        return this?.takeIf { it.isNotBlank() }
    }
}
