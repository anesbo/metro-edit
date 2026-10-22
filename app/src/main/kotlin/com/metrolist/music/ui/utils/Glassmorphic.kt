/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.metrolist.music.constants.GlassStyle
import com.metrolist.music.ui.modifier.liquidGlass

/**
 * Applies high-fidelity iOS and visionOS-style "Liquid Glass" modifier.
 *
 * Features:
 * - Convex Specular Lighting with multi-stop sheen mimicking light reflecting off a curved lens.
 * - Refractive Beveled Rim catching ambient light on top-left and dispersing to shadow on bottom-right.
 * - Hardware backdrop blur (API 31+) in the background draw phase with crisp, unblurred child composables.
 * - Graceful frosted fallback on API < 31.
 *
 * @param style The glassmorphism style ([GlassStyle.NONE], [GlassStyle.BLURRY], [GlassStyle.FROSTY]).
 * @param intensity Glass intensity factor from 0.0f to 1.0f.
 * @param shape Corner shape for clipping the glass layer and border. Defaults to 16.dp rounded corners.
 */
fun Modifier.glassmorphic(
    style: GlassStyle,
    intensity: Float,
    shape: Shape = RoundedCornerShape(16.dp),
): Modifier = liquidGlass(
    style = style,
    intensity = intensity,
    shape = shape,
    isCurvedBevel = true,
)

