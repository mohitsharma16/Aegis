package com.mslabs.aegis.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AegisCyan,
    onPrimary = AegisObsidian,
    secondary = AegisMint,
    onSecondary = AegisObsidian,
    tertiary = AegisAmber,
    onTertiary = AegisObsidian,
    background = AegisObsidian,
    onBackground = AegisTextPrimary,
    surface = AegisGraphite,
    onSurface = AegisTextPrimary,
    surfaceVariant = AegisPanel,
    onSurfaceVariant = AegisTextSecondary,
    error = AegisRose,
)

private val LightColorScheme = lightColorScheme(
    primary = AegisCyan,
    onPrimary = AegisObsidian,
    secondary = AegisMint,
    onSecondary = AegisObsidian,
    tertiary = AegisAmber,
    onTertiary = AegisObsidian,
    background = AegisTextPrimary,
    onBackground = AegisObsidian,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = AegisObsidian,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE5EDF1),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF43515A),
    error = AegisRose,
)

@Composable
fun AegisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    ConfigureSystemBars(darkTheme = darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AegisTypography,
        content = content,
    )
}

/**
 * Under edge-to-edge the bars are transparent and drawn behind by the system, so
 * statusBarColor/navigationBarColor are no-ops on API 35+. All that is left to
 * control is whether the system draws its icons dark or light.
 */
@Composable
private fun ConfigureSystemBars(darkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
}
