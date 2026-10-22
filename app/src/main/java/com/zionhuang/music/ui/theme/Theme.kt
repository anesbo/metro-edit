/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.zionhuang.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.metrolist.music.constants.BackgroundStyle
import com.zionhuang.music.model.CuratedTheme

import com.metrolist.music.ui.theme.animateColors as metrolistAnimateColors

val DefaultThemeColor: Color get() = com.metrolist.music.ui.theme.DefaultThemeColor

@Composable
fun MusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = com.metrolist.music.ui.theme.DefaultThemeColor,
    useCustomAccent: Boolean? = null,
    customPrimaryColor: Color? = null,
    customSecondaryColor: Color? = null,
    backgroundStyle: BackgroundStyle? = null,
    content: @Composable () -> Unit,
) {
    com.metrolist.music.ui.theme.MusicTheme(
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

@Composable
fun MetrolistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = com.metrolist.music.ui.theme.DefaultThemeColor,
    useCustomAccent: Boolean? = null,
    customPrimaryColor: Color? = null,
    customSecondaryColor: Color? = null,
    backgroundStyle: BackgroundStyle? = null,
    content: @Composable () -> Unit,
) {
    com.metrolist.music.ui.theme.MetrolistTheme(
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

fun buildCuratedColorScheme(
    theme: CuratedTheme,
    isDark: Boolean,
): ColorScheme = com.metrolist.music.ui.theme.buildCuratedColorScheme(theme, isDark)

@Composable
fun ColorScheme.animateColors(durationMillis: Int = 400): ColorScheme {
    return this.metrolistAnimateColors(durationMillis)
}
