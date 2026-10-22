/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.music.constants.GlassIntensityDefault
import com.metrolist.music.constants.GlassIntensityKey
import com.metrolist.music.constants.GlassStyle
import com.metrolist.music.constants.GlassStyleDefault
import com.metrolist.music.constants.GlassStyleKey
import com.metrolist.music.ui.modifier.liquidGlass
import com.metrolist.music.ui.screens.Screens
import com.metrolist.music.ui.utils.glassmorphic
import com.metrolist.music.utils.rememberEnumPreference
import com.metrolist.music.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Stable
private fun isRouteSelected(currentRoute: String?, screenRoute: String, navigationItems: List<Screens>): Boolean {
    if (currentRoute == null) return false
    if (currentRoute == screenRoute) return true
    if (navigationItems.any { it.route == screenRoute } &&
        currentRoute.startsWith("$screenRoute/")) return true

    // Fix: match the route template, not the resolved route
    if (screenRoute == "search_input" &&
        (currentRoute.startsWith("search/") || currentRoute == "search/{query}")) return true

    return false
}

@Composable
fun AppNavigationRail(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null,
    onHomeLongHold: (() -> Unit)? = null,
) {
    val glassStyle by rememberEnumPreference(GlassStyleKey, defaultValue = GlassStyleDefault)
    val glassIntensity by rememberPreference(GlassIntensityKey, defaultValue = GlassIntensityDefault)
    val isGlass = glassStyle != GlassStyle.NONE

    val containerColor = if (isGlass) Color.Transparent else if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
    val haptics = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current

    val railShape = RoundedCornerShape(20.dp)
    val railItemColors = if (isGlass) {
        val selectedColor = if (glassStyle == GlassStyle.FROSTY) MaterialTheme.colorScheme.primary else Color.White
        val unselectedColor = if (glassStyle == GlassStyle.FROSTY) Color.Black.copy(alpha = 0.70f) else Color.White.copy(alpha = 0.70f)
        val indicatorColor = if (glassStyle == GlassStyle.FROSTY) Color.Black.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.22f)
        NavigationRailItemDefaults.colors(
            selectedIconColor = selectedColor,
            unselectedIconColor = unselectedColor,
            indicatorColor = indicatorColor,
        )
    } else {
        NavigationRailItemDefaults.colors()
    }

    val railModifier = if (isGlass) {
        modifier
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
            .shadow(
                elevation = 10.dp,
                shape = railShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.35f),
            )
            .liquidGlass(
                style = glassStyle,
                intensity = glassIntensity,
                shape = railShape,
            )
    } else {
        modifier.glassmorphic(glassStyle, glassIntensity, RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
    }

    NavigationRail(
        modifier = railModifier,
        containerColor = containerColor,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        navigationItems.forEach { screen ->
            val isSelected = remember(currentRoute, screen.route) {
                isRouteSelected(currentRoute, screen.route, navigationItems)
            }
            val currentIsSelected by rememberUpdatedState(isSelected)
            val iconRes = remember(isSelected, screen) {
                if (isSelected) screen.iconIdActive else screen.iconIdInactive
            }

            val isSearchItem = screen == Screens.Search && onSearchLongClick != null
            val isHomeHoldItem = screen == Screens.Home && onHomeLongHold != null
            val interactionSource = remember { MutableInteractionSource() }

            // Long press detection using InteractionSource
            if (isSearchItem || isHomeHoldItem) {
                LaunchedEffect(interactionSource) {
                    var isLongClick = false
                    interactionSource.interactions.collectLatest { interaction ->
                        when (interaction) {
                            is PressInteraction.Press -> {
                                isLongClick = false
                                delay(if (isHomeHoldItem) 15_000L else viewConfiguration.longPressTimeoutMillis)
                                isLongClick = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isHomeHoldItem) onHomeLongHold.invoke() else onSearchLongClick?.invoke()
                            }
                            is PressInteraction.Release -> {
                                if (!isLongClick) {
                                    onItemClick(screen, currentIsSelected)
                                }
                            }
                            is PressInteraction.Cancel -> {
                                isLongClick = false
                            }
                        }
                    }
                }
            }

            NavigationRailItem(
                selected = isSelected,
                onClick = {
                    if (!isSearchItem && !isHomeHoldItem) {
                        onItemClick(screen, currentIsSelected)
                    }
                    // Long presses are handled via InteractionSource
                },
                interactionSource = interactionSource,
                colors = railItemColors,
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = stringResource(screen.titleId),
                    )
                },
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun AppNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    slimNav: Boolean = false,
    onSearchLongClick: (() -> Unit)? = null,
    onHomeLongHold: (() -> Unit)? = null,
) {
    val glassStyle by rememberEnumPreference(GlassStyleKey, defaultValue = GlassStyleDefault)
    val glassIntensity by rememberPreference(GlassIntensityKey, defaultValue = GlassIntensityDefault)
    val isGlass = glassStyle != GlassStyle.NONE

    val containerColor = if (isGlass) Color.Transparent else if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
    val contentColor = if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val haptics = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current

    val navBarShape = RoundedCornerShape(20.dp)

    val navItemColors = if (isGlass) {
        val selectedColor = if (glassStyle == GlassStyle.FROSTY) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.White
        }
        val unselectedColor = if (glassStyle == GlassStyle.FROSTY) {
            Color.Black.copy(alpha = 0.70f)
        } else {
            Color.White.copy(alpha = 0.70f)
        }
        val indicatorColor = if (glassStyle == GlassStyle.FROSTY) {
            Color.Black.copy(alpha = 0.12f)
        } else {
            Color.White.copy(alpha = 0.22f)
        }
        NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor,
            indicatorColor = indicatorColor,
        )
    } else {
        NavigationBarItemDefaults.colors()
    }

    val navContent: @Composable RowScope.() -> Unit = {
        navigationItems.forEach { screen ->
            val isSelected = remember(currentRoute, screen.route) {
                isRouteSelected(currentRoute, screen.route, navigationItems)
            }
            val currentIsSelected by rememberUpdatedState(isSelected)
            val iconRes = remember(isSelected, screen) {
                if (isSelected) screen.iconIdActive else screen.iconIdInactive
            }

            val isSearchItem = screen == Screens.Search && onSearchLongClick != null
            val isHomeHoldItem = screen == Screens.Home && onHomeLongHold != null
            val interactionSource = remember { MutableInteractionSource() }

            // Long press detection using InteractionSource
            if (isSearchItem || isHomeHoldItem) {
                LaunchedEffect(interactionSource) {
                    var isLongClick = false
                    interactionSource.interactions.collectLatest { interaction ->
                        when (interaction) {
                            is PressInteraction.Press -> {
                                isLongClick = false
                                delay(if (isHomeHoldItem) 15_000L else viewConfiguration.longPressTimeoutMillis)
                                isLongClick = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isHomeHoldItem) onHomeLongHold.invoke() else onSearchLongClick?.invoke()
                            }
                            is PressInteraction.Release -> {
                                if (!isLongClick) {
                                    onItemClick(screen, currentIsSelected)
                                }
                            }
                            is PressInteraction.Cancel -> {
                                isLongClick = false
                            }
                        }
                    }
                }
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSearchItem && !isHomeHoldItem) {
                        onItemClick(screen, currentIsSelected)
                    }
                    // Long presses are handled via InteractionSource
                },
                interactionSource = interactionSource,
                colors = navItemColors,
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = stringResource(screen.titleId),
                    )
                },
                label = if (!slimNav) {
                    {
                        Text(
                            text = stringResource(screen.titleId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                } else null,
            )
        }
    }

    if (isGlass) {
        val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(
            modifier = modifier,
            contentAlignment = Alignment.BottomCenter,
        ) {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = if (bottomInset > 0.dp) bottomInset else 8.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = navBarShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.35f),
                    )
                    .liquidGlass(
                        style = glassStyle,
                        intensity = glassIntensity,
                        shape = navBarShape,
                    ),
                containerColor = Color.Transparent,
                contentColor = contentColor,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                content = navContent,
            )
        }
    } else {
        NavigationBar(
            modifier = modifier.glassmorphic(glassStyle, glassIntensity, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            containerColor = containerColor,
            contentColor = contentColor,
            tonalElevation = NavigationBarDefaults.Elevation,
            content = navContent,
        )
    }
}
