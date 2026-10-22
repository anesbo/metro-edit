/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.materialkolor.score.Score
import com.metrolist.music.constants.BackgroundStyle
import com.metrolist.music.constants.BackgroundStyleDefault
import com.metrolist.music.constants.BackgroundStyleKey
import com.metrolist.music.constants.CustomPrimaryColorDefault
import com.metrolist.music.constants.CustomPrimaryColorKey
import com.metrolist.music.constants.CustomSecondaryColorDefault
import com.metrolist.music.constants.CustomSecondaryColorKey
import com.metrolist.music.constants.UseCustomAccentDefault
import com.metrolist.music.constants.UseCustomAccentKey
import com.metrolist.music.utils.rememberEnumPreference
import com.metrolist.music.utils.rememberPreference
import com.zionhuang.music.data.ThemeRepository
import com.zionhuang.music.model.CuratedTheme

val DefaultThemeColor = Color(0xFFED5564)

@Composable
fun MetrolistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    useCustomAccent: Boolean? = null,
    customPrimaryColor: Color? = null,
    customSecondaryColor: Color? = null,
    backgroundStyle: BackgroundStyle? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val actualUseCustomAccent = useCustomAccent ?: rememberPreference(UseCustomAccentKey, UseCustomAccentDefault).value
    val actualCustomPrimaryInt = customPrimaryColor?.toArgb() ?: rememberPreference(CustomPrimaryColorKey, CustomPrimaryColorDefault).value
    val actualCustomSecondaryInt = customSecondaryColor?.toArgb() ?: rememberPreference(CustomSecondaryColorKey, CustomSecondaryColorDefault).value
    val actualBackgroundStyle = backgroundStyle ?: rememberEnumPreference(BackgroundStyleKey, BackgroundStyleDefault).value

    // Observe active curated theme from ThemeRepository
    val themeRepository = remember(context) { ThemeRepository.getInstance(context) }
    val activeCuratedTheme by themeRepository.getActiveTheme().collectAsState(initial = null)

    // Determine if system dynamic colors should be used (Android S+ and default theme color when not using custom accent or curated theme)
    val useSystemDynamicColor = (activeCuratedTheme == null && !actualUseCustomAccent && themeColor == DefaultThemeColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    val effectiveSeedColor = if (actualUseCustomAccent) Color(actualCustomPrimaryInt) else themeColor

    // Select the appropriate base color scheme generation method
    val baseColorScheme = if (useSystemDynamicColor) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        rememberDynamicColorScheme(
            seedColor = effectiveSeedColor,
            isDark = darkTheme,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            style = PaletteStyle.TonalSpot,
        )
    }

    // Build the target color scheme: either from curated theme or custom accent / base scheme
    val targetColorScheme = remember(
        activeCuratedTheme,
        baseColorScheme,
        actualUseCustomAccent,
        actualCustomPrimaryInt,
        actualCustomSecondaryInt,
        darkTheme,
        pureBlack,
        actualBackgroundStyle,
    ) {
        val scheme = if (activeCuratedTheme != null) {
            buildCuratedColorScheme(activeCuratedTheme!!, darkTheme)
        } else if (actualUseCustomAccent) {
            val primary = Color(actualCustomPrimaryInt)
            val secondary = Color(actualCustomSecondaryInt)

            val onPrimary = primary.contrastText()
            val onSecondary = secondary.contrastText()

            baseColorScheme.copy(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primary.copy(alpha = if (darkTheme) 0.3f else 0.15f),
                onPrimaryContainer = if (darkTheme) Color.White else primary,
                secondary = secondary,
                onSecondary = onSecondary,
                secondaryContainer = secondary.copy(alpha = if (darkTheme) 0.3f else 0.15f),
                onSecondaryContainer = if (darkTheme) Color.White else secondary,
            )
        } else {
            baseColorScheme
        }

        var finalScheme = if (darkTheme && pureBlack && actualBackgroundStyle == BackgroundStyle.DEFAULT) {
            scheme.pureBlack(true)
        } else {
            scheme
        }

        if (actualBackgroundStyle == BackgroundStyle.SOLID_CUSTOM || actualBackgroundStyle == BackgroundStyle.GRADIENT) {
            finalScheme = finalScheme.copy(
                background = Color.Transparent,
                surface = Color.Transparent,
            )
        }

        finalScheme
    }

    // Smooth Color Transitions with tween(400)
    val animatedColorScheme = targetColorScheme.animateColors(400)

    MaterialTheme(
        colorScheme = animatedColorScheme,
        content = content,
    )
}

