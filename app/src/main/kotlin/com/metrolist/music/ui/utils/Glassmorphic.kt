/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.utils

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.metrolist.music.constants.GlassStyle

/**
 * Applies a high-performance glassmorphic background layer and refractive glass border to a Composable.
 *
 * Uses [drawBehind] to render the translucent glass surface directly beneath child content,
 * ensuring that child elements (text, icons, badges) remain perfectly sharp, legible, and unblurred.
 *
 * @param style The glassmorphism style ([GlassStyle.NONE], [GlassStyle.BLURRY], [GlassStyle.FROSTY]).
 * @param intensity Glass intensity factor from 0.0f to 1.0f.
 * @param shape Corner shape for clipping the glass layer and border. Defaults to 16.dp rounded corners.
 */
fun Modifier.glassmorphic(
    style: GlassStyle,
    intensity: Float,
    shape: Shape = RoundedCornerShape(16.dp),
): Modifier {
    if (style == GlassStyle.NONE || intensity <= 0.01f) {
        return this
    }

    val clampedIntensity = intensity.coerceIn(0f, 1f)
    // Blur radius calculated from intensity
    val blurRadius = (clampedIntensity * 30f).coerceAtLeast(1f)

    return this
        .clip(shape)
        .drawBehind {
            when (style) {
                GlassStyle.BLURRY -> {
                    // Dark / surface adaptive translucent tint
                    val alpha = (0.3f + 0.4f * clampedIntensity).coerceIn(0f, 1f)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f * clampedIntensity),
                                Color.Black.copy(alpha = alpha),
                            )
                        )
                    )
                }
                GlassStyle.FROSTY -> {
                    // High-luminance milky white/frosty tint blended over surface
                    val alpha = (0.15f + 0.35f * clampedIntensity).coerceIn(0f, 1f)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = (alpha + 0.15f).coerceAtMost(0.95f)),
                                Color.White.copy(alpha = alpha),
                            )
                        )
                    )
                }
                GlassStyle.NONE -> Unit
            }
        }
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = (0.5f * clampedIntensity).coerceIn(0.05f, 0.85f)),
                    Color.White.copy(alpha = (0.12f * clampedIntensity).coerceIn(0.01f, 0.35f)),
                )
            ),
            shape = shape,
        )
}
