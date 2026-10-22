package com.metrolist.music.ui.screens.settings

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import com.metrolist.music.R
import com.metrolist.music.constants.BackgroundStyle
import com.metrolist.music.constants.BackgroundStyleDefault
import com.metrolist.music.constants.BackgroundStyleKey
import com.metrolist.music.constants.CustomBgColorDefault
import com.metrolist.music.constants.CustomBgColorKey
import com.metrolist.music.constants.CustomPrimaryColorDefault
import com.metrolist.music.constants.CustomPrimaryColorKey
import com.metrolist.music.constants.CustomSecondaryColorDefault
import com.metrolist.music.constants.CustomSecondaryColorKey
import com.metrolist.music.constants.DarkModeKey
import com.metrolist.music.constants.DynamicThemeKey
import com.metrolist.music.constants.GradientDirection
import com.metrolist.music.constants.GradientDirectionDefault
import com.metrolist.music.constants.GradientDirectionKey
import com.metrolist.music.constants.GradientEndColorDefault
import com.metrolist.music.constants.GradientEndColorKey
import com.metrolist.music.constants.GradientStartColorDefault
import com.metrolist.music.constants.GradientStartColorKey
import com.metrolist.music.constants.PureBlackKey
import com.metrolist.music.constants.PureBlackMiniPlayerKey
import com.metrolist.music.constants.SelectedThemeColorKey
import com.metrolist.music.constants.UseCustomAccentDefault
import com.metrolist.music.constants.UseCustomAccentKey
import com.metrolist.music.constants.GlassCurvedRefractionDefault
import com.metrolist.music.constants.GlassCurvedRefractionKey
import com.metrolist.music.constants.GlassIntensityDefault
import com.metrolist.music.constants.GlassIntensityKey
import com.metrolist.music.constants.GlassStyle
import com.metrolist.music.constants.GlassStyleDefault
import com.metrolist.music.constants.GlassStyleKey
import com.metrolist.music.ui.component.ColorPickerDialog
import com.metrolist.music.ui.modifier.liquidGlass
import com.metrolist.music.ui.theme.DefaultThemeColor
import com.metrolist.music.ui.theme.MetrolistTheme
import com.metrolist.music.utils.rememberEnumPreference
import com.metrolist.music.utils.rememberPreference

data class ThemePalette(
    val nameRes: Int,
    val seedColor: Color
)

val PaletteColors = listOf(
    ThemePalette(R.string.palette_dynamic, Color.Transparent), // Sentinel for System/Dynamic colors
    ThemePalette(R.string.palette_crimson, Color(0xFFEC5464)), // Slightly shifted from DefaultThemeColor (0xFFED5564) to avoid conflict
    ThemePalette(R.string.palette_rose, Color(0xFFD81B60)),
    ThemePalette(R.string.palette_purple, Color(0xFF8E24AA)),
    ThemePalette(R.string.palette_deep_purple, Color(0xFF5E35B1)),
    ThemePalette(R.string.palette_indigo, Color(0xFF3949AB)),
    ThemePalette(R.string.palette_blue, Color(0xFF1E88E5)),
    ThemePalette(R.string.palette_sky_blue, Color(0xFF039BE5)),
    ThemePalette(R.string.palette_cyan, Color(0xFF00ACC1)),
    ThemePalette(R.string.palette_teal, Color(0xFF00897B)),
    ThemePalette(R.string.palette_green, Color(0xFF43A047)),
    ThemePalette(R.string.palette_light_green, Color(0xFF7CB342)),
    ThemePalette(R.string.palette_lime, Color(0xFFC0CA33)),
    ThemePalette(R.string.palette_yellow, Color(0xFFFDD835)),
    ThemePalette(R.string.palette_amber, Color(0xFFFFB300)),
    ThemePalette(R.string.palette_orange, Color(0xFFFB8C00)),
    ThemePalette(R.string.palette_deep_orange, Color(0xFFF4511E)),
    ThemePalette(R.string.palette_brown, Color(0xFF6D4C41)),
    ThemePalette(R.string.palette_grey, Color(0xFF757575)),
    ThemePalette(R.string.palette_blue_grey, Color(0xFF546E7A)),
)