@Composable
fun MusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    useCustomAccent: Boolean? = null,
    customPrimaryColor: Color? = null,
    customSecondaryColor: Color? = null,
    backgroundStyle: BackgroundStyle? = null,
    content: @Composable () -> Unit,
) {
    MetrolistTheme(
        darkTheme = darkTheme,
        pureBlack = pureBlack,
        themeColor = themeColor,
        useCustomAccent = useCustomAccent,
        customPrimaryColor = customPrimaryColor,
        customSecondaryColor = customSecondaryColor,
        backgroundStyle = backgroundStyle,
        content = content,
    )
}

/**
 * Derives a complete Material 3 ColorScheme dynamically from a CuratedTheme.
 */
fun buildCuratedColorScheme(
    theme: CuratedTheme,
    isDark: Boolean,
): ColorScheme {
    val primary = theme.primaryColor
    val secondary = theme.secondaryColor
    val background = theme.backgroundColor
    val surface = theme.surfaceColor

    val onPrimary = primary.contrastText()
    val onSecondary = secondary.contrastText()
    val onBackground = background.contrastText()
    val onSurface = surface.contrastText()

    val primaryContainer = if (isDark) primary.copy(alpha = 0.30f) else primary.copy(alpha = 0.18f)
    val onPrimaryContainer = if (isDark) Color.White else primary

    val secondaryContainer = if (isDark) secondary.copy(alpha = 0.30f) else secondary.copy(alpha = 0.18f)
    val onSecondaryContainer = if (isDark) Color.White else secondary

    val surfaceVariant = if (isDark) surface.lighten(0.08f) else surface.darken(0.06f)
    val onSurfaceVariant = onSurface.copy(alpha = 0.75f)

    val surfaceContainer = surface
    val surfaceContainerLow = if (isDark) surface.darken(0.04f) else surface.lighten(0.04f)
    val surfaceContainerLowest = if (isDark) surface.darken(0.08f) else surface.lighten(0.08f)
    val surfaceContainerHigh = if (isDark) surface.lighten(0.05f) else surface.darken(0.05f)
    val surfaceContainerHighest = if (isDark) surface.lighten(0.09f) else surface.darken(0.09f)

    val outline = onSurface.copy(alpha = 0.28f)
    val outlineVariant = onSurface.copy(alpha = 0.16f)

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = primary.lighten(0.2f),
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = secondary,
            onTertiary = onSecondary,
            tertiaryContainer = secondaryContainer,
            onTertiaryContainer = onSecondaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = primary,
            inverseSurface = onSurface,
            inverseOnSurface = surface,
            surfaceContainer = surfaceContainer,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            outline = outline,
            outlineVariant = outlineVariant,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = primary.darken(0.2f),
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = secondary,
            onTertiary = onSecondary,
            tertiaryContainer = secondaryContainer,
            onTertiaryContainer = onSecondaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = primary,
            inverseSurface = onSurface,
            inverseOnSurface = surface,
            surfaceContainer = surfaceContainer,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            outline = outline,
            outlineVariant = outlineVariant,
        )
    }
}

/**
 * Computes high-contrast text color (pure white or deep black) for 100% readability.
 */
fun Color.contrastText(): Color {
    val luminance = 0.2126f * red + 0.7152f * green + 0.0722f * blue
    return if (luminance > 0.45f) Color(0xFF111111) else Color(0xFFFCFCFC)
}

fun Color.lighten(fraction: Float): Color {
    return Color(
        red = (red + (1f - red) * fraction).coerceIn(0f, 1f),
        green = (green + (1f - green) * fraction).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * fraction).coerceIn(0f, 1f),
        alpha = alpha,
    )
}

