package com.mslabs.aegis.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private const val PERIOD_SECONDS = 30

/**
 * The animated countdown ring next to a TOTP code.
 *
 * Turns amber in the last third of the window so the user knows the code is about
 * to roll over before they start typing it.
 */
@Composable
fun TotpRing(
    secondsRemaining: Int,
    modifier: Modifier = Modifier,
    totalSeconds: Int = PERIOD_SECONDS,
) {
    val clamped = secondsRemaining.coerceIn(0, totalSeconds)
    val target = clamped / totalSeconds.toFloat()

    // Snap rather than sweep backwards when the period rolls over from 0 back to full.
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = if (target > 0.95f) tween(0) else tween(durationMillis = 950),
        label = "totpProgress",
    )

    val isExpiring = clamped <= totalSeconds / 3
    val ringColor = if (isExpiring) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .size(40.dp)
            .clearAndSetSemantics { contentDescription = "$clamped seconds remaining" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val stroke = Stroke(width = 3.dp.toPx())
            val inset = stroke.width / 2
            val diameter = size.minDimension - stroke.width

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = stroke,
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = stroke,
            )
        }

        Text(
            text = clamped.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = ringColor,
        )
    }
}

/** A TOTP code paired with its countdown ring. */
@Composable
fun TotpCodeRow(
    code: String,
    secondsRemaining: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TotpRing(secondsRemaining = secondsRemaining)
        Text(
            // Grouped 3-and-3 so it can be read aloud or typed without losing place.
            text = code.chunked(3).joinToString(" "),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
