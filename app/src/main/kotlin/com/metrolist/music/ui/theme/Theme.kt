/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
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

    // Determine if system dynamic colors should be used (Android S+ and default theme color when not using custom accent)
    val useSystemDynamicColor = (!actualUseCustomAccent && themeColor == DefaultThemeColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    val effectiveSeedColor = if (actualUseCustomAccent) Color(actualCustomPrimaryInt) else themeColor

    // Select the appropriate color scheme generation method
    val baseColorScheme = if (useSystemDynamicColor) {
        // Use standard Material 3 dynamic color functions for system wallpaper colors
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        // Use materialKolor with seed color
        rememberDynamicColorScheme(
            seedColor = effectiveSeedColor,
            isDark = darkTheme,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            style = PaletteStyle.TonalSpot
        )
    }

    // Apply custom accent colors if enabled
    val customAccentColorScheme = remember(baseColorScheme, actualUseCustomAccent, actualCustomPrimaryInt, actualCustomSecondaryInt, darkTheme) {
        if (actualUseCustomAccent) {
            val primary = Color(actualCustomPrimaryInt)
            val secondary = Color(actualCustomSecondaryInt)

            val primaryLuminance = (0.299f * primary.red + 0.587f * primary.green + 0.114f * primary.blue)
            val onPrimary = if (primaryLuminance > 0.5f) Color.Black else Color.White

            val secondaryLuminance = (0.299f * secondary.red + 0.587f * secondary.green + 0.114f * secondary.blue)
            val onSecondary = if (secondaryLuminance > 0.5f) Color.Black else Color.White

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
    }

    // Apply pureBlack modification and custom background transparency
    val colorScheme = remember(customAccentColorScheme, pureBlack, darkTheme, actualBackgroundStyle) {
        var scheme = if (darkTheme && pureBlack && actualBackgroundStyle == BackgroundStyle.DEFAULT) {
            customAccentColorScheme.pureBlack(true)
        } else {
            customAccentColorScheme
        }

        if (actualBackgroundStyle == BackgroundStyle.SOLID_CUSTOM || actualBackgroundStyle == BackgroundStyle.GRADIENT) {
            scheme = scheme.copy(
                background = Color.Transparent,
                surface = Color.Transparent,
            )
        }

        scheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
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
        background = Color.Black
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}