fun Color.darken(fraction: Float): Color {
    return Color(
        red = (red * (1f - fraction)).coerceIn(0f, 1f),
        green = (green * (1f - fraction)).coerceIn(0f, 1f),
        blue = (blue * (1f - fraction)).coerceIn(0f, 1f),
        alpha = alpha,
    )
}

/**
 * Smoothly animates all colors of a ColorScheme with crossfade transitions.
 */
@Composable
fun ColorScheme.animateColors(durationMillis: Int = 400): ColorScheme {
    val spec = tween<Color>(durationMillis)
    return copy(
        primary = animateColorAsState(primary, spec, label = "primary").value,
        onPrimary = animateColorAsState(onPrimary, spec, label = "onPrimary").value,
        primaryContainer = animateColorAsState(primaryContainer, spec, label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(onPrimaryContainer, spec, label = "onPrimaryContainer").value,
        inversePrimary = animateColorAsState(inversePrimary, spec, label = "inversePrimary").value,
        secondary = animateColorAsState(secondary, spec, label = "secondary").value,
        onSecondary = animateColorAsState(onSecondary, spec, label = "onSecondary").value,
        secondaryContainer = animateColorAsState(secondaryContainer, spec, label = "secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(onSecondaryContainer, spec, label = "onSecondaryContainer").value,
        tertiary = animateColorAsState(tertiary, spec, label = "tertiary").value,
        onTertiary = animateColorAsState(onTertiary, spec, label = "onTertiary").value,
        tertiaryContainer = animateColorAsState(tertiaryContainer, spec, label = "tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(onTertiaryContainer, spec, label = "onTertiaryContainer").value,
        background = animateColorAsState(background, spec, label = "background").value,
        onBackground = animateColorAsState(onBackground, spec, label = "onBackground").value,
        surface = animateColorAsState(surface, spec, label = "surface").value,
        onSurface = animateColorAsState(onSurface, spec, label = "onSurface").value,
        surfaceVariant = animateColorAsState(surfaceVariant, spec, label = "surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(onSurfaceVariant, spec, label = "onSurfaceVariant").value,
        surfaceTint = animateColorAsState(surfaceTint, spec, label = "surfaceTint").value,
        inverseSurface = animateColorAsState(inverseSurface, spec, label = "inverseSurface").value,
        inverseOnSurface = animateColorAsState(inverseOnSurface, spec, label = "inverseOnSurface").value,
        error = animateColorAsState(error, spec, label = "error").value,
        onError = animateColorAsState(onError, spec, label = "onError").value,
        errorContainer = animateColorAsState(errorContainer, spec, label = "errorContainer").value,
        onErrorContainer = animateColorAsState(onErrorContainer, spec, label = "onErrorContainer").value,
        outline = animateColorAsState(outline, spec, label = "outline").value,
        outlineVariant = animateColorAsState(outlineVariant, spec, label = "outlineVariant").value,
        scrim = animateColorAsState(scrim, spec, label = "scrim").value,
        surfaceBright = animateColorAsState(surfaceBright, spec, label = "surfaceBright").value,
        surfaceDim = animateColorAsState(surfaceDim, spec, label = "surfaceDim").value,
        surfaceContainer = animateColorAsState(surfaceContainer, spec, label = "surfaceContainer").value,
        surfaceContainerHigh = animateColorAsState(surfaceContainerHigh, spec, label = "surfaceContainerHigh").value,
        surfaceContainerHighest = animateColorAsState(surfaceContainerHighest, spec, label = "surfaceContainerHighest").value,
        surfaceContainerLow = animateColorAsState(surfaceContainerLow, spec, label = "surfaceContainerLow").value,
        surfaceContainerLowest = animateColorAsState(surfaceContainerLowest, spec, label = "surfaceContainerLowest").value,
    )
}

fun Bitmap.extractThemeColor(): Color = Color(
    Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .rankedColors(1, DefaultThemeColor.toArgb())
        .first()
)

internal fun Palette.rankedColors(
    desiredColorCount: Int,
    fallbackColor: Int,
): List<Int> = Score.score(
    swatches.associate { it.rgb to it.population },
    desiredColorCount,
    fallbackColor,
    true,
)

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black,
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}