private enum class ActiveColorPicker {
    NONE,
    PRIMARY_COLOR,
    SECONDARY_COLOR,
    CUSTOM_BG,
    GRADIENT_START,
    GRADIENT_END,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    navController: NavController,
) {
    val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, DarkMode.AUTO)
    val (pureBlack, onPureBlackChangeRaw) = rememberPreference(PureBlackKey, defaultValue = false)
    val (_, onPureBlackMiniPlayerChange) = rememberPreference(
        PureBlackMiniPlayerKey,
        defaultValue = false
    )

    val onPureBlackChange: (Boolean) -> Unit = { enabled ->
        onPureBlackChangeRaw(enabled)
        onPureBlackMiniPlayerChange(enabled)
    }
    val (selectedThemeColorInt, onSelectedThemeColorChange) = rememberPreference(
        SelectedThemeColorKey,
        DefaultThemeColor.toArgb()
    )
    val (_, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = true)

    val (useCustomAccent, onUseCustomAccentChange) = rememberPreference(
        UseCustomAccentKey,
        defaultValue = UseCustomAccentDefault
    )
    val (customPrimaryColorInt, onCustomPrimaryColorChange) = rememberPreference(
        CustomPrimaryColorKey,
        defaultValue = CustomPrimaryColorDefault
    )
    val (customSecondaryColorInt, onCustomSecondaryColorChange) = rememberPreference(
        CustomSecondaryColorKey,
        defaultValue = CustomSecondaryColorDefault
    )
    val (backgroundStyle, onBackgroundStyleChange) = rememberEnumPreference(
        BackgroundStyleKey,
        defaultValue = BackgroundStyleDefault
    )
    val (customBgColorInt, onCustomBgColorChange) = rememberPreference(
        CustomBgColorKey,
        defaultValue = CustomBgColorDefault
    )
    val (gradientStartColorInt, onGradientStartColorChange) = rememberPreference(
        GradientStartColorKey,
        defaultValue = GradientStartColorDefault
    )
    val (gradientEndColorInt, onGradientEndColorChange) = rememberPreference(
        GradientEndColorKey,
        defaultValue = GradientEndColorDefault
    )
    val (gradientDirection, onGradientDirectionChange) = rememberEnumPreference(
        GradientDirectionKey,
        defaultValue = GradientDirectionDefault
    )

    val (glassStyle) = rememberEnumPreference(GlassStyleKey, defaultValue = GlassStyleDefault)
    val (glassIntensity) = rememberPreference(GlassIntensityKey, defaultValue = GlassIntensityDefault)
    val (glassCurvedRefraction) = rememberPreference(GlassCurvedRefractionKey, defaultValue = GlassCurvedRefractionDefault)

    val selectedThemeColor = Color(selectedThemeColorInt)
    val customPrimaryColor = Color(customPrimaryColorInt)
    val customSecondaryColor = Color(customSecondaryColorInt)
    val customBgColor = Color(customBgColorInt)
    val gradientStartColor = Color(gradientStartColorInt)
    val gradientEndColor = Color(gradientEndColorInt)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Helper function to handle color selection with dynamic theme toggle
    val handleColorSelection: (Color) -> Unit = { color ->
        onUseCustomAccentChange(false)
        onSelectedThemeColorChange(color.toArgb())
        val isDynamicColor = color == DefaultThemeColor
        onDynamicThemeChange(isDynamicColor)
    }

    val handleCustomAccentToggle: (Boolean) -> Unit = { enabled ->
        onUseCustomAccentChange(enabled)
        if (enabled) {
            onDynamicThemeChange(false)
        }
    }

    if (isLandscape) {
        LandscapeThemeLayout(
            innerPadding = PaddingValues(0.dp),
            darkMode = darkMode,
            onDarkModeChange = onDarkModeChange,
            pureBlack = pureBlack,
            onPureBlackChange = onPureBlackChange,
            selectedThemeColor = selectedThemeColor,
            onSelectedThemeColorChange = handleColorSelection,
            useCustomAccent = useCustomAccent,
            onUseCustomAccentChange = handleCustomAccentToggle,
            customPrimaryColor = customPrimaryColor,
            onCustomPrimaryColorChange = { onCustomPrimaryColorChange(it.toArgb()) },
            customSecondaryColor = customSecondaryColor,
            onCustomSecondaryColorChange = { onCustomSecondaryColorChange(it.toArgb()) },
            backgroundStyle = backgroundStyle,
            onBackgroundStyleChange = onBackgroundStyleChange,
            customBgColor = customBgColor,
            onCustomBgColorChange = { onCustomBgColorChange(it.toArgb()) },
            gradientStartColor = gradientStartColor,
            onGradientStartColorChange = { onGradientStartColorChange(it.toArgb()) },
            gradientEndColor = gradientEndColor,
            onGradientEndColorChange = { onGradientEndColorChange(it.toArgb()) },
            gradientDirection = gradientDirection,
            onGradientDirectionChange = onGradientDirectionChange,
            onNavigateToCatalog = { navController.navigate("settings/appearance/theme_catalog") },
            glassStyle = glassStyle,
            glassIntensity = glassIntensity,
            isCurvedBevel = glassCurvedRefraction,
        )
    } else {
        PortraitThemeLayout(
            innerPadding = PaddingValues(0.dp),
            darkMode = darkMode,
            onDarkModeChange = onDarkModeChange,
            pureBlack = pureBlack,
            onPureBlackChange = onPureBlackChange,
            selectedThemeColor = selectedThemeColor,
            onSelectedThemeColorChange = handleColorSelection,
            useCustomAccent = useCustomAccent,
            onUseCustomAccentChange = handleCustomAccentToggle,
            customPrimaryColor = customPrimaryColor,
            onCustomPrimaryColorChange = { onCustomPrimaryColorChange(it.toArgb()) },
            customSecondaryColor = customSecondaryColor,
            onCustomSecondaryColorChange = { onCustomSecondaryColorChange(it.toArgb()) },
            backgroundStyle = backgroundStyle,
            onBackgroundStyleChange = onBackgroundStyleChange,
            customBgColor = customBgColor,
            onCustomBgColorChange = { onCustomBgColorChange(it.toArgb()) },
            gradientStartColor = gradientStartColor,
            onGradientStartColorChange = { onGradientStartColorChange(it.toArgb()) },
            gradientEndColor = gradientEndColor,
            onGradientEndColorChange = { onGradientEndColorChange(it.toArgb()) },
            gradientDirection = gradientDirection,
            onGradientDirectionChange = onGradientDirectionChange,
            onNavigateToCatalog = { navController.navigate("settings/appearance/theme_catalog") },
            glassStyle = glassStyle,
            glassIntensity = glassIntensity,
            isCurvedBevel = glassCurvedRefraction,
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.theme_colors)) },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.cd_back)
                )
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate("settings/appearance/theme_catalog") }) {
                Icon(
                    painter = painterResource(R.drawable.grid_view),
                    contentDescription = stringResource(R.string.theme_gallery),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    )
}

