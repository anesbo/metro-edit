/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.zionhuang.music.ui.modifier

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.metrolist.music.constants.GlassStyle
import com.metrolist.music.ui.modifier.liquidGlass as coreLiquidGlass

/**
 * Compatibility alias matching the original package namespace.
 * Delegates directly to [com.metrolist.music.ui.modifier.liquidGlass].
 */
fun Modifier.liquidGlass(
    style: GlassStyle = GlassStyle.BLURRY,
    intensity: Float = 0.6f,
    shape: Shape = RoundedCornerShape(24.dp),
    isCurvedBevel: Boolean = true,
): Modifier = coreLiquidGlass(
    style = style,
    intensity = intensity,
    shape = shape,
    isCurvedBevel = isCurvedBevel,
)
