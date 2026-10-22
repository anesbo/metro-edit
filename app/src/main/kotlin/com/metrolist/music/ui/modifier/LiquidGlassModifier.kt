/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.modifier

import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.metrolist.music.constants.GlassStyle

/**
 * High-fidelity iOS and visionOS-style "Liquid Glass" modifier.
 *
 * Simulates physical optical properties of curved convex lens glass:
 * 1. Convex Specular Lighting: Multi-stop surface gradient sheen with upper highlight,
 *    translucent center, and ambient bounce reflection.
 * 2. Refractive Beveled Rim: Diagonal lighting border catching ambient specular light on top-left
 *    and dispersing into a dark refraction rim on bottom-right.
 * 3. Hardware Backdrop Blur (API 31+): Uses [RenderEffect.createBlurEffect] on a background
 *    [RenderNode] exclusively in the [onDrawBehind] phase, keeping child composables 100% crisp and unblurred.
 * 4. Graceful Fallback (API < 31): High-saturation frosted multi-stop diffusion layers.
 *
 * @param style The glass style ([GlassStyle.BLURRY], [GlassStyle.FROSTY], [GlassStyle.NONE]).
 * @param intensity Glass intensity factor from 0.0f to 1.0f (default: 0.6f).
 * @param shape Corner shape for continuous curvature (default: 24.dp rounded corners).
 * @param isCurvedBevel Whether to render convex specular flares and inner beveled refractive highlights.
 */
fun Modifier.liquidGlass(
    style: GlassStyle = GlassStyle.BLURRY,
    intensity: Float = 0.6f,
    shape: Shape = RoundedCornerShape(24.dp),
    isCurvedBevel: Boolean = true,
): Modifier {
    if (style == GlassStyle.NONE || intensity <= 0.01f) {
        return this
    }

    val clampedIntensity = intensity.coerceIn(0.05f, 1.0f)
    val blurRadius = (clampedIntensity * 32f).coerceIn(8f, 60f)

    // Base translucent backdrop tints depending on glass style
    val backdropColor = when (style) {
        GlassStyle.BLURRY -> Color(0xFF12131C).copy(
            alpha = (0.35f + 0.35f * clampedIntensity).coerceIn(0.25f, 0.85f)
        )
        GlassStyle.FROSTY -> Color(0xFFF2F4FA).copy(
            alpha = (0.22f + 0.38f * clampedIntensity).coerceIn(0.18f, 0.80f)
        )
        GlassStyle.NONE -> Color.Transparent
    }

    // Dynamic beveled rim border gradient: top-left specular highlight to bottom-right refraction shadow
    val beveledBorderBrush = when (style) {
        GlassStyle.BLURRY -> Brush.linearGradient(
            0.0f to Color.White.copy(alpha = (0.45f * clampedIntensity).coerceIn(0.12f, 0.85f)),
            0.35f to Color.White.copy(alpha = (0.18f * clampedIntensity).coerceIn(0.04f, 0.40f)),
            0.70f to Color.Black.copy(alpha = (0.08f * clampedIntensity).coerceIn(0.02f, 0.20f)),
            1.0f to Color.Black.copy(alpha = (0.22f * clampedIntensity).coerceIn(0.06f, 0.45f)),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
        GlassStyle.FROSTY -> Brush.linearGradient(
            0.0f to Color.White.copy(alpha = (0.65f * clampedIntensity).coerceIn(0.20f, 0.95f)),
            0.45f to Color.White.copy(alpha = (0.32f * clampedIntensity).coerceIn(0.10f, 0.65f)),
            1.0f to Color.White.copy(alpha = (0.15f * clampedIntensity).coerceIn(0.05f, 0.40f)),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
        GlassStyle.NONE -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }

    return this
        .clip(shape)
        .drawWithCache {
            val renderNode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                RenderNode("LiquidGlassBackdrop").apply {
                    setRenderEffect(
                        RenderEffect.createBlurEffect(
                            blurRadius,
                            blurRadius,
                            Shader.TileMode.CLAMP,
                        )
                    )
                }
            } else {
                null
            }

            val paint = Paint().apply {
                isAntiAlias = true
                color = backdropColor.toArgb()
            }

            // Convex Specular Lighting: Multi-stop surface gradient sheen
            val specularSheenBrush = when (style) {
                GlassStyle.BLURRY -> Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = (0.22f * clampedIntensity).coerceIn(0.05f, 0.50f)),
                    0.15f to Color.White.copy(alpha = (0.10f * clampedIntensity).coerceIn(0.02f, 0.25f)),
                    0.50f to Color.White.copy(alpha = (0.015f * clampedIntensity).coerceIn(0.005f, 0.08f)),
                    0.85f to Color.Black.copy(alpha = (0.08f * clampedIntensity).coerceIn(0.02f, 0.20f)),
                    1.0f to Color.White.copy(alpha = (0.06f * clampedIntensity).coerceIn(0.01f, 0.18f)),
                )
                GlassStyle.FROSTY -> Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = (0.38f * clampedIntensity).coerceIn(0.10f, 0.70f)),
                    0.20f to Color.White.copy(alpha = (0.20f * clampedIntensity).coerceIn(0.05f, 0.45f)),
                    0.55f to Color.White.copy(alpha = (0.08f * clampedIntensity).coerceIn(0.02f, 0.20f)),
                    0.88f to Color.White.copy(alpha = (0.04f * clampedIntensity).coerceIn(0.01f, 0.12f)),
                    1.0f to Color.White.copy(alpha = (0.14f * clampedIntensity).coerceIn(0.03f, 0.30f)),
                )
                GlassStyle.NONE -> null
            }

            val flareBrush = if (isCurvedBevel && size.width > 0f) {
                val flareAlpha = when (style) {
                    GlassStyle.BLURRY -> 0.16f * clampedIntensity
                    GlassStyle.FROSTY -> 0.26f * clampedIntensity
                    GlassStyle.NONE -> 0f
                }
                if (flareAlpha > 0.01f) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = flareAlpha),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.45f, 0f),
                        radius = size.width * 0.75f,
                    )
                } else null
            } else null

            val innerBevelAlpha = (0.28f * clampedIntensity).coerceIn(0.05f, 0.55f)
            val innerBevelBrush = if (isCurvedBevel) {
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = innerBevelAlpha),
                    0.08f to Color.White.copy(alpha = innerBevelAlpha * 0.3f),
                    0.25f to Color.Transparent,
                )
            } else null

            onDrawBehind {
                // 1. Hardware backdrop blur layer (API 31+) or frosted diffusion fallback (API < 31)
                // Rendered exclusively in onDrawBehind so child composables remain 100% crisp.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && renderNode != null) {
                    val w = size.width.toInt().coerceAtLeast(1)
                    val h = size.height.toInt().coerceAtLeast(1)
                    renderNode.setPosition(0, 0, w, h)
                    val recordingCanvas = renderNode.beginRecording()
                    recordingCanvas.drawRect(0f, 0f, size.width, size.height, paint)
                    renderNode.endRecording()

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRenderNode(renderNode)
                    }
                } else {
                    drawRect(color = backdropColor)
                }

                // 2. Convex Specular Lighting
                if (specularSheenBrush != null) {
                    drawRect(brush = specularSheenBrush)
                }

                // 3. Ambient Flare & Inner Refractive Bevel
                if (flareBrush != null) {
                    drawRect(brush = flareBrush)
                }
                if (innerBevelBrush != null) {
                    drawRect(brush = innerBevelBrush)
                }
            }
        }
        .border(
            width = 1.dp,
            brush = beveledBorderBrush,
            shape = shape,
        )
}