@Composable
fun PortraitThemeLayout(
    innerPadding: PaddingValues,
    darkMode: DarkMode,
    onDarkModeChange: (DarkMode) -> Unit,
    pureBlack: Boolean,
    onPureBlackChange: (Boolean) -> Unit,
    selectedThemeColor: Color,
    onSelectedThemeColorChange: (Color) -> Unit,
    useCustomAccent: Boolean,
    onUseCustomAccentChange: (Boolean) -> Unit,
    customPrimaryColor: Color,
    onCustomPrimaryColorChange: (Color) -> Unit,
    customSecondaryColor: Color,
    onCustomSecondaryColorChange: (Color) -> Unit,
    backgroundStyle: BackgroundStyle,
    onBackgroundStyleChange: (BackgroundStyle) -> Unit,
    customBgColor: Color,
    onCustomBgColorChange: (Color) -> Unit,
    gradientStartColor: Color,
    onGradientStartColorChange: (Color) -> Unit,
    gradientEndColor: Color,
    onGradientEndColorChange: (Color) -> Unit,
    gradientDirection: GradientDirection,
    onGradientDirectionChange: (GradientDirection) -> Unit,
    onNavigateToCatalog: () -> Unit = {},
    glassStyle: GlassStyle = GlassStyle.NONE,
    glassIntensity: Float = 0.5f,
    isCurvedBevel: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            ThemeMockupPortrait(
                darkMode = darkMode,
                pureBlack = pureBlack,
                themeColor = selectedThemeColor,
                useCustomAccent = useCustomAccent,
                customPrimaryColor = customPrimaryColor,
                customSecondaryColor = customSecondaryColor,
                backgroundStyle = backgroundStyle,
                customBgColor = customBgColor,
                gradientStartColor = gradientStartColor,
                gradientEndColor = gradientEndColor,
                gradientDirection = gradientDirection,
                glassStyle = glassStyle,
                glassIntensity = glassIntensity,
                isCurvedBevel = isCurvedBevel,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Theme Gallery banner button
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onNavigateToCatalog() },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.palette),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.theme_gallery_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = stringResource(R.string.theme_gallery_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                }
                Icon(
                    painter = painterResource(R.drawable.arrow_forward),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        ThemeControls(
            darkMode = darkMode,
            onDarkModeChange = onDarkModeChange,
            pureBlack = pureBlack,
            onPureBlackChange = onPureBlackChange,
            selectedThemeColor = selectedThemeColor,
            onSelectedThemeColorChange = onSelectedThemeColorChange,
            useCustomAccent = useCustomAccent,
            onUseCustomAccentChange = onUseCustomAccentChange,
            customPrimaryColor = customPrimaryColor,
            onCustomPrimaryColorChange = onCustomPrimaryColorChange,
            customSecondaryColor = customSecondaryColor,
            onCustomSecondaryColorChange = onCustomSecondaryColorChange,
            backgroundStyle = backgroundStyle,
            onBackgroundStyleChange = onBackgroundStyleChange,
            customBgColor = customBgColor,
            onCustomBgColorChange = onCustomBgColorChange,
            gradientStartColor = gradientStartColor,
            onGradientStartColorChange = onGradientStartColorChange,
            gradientEndColor = gradientEndColor,
            onGradientEndColorChange = onGradientEndColorChange,
            gradientDirection = gradientDirection,
            onGradientDirectionChange = onGradientDirectionChange,
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun LandscapeThemeLayout(
    innerPadding: PaddingValues,
    darkMode: DarkMode,
    onDarkModeChange: (DarkMode) -> Unit,
    pureBlack: Boolean,
    onPureBlackChange: (Boolean) -> Unit,
    selectedThemeColor: Color,
    onSelectedThemeColorChange: (Color) -> Unit,
    useCustomAccent: Boolean,
    onUseCustomAccentChange: (Boolean) -> Unit,
    customPrimaryColor: Color,
    onCustomPrimaryColorChange: (Color) -> Unit,
    customSecondaryColor: Color,
    onCustomSecondaryColorChange: (Color) -> Unit,
    backgroundStyle: BackgroundStyle,
    onBackgroundStyleChange: (BackgroundStyle) -> Unit,
    customBgColor: Color,
    onCustomBgColorChange: (Color) -> Unit,
    gradientStartColor: Color,
    onGradientStartColorChange: (Color) -> Unit,
    gradientEndColor: Color,
    onGradientEndColorChange: (Color) -> Unit,
    gradientDirection: GradientDirection,
    onGradientDirectionChange: (GradientDirection) -> Unit,
    onNavigateToCatalog: () -> Unit = {},
    glassStyle: GlassStyle = GlassStyle.NONE,
    glassIntensity: Float = 0.5f,
    isCurvedBevel: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .heightIn(max = 300.dp),
                contentAlignment = Alignment.Center
            ) {
                ThemeMockup(
                    darkMode = darkMode,
                    pureBlack = pureBlack,
                    themeColor = selectedThemeColor,
                    useCustomAccent = useCustomAccent,
                    customPrimaryColor = customPrimaryColor,
                    customSecondaryColor = customSecondaryColor,
                    backgroundStyle = backgroundStyle,
                    customBgColor = customBgColor,
                    gradientStartColor = gradientStartColor,
                    gradientEndColor = gradientEndColor,
                    gradientDirection = gradientDirection,
                    glassStyle = glassStyle,
                    glassIntensity = glassIntensity,
                    isCurvedBevel = isCurvedBevel,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            ThemeControls(
                darkMode = darkMode,
                onDarkModeChange = onDarkModeChange,
                pureBlack = pureBlack,
                onPureBlackChange = onPureBlackChange,
                selectedThemeColor = selectedThemeColor,
                onSelectedThemeColorChange = onSelectedThemeColorChange,
                useCustomAccent = useCustomAccent,
                onUseCustomAccentChange = onUseCustomAccentChange,
                customPrimaryColor = customPrimaryColor,
                onCustomPrimaryColorChange = onCustomPrimaryColorChange,
                customSecondaryColor = customSecondaryColor,
                onCustomSecondaryColorChange = onCustomSecondaryColorChange,
                backgroundStyle = backgroundStyle,
                onBackgroundStyleChange = onBackgroundStyleChange,
                customBgColor = customBgColor,
                onCustomBgColorChange = onCustomBgColorChange,
                gradientStartColor = gradientStartColor,
                onGradientStartColorChange = onGradientStartColorChange,
                gradientEndColor = gradientEndColor,
                onGradientEndColorChange = onGradientEndColorChange,
                gradientDirection = gradientDirection,
                onGradientDirectionChange = onGradientDirectionChange,
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ThemeControls(
    darkMode: DarkMode,
    onDarkModeChange: (DarkMode) -> Unit,
    pureBlack: Boolean,
    onPureBlackChange: (Boolean) -> Unit,
    selectedThemeColor: Color,
    onSelectedThemeColorChange: (Color) -> Unit,
    useCustomAccent: Boolean,
    onUseCustomAccentChange: (Boolean) -> Unit,
    customPrimaryColor: Color,
    onCustomPrimaryColorChange: (Color) -> Unit,
    customSecondaryColor: Color,
    onCustomSecondaryColorChange: (Color) -> Unit,
    backgroundStyle: BackgroundStyle,
    onBackgroundStyleChange: (BackgroundStyle) -> Unit,
    customBgColor: Color,
    onCustomBgColorChange: (Color) -> Unit,
    gradientStartColor: Color,
    onGradientStartColorChange: (Color) -> Unit,
    gradientEndColor: Color,
    onGradientEndColorChange: (Color) -> Unit,
    gradientDirection: GradientDirection,
    onGradientDirectionChange: (GradientDirection) -> Unit,
) {
    var activeColorPicker by remember { mutableStateOf(ActiveColorPicker.NONE) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Mode Selector
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.theme_mode),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // System mode (AUTO)
                    ModeCircle(
                        darkMode = darkMode,
                        pureBlack = pureBlack,
                        targetMode = DarkMode.AUTO,
                        targetPureBlack = pureBlack,
                        onClick = {
                            onDarkModeChange(DarkMode.AUTO)
                        },
                        showIcon = true
                    )
                    
                    // Vertical divider to separate System from manual modes
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    
                    // Manual modes (Light, Dark, Pure Black)
                    ModeCircle(
                        darkMode = darkMode,
                        pureBlack = pureBlack,
                        targetMode = DarkMode.OFF,
                        targetPureBlack = false,
                        onClick = {
                            onDarkModeChange(DarkMode.OFF)
                            onPureBlackChange(false)
                        },
                        showIcon = false
                    )
                    
                    ModeCircle(
                        darkMode = darkMode,
                        pureBlack = pureBlack,
                        targetMode = DarkMode.ON,
                        targetPureBlack = false,
                        onClick = {
                            onDarkModeChange(DarkMode.ON)
                            onPureBlackChange(false)
                        },
                        showIcon = false
                    )
                    
                    ModeCircle(
                        darkMode = darkMode,
                        pureBlack = pureBlack,
                        targetMode = DarkMode.ON,
                        targetPureBlack = true,
                        onClick = {
                            onDarkModeChange(DarkMode.ON)
                            onPureBlackChange(true)
                        },
                        showIcon = false
                    )
                }
            }

            // Color Palette Selector
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.color_palette),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    item {
                        CustomPaletteItem(
                            isSelected = useCustomAccent,
                            primaryColor = customPrimaryColor,
                            secondaryColor = customSecondaryColor,
                            onClick = {
                                onUseCustomAccentChange(true)
                            }
                        )
                    }

                    items(PaletteColors) { palette ->
                        val isDynamicPalette = palette.seedColor == Color.Transparent
                        val isSelected = !useCustomAccent && if (isDynamicPalette) {
                            selectedThemeColor == DefaultThemeColor
                        } else {
                            selectedThemeColor == palette.seedColor
                        }
                        
                        PaletteItem(
                            palette = palette,
                            isSelected = isSelected,
                            onClick = { 
                                val colorToSave = if (isDynamicPalette) DefaultThemeColor else palette.seedColor
                                onSelectedThemeColorChange(colorToSave) 
                            }
                        )
                    }
                }

                // Custom Primary & Secondary Color Swatch Pills
                AnimatedVisibility(
                    visible = useCustomAccent,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ColorSwatchPill(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.primary_color),
                            color = customPrimaryColor,
                            onClick = { activeColorPicker = ActiveColorPicker.PRIMARY_COLOR }
                        )

                        ColorSwatchPill(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.secondary_color),
                            color = customSecondaryColor,
                            onClick = { activeColorPicker = ActiveColorPicker.SECONDARY_COLOR }
                        )
                    }
                }
            }

            // Background Style Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.background_style),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = backgroundStyle == BackgroundStyle.DEFAULT,
                        onClick = { onBackgroundStyleChange(BackgroundStyle.DEFAULT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        label = {
                            Text(
                                text = stringResource(R.string.background_default),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                    SegmentedButton(
                        selected = backgroundStyle == BackgroundStyle.SOLID_CUSTOM,
                        onClick = { onBackgroundStyleChange(BackgroundStyle.SOLID_CUSTOM) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        label = {
                            Text(
                                text = stringResource(R.string.background_solid),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                    SegmentedButton(
                        selected = backgroundStyle == BackgroundStyle.GRADIENT,
                        onClick = { onBackgroundStyleChange(BackgroundStyle.GRADIENT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        label = {
                            Text(
                                text = stringResource(R.string.background_gradient),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }

                // Solid Color picker pill
                AnimatedVisibility(
                    visible = backgroundStyle == BackgroundStyle.SOLID_CUSTOM,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    ColorSwatchPill(
                        label = stringResource(R.string.background_color),
                        color = customBgColor,
                        onClick = { activeColorPicker = ActiveColorPicker.CUSTOM_BG },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Gradient Controls (Start/End Swatches & Direction Selector)
                AnimatedVisibility(
                    visible = backgroundStyle == BackgroundStyle.GRADIENT,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ColorSwatchPill(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.gradient_start_color),
                                color = gradientStartColor,
                                onClick = { activeColorPicker = ActiveColorPicker.GRADIENT_START }
                            )

                            ColorSwatchPill(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.gradient_end_color),
                                color = gradientEndColor,
                                onClick = { activeColorPicker = ActiveColorPicker.GRADIENT_END }
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.gradient_direction),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SegmentedButton(
                                    selected = gradientDirection == GradientDirection.TOP_TO_BOTTOM,
                                    onClick = { onGradientDirectionChange(GradientDirection.TOP_TO_BOTTOM) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                    label = {
                                        Text(
                                            text = stringResource(R.string.gradient_vertical),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                                SegmentedButton(
                                    selected = gradientDirection == GradientDirection.DIAGONAL,
                                    onClick = { onGradientDirectionChange(GradientDirection.DIAGONAL) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                    label = {
                                        Text(
                                            text = stringResource(R.string.gradient_diagonal),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                                SegmentedButton(
                                    selected = gradientDirection == GradientDirection.RADIAL,
                                    onClick = { onGradientDirectionChange(GradientDirection.RADIAL) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                    label = {
                                        Text(
                                            text = stringResource(R.string.gradient_radial),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Active Color Picker Dialogs
    when (activeColorPicker) {
        ActiveColorPicker.PRIMARY_COLOR -> {
            ColorPickerDialog(
                initialColor = customPrimaryColor,
                title = stringResource(R.string.primary_color),
                onDismiss = { activeColorPicker = ActiveColorPicker.NONE },
                onColorSelected = {
                    onCustomPrimaryColorChange(it)
                    activeColorPicker = ActiveColorPicker.NONE
                }
            )
        }
        ActiveColorPicker.SECONDARY_COLOR -> {
            ColorPickerDialog(
                initialColor = customSecondaryColor,
                title = stringResource(R.string.secondary_color),
                onDismiss = { activeColorPicker = ActiveColorPicker.NONE },
                onColorSelected = {
                    onCustomSecondaryColorChange(it)
                    activeColorPicker = ActiveColorPicker.NONE
                }
            )
        }
        ActiveColorPicker.CUSTOM_BG -> {
            ColorPickerDialog(
                initialColor = customBgColor,
                title = stringResource(R.string.background_color),
                onDismiss = { activeColorPicker = ActiveColorPicker.NONE },
                onColorSelected = {
                    onCustomBgColorChange(it)
                    activeColorPicker = ActiveColorPicker.NONE
                }
            )
        }
        ActiveColorPicker.GRADIENT_START -> {
            ColorPickerDialog(
                initialColor = gradientStartColor,
                title = stringResource(R.string.gradient_start_color),
                onDismiss = { activeColorPicker = ActiveColorPicker.NONE },
                onColorSelected = {
                    onGradientStartColorChange(it)
                    activeColorPicker = ActiveColorPicker.NONE
                }
            )
        }
        ActiveColorPicker.GRADIENT_END -> {
            ColorPickerDialog(
                initialColor = gradientEndColor,
                title = stringResource(R.string.gradient_end_color),
                onDismiss = { activeColorPicker = ActiveColorPicker.NONE },
                onColorSelected = {
                    onGradientEndColorChange(it)
                    activeColorPicker = ActiveColorPicker.NONE
                }
            )
        }
        ActiveColorPicker.NONE -> Unit
    }
}

@Composable
fun ColorSwatchPill(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color, CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = String.format("#%06X", 0xFFFFFF and color.toArgb()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ModeCircle(
    darkMode: DarkMode,
    pureBlack: Boolean,
    targetMode: DarkMode,
    targetPureBlack: Boolean,
    showIcon: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    val isSelected = darkMode == targetMode && pureBlack == targetPureBlack
    
    val effectiveDark = when (targetMode) {
        DarkMode.AUTO -> isSystemDark
        DarkMode.ON -> true
        DarkMode.OFF -> false
    }
    
    // Use actual system colors for AUTO mode on Android 12+
    val modeColorScheme = if (targetMode == DarkMode.AUTO && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (effectiveDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        rememberDynamicColorScheme(
            seedColor = DefaultThemeColor,
            isDark = effectiveDark,
            style = PaletteStyle.TonalSpot
        )
    }
    
    val fillColor = when {
        targetPureBlack -> Color.Black
        effectiveDark -> modeColorScheme.surface
        else -> modeColorScheme.surface
    }
    
    // Animated border width
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "borderWidth"
    )
    
    // Animated scale for the entire circle
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    
    val contentDesc = when {
        targetPureBlack -> stringResource(R.string.cd_pure_black_mode)
        targetMode == DarkMode.OFF -> stringResource(R.string.cd_light_mode)
        targetMode == DarkMode.ON -> stringResource(R.string.cd_dark_mode)
        else -> stringResource(R.string.cd_system_mode)
    }
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(fillColor)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(
                        width = borderWidth,
                        color = MaterialTheme.colorScheme.inversePrimary,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .semantics {
                contentDescription = contentDesc
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            showIcon -> {
                Icon(
                    painter = painterResource(R.drawable.sync),
                    contentDescription = null,
                    tint = modeColorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            isSelected -> {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                        initialScale = 0.3f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + scaleOut(
                        targetScale = 0.3f,
                        animationSpec = tween(150)
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inversePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomPaletteItem(
    isSelected: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 48.dp * 0.25f else 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "customCornerRadius"
    )
    
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "customBorderWidth"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "customScale"
    )
    
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val contentDesc = stringResource(R.string.custom_color_palette)
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(
                        width = borderWidth,
                        color = MaterialTheme.colorScheme.inversePrimary,
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .semantics {
                contentDescription = contentDesc
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            drawRect(
                color = primaryColor,
                topLeft = Offset(0f, 0f),
                size = Size(width, height / 2)
            )
            drawRect(
                color = secondaryColor,
                topLeft = Offset(0f, height / 2),
                size = Size(width, height / 2)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.palette),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PaletteItem(
    palette: ThemePalette,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    
    val colorScheme = rememberDynamicColorScheme(
        seedColor = palette.seedColor,
        isDark = isSystemDark,
        style = PaletteStyle.TonalSpot
    )
    
    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 48.dp * 0.25f else 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cornerRadius"
    )
    
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "borderWidth"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    
    val paletteName = stringResource(palette.nameRes)
    val contentDesc = stringResource(R.string.cd_palette_item, paletteName)
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(
                        width = borderWidth,
                        color = MaterialTheme.colorScheme.inversePrimary,
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .semantics {
                contentDescription = contentDesc
            }
    ) {
        if (palette.seedColor == Color.Transparent) {
            // Draw Dynamic/System icon using Material Design icon
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.palette),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                drawRect(
                    color = colorScheme.onPrimary,
                    topLeft = Offset(0f, 0f),
                    size = Size(width, height / 2)
                )
                
                drawRect(
                    color = colorScheme.secondary,
                    topLeft = Offset(0f, height / 2),
                    size = Size(width / 2, height / 2)
                )
                
                drawRect(
                    color = colorScheme.tertiary,
                    topLeft = Offset(width / 2, height / 2),
                    size = Size(width / 2, height / 2)
                )
            }
        }
    }
}

private fun Modifier.mockupBackground(
    backgroundStyle: BackgroundStyle,
    customBgColor: Color,
    gradientStartColor: Color,
    gradientEndColor: Color,
    gradientDirection: GradientDirection,
    defaultColor: Color
): Modifier {
    return when (backgroundStyle) {
        BackgroundStyle.DEFAULT -> this.background(defaultColor)
        BackgroundStyle.SOLID_CUSTOM -> this.background(customBgColor)
        BackgroundStyle.GRADIENT -> {
            val brush = when (gradientDirection) {
                GradientDirection.TOP_TO_BOTTOM -> Brush.verticalGradient(
                    colors = listOf(gradientStartColor, gradientEndColor)
                )
                GradientDirection.DIAGONAL -> Brush.linearGradient(
                    colors = listOf(gradientStartColor, gradientEndColor),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
                GradientDirection.RADIAL -> Brush.radialGradient(
                    colors = listOf(gradientStartColor, gradientEndColor)
                )
            }
            this.background(brush)
        }
    }
}

@Composable
fun ThemeMockup(
    darkMode: DarkMode,
    pureBlack: Boolean,
    themeColor: Color,
    useCustomAccent: Boolean,
    customPrimaryColor: Color,
    customSecondaryColor: Color,
    backgroundStyle: BackgroundStyle,
    customBgColor: Color,
    gradientStartColor: Color,
    gradientEndColor: Color,
    gradientDirection: GradientDirection,
    glassStyle: GlassStyle = GlassStyle.NONE,
    glassIntensity: Float = 0.5f,
    isCurvedBevel: Boolean = true,
) {
    val isSystemDark = isSystemInDarkTheme()
    val useDark = when (darkMode) {
        DarkMode.AUTO -> isSystemDark
        DarkMode.ON -> true
        DarkMode.OFF -> false
    }

    val effectivePrimary = if (useCustomAccent) customPrimaryColor else null
    val effectiveSecondary = if (useCustomAccent) customSecondaryColor else null

    MetrolistTheme(
        darkTheme = useDark,
        pureBlack = pureBlack,
        themeColor = themeColor
    ) {
        val primary = effectivePrimary ?: MaterialTheme.colorScheme.primary
        val secondary = effectiveSecondary ?: MaterialTheme.colorScheme.secondary
        val tertiary = if (useCustomAccent) secondary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.tertiary
        val defaultBg = if (pureBlack && useDark) Color.Black else MaterialTheme.colorScheme.surface

        Card(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(9f / 18f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .mockupBackground(
                        backgroundStyle = backgroundStyle,
                        customBgColor = customBgColor,
                        gradientStartColor = gradientStartColor,
                        gradientEndColor = gradientEndColor,
                        gradientDirection = gradientDirection,
                        defaultColor = defaultBg
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                if (backgroundStyle == BackgroundStyle.DEFAULT) {
                                    MaterialTheme.colorScheme.surfaceContainer
                                } else {
                                    Color.Black.copy(alpha = 0.25f)
                                }
                            )
                            .padding(10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(primary, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(secondary, CircleShape)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(primary, RoundedCornerShape(6.dp))
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(secondary, RoundedCornerShape(6.dp))
                            )
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(tertiary, RoundedCornerShape(6.dp))
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(
                                    if (useCustomAccent) primary else MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeMockupPortrait(
    darkMode: DarkMode,
    pureBlack: Boolean,
    themeColor: Color,
    useCustomAccent: Boolean,
    customPrimaryColor: Color,
    customSecondaryColor: Color,
    backgroundStyle: BackgroundStyle,
    customBgColor: Color,
    gradientStartColor: Color,
    gradientEndColor: Color,
    gradientDirection: GradientDirection,
    glassStyle: GlassStyle = GlassStyle.NONE,
    glassIntensity: Float = 0.5f,
    isCurvedBevel: Boolean = true,
) {
    val isSystemDark = isSystemInDarkTheme()
    val useDark = when (darkMode) {
        DarkMode.AUTO -> isSystemDark
        DarkMode.ON -> true
        DarkMode.OFF -> false
    }

    val effectivePrimary = if (useCustomAccent) customPrimaryColor else null
    val effectiveSecondary = if (useCustomAccent) customSecondaryColor else null

    MetrolistTheme(
        darkTheme = useDark,
        pureBlack = pureBlack,
        themeColor = themeColor
    ) {
        val primary = effectivePrimary ?: MaterialTheme.colorScheme.primary
        val secondary = effectiveSecondary ?: MaterialTheme.colorScheme.secondary
        val tertiary = if (useCustomAccent) secondary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.tertiary
        val defaultBg = if (pureBlack && useDark) Color.Black else MaterialTheme.colorScheme.surface

        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .mockupBackground(
                        backgroundStyle = backgroundStyle,
                        customBgColor = customBgColor,
                        gradientStartColor = gradientStartColor,
                        gradientEndColor = gradientEndColor,
                        gradientDirection = gradientDirection,
                        defaultColor = defaultBg
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header (20% of height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.2f)
                            .background(
                                if (backgroundStyle == BackgroundStyle.DEFAULT) {
                                    MaterialTheme.colorScheme.surfaceContainer
                                } else {
                                    Color.Black.copy(alpha = 0.25f)
                                }
                            )
                            .padding(6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(primary, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(secondary, CircleShape)
                            )
                        }
                    }

                    // Main Content (52% of height)
                    Column(
                        modifier = Modifier
                            .weight(0.52f)
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(primary, RoundedCornerShape(4.dp))
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.2f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(secondary, RoundedCornerShape(4.dp))
                            )
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(tertiary, RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    // Floating Glass Surfaces: MiniPlayer Pill & Navigation Dock (28% of height)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.28f)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.Bottom),
                    ) {
                        // Floating Glass MiniPlayer Pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlass(
                                    style = glassStyle,
                                    intensity = glassIntensity,
                                    shape = RoundedCornerShape(6.dp),
                                    isCurvedBevel = isCurvedBevel,
                                )
                                .background(
                                    if (glassStyle == GlassStyle.NONE) {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    } else {
                                        Color.Transparent
                                    },
                                    RoundedCornerShape(6.dp),
                                )
                                .padding(horizontal = 5.dp, vertical = 3.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(primary, RoundedCornerShape(2.dp)),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(3.dp)
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), RoundedCornerShape(1.dp)),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(primary, CircleShape),
                                )
                            }
                        }

                        // Floating Glass Navigation Dock
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlass(
                                    style = glassStyle,
                                    intensity = glassIntensity,
                                    shape = RoundedCornerShape(8.dp),
                                    isCurvedBevel = isCurvedBevel,
                                )
                                .background(
                                    if (glassStyle == GlassStyle.NONE) {
                                        MaterialTheme.colorScheme.surfaceContainer
                                    } else {
                                        Color.Transparent
                                    },
                                    RoundedCornerShape(8.dp),
                                )
                                .padding(vertical = 3.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.75f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(modifier = Modifier.size(5.dp).background(primary, CircleShape))
                                Box(modifier = Modifier.size(5.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), CircleShape))
                                Box(modifier = Modifier.size(5.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }
}
