package com.mslabs.aegis.presentation.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * The vault's single text input.
 *
 * Secure fields are masked by default and only reveal when the caller explicitly
 * opts in, so every reveal has an owner that can revoke it - see ItemDetailScreen,
 * which re-masks on ON_PAUSE per sdd.md §4.
 */
@Composable
fun AegisTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isSecure: Boolean = false,
    isRevealed: Boolean = false,
    onToggleReveal: (() -> Unit)? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val masked = isSecure && !isRevealed

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        visualTransformation = if (masked) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isSecure) KeyboardType.Password else keyboardType,
        ),
        trailingIcon = if (isSecure && onToggleReveal != null) {
            {
                TextButton(onClick = onToggleReveal) {
                    Text(
                        text = if (isRevealed) "Hide" else "Show",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        } else {
            null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
