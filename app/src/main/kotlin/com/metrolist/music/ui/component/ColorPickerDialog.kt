/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Reusable Material 3 Color Picker Dialog.
 *
 * Features:
 * - Live color preview box displaying the resolved color with contrasting text.
 * - HEX code text field with live validation supporting both 6-digit (#RRGGBB) and 8-digit (#AARRGGBB) formats.
 * - Three smooth sliders for Hue (0..360), Saturation (0..1), and Value/Brightness (0..1).
 * - Cancel and Apply action buttons.
 *
 * @param initialColor The initial color to populate the picker with.
 * @param title The dialog title text.
 * @param onDismiss Callback invoked when the dialog is dismissed without applying.
 * @param onColorSelected Callback invoked with the selected [Color] when applied.
 */
@Suppress("DEPRECATION")
@Composable
fun ColorPickerDialog(
    initialColor: Color,
    title: String,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    val initialHsv = remember(initialColor) {
        FloatArray(3).apply {
            android.graphics.Color.colorToHSV(initialColor.toArgb(), this)
        }
    }

    val startAlpha = if (initialColor.alpha == 0f) 1f else initialColor.alpha

    var hue by remember(initialColor) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(initialColor) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(initialColor) { mutableFloatStateOf(initialHsv[2]) }
    var alpha by remember(initialColor) { mutableFloatStateOf(startAlpha) }

    var hexInput by remember(initialColor) {
        mutableStateOf(formatColorToHex(initialColor, startAlpha < 1f))
    }

    val currentColor = remember(hue, saturation, value, alpha) {
        hsvToColor(hue, saturation, value, alpha)
    }

    val parsedColorFromHex = remember(hexInput) {
        parseHexColor(hexInput)
    }

    val cleanHex = hexInput.trim().removePrefix("#")
    val isHexIncomplete = cleanHex.length in 1..5 || cleanHex.length == 7
    val isHexError = hexInput.isNotEmpty() && hexInput != "#" && (parsedColorFromHex == null && !isHexIncomplete || cleanHex.length > 8)

    DefaultDialog(
        onDismiss = onDismiss,
        modifier = modifier.widthIn(max = 420.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
            TextButton(
                enabled = parsedColorFromHex != null || !isHexError,
                onClick = {
                    val finalColor = parsedColorFromHex ?: currentColor
                    onColorSelected(finalColor)
                    onDismiss()
                },
            ) {
                Text(text = "Apply")
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Live Color Preview Box
            val previewColor = parsedColorFromHex ?: currentColor
            val luminance = (0.299f * previewColor.red + 0.587f * previewColor.green + 0.114f * previewColor.blue)
            val textColor = if (luminance > 0.5f) Color.Black else Color.White

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(previewColor, shape = RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = formatColorToHex(previewColor, alpha < 1f),
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // 2. HEX Code OutlinedTextField with Live Validation
            OutlinedTextField(
                value = hexInput,
                onValueChange = { input ->
                    val filtered = input.filter { it == '#' || it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
                    if (filtered.length <= 9) {
                        hexInput = filtered
                        val parsed = parseHexColor(filtered)
                        if (parsed != null) {
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(parsed.toArgb(), hsv)
                            hue = hsv[0]
                            saturation = hsv[1]
                            value = hsv[2]
                            alpha = parsed.alpha
                        }
                    }
                },
                label = { Text("HEX Code") },
                placeholder = { Text("#RRGGBB or #AARRGGBB") },
                isError = isHexError,
                supportingText = if (isHexError) {
                    { Text("Enter a valid 6 or 8-digit HEX code (e.g. #RRGGBB)") }
                } else null,
                singleLine = true,
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(previewColor, shape = CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape,
                            ),
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // 3. Hue Slider (0f..360f)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Hue",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "${hue.roundToInt()}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Slider(
                    value = hue,
                    onValueChange = { newHue ->
                        hue = newHue
                        val newColor = hsvToColor(newHue, saturation, value, alpha)
                        hexInput = formatColorToHex(newColor, alpha < 1f)
                    },
                    valueRange = 0f..360f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 4. Saturation Slider (0f..1f)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Saturation",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "${(saturation * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Slider(
                    value = saturation,
                    onValueChange = { newSaturation ->
                        saturation = newSaturation
                        val newColor = hsvToColor(hue, newSaturation, value, alpha)
                        hexInput = formatColorToHex(newColor, alpha < 1f)
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 5. Value/Brightness Slider (0f..1f)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Brightness",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "${(value * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Slider(
                    value = value,
                    onValueChange = { newValue ->
                        value = newValue
                        val newColor = hsvToColor(hue, saturation, newValue, alpha)
                        hexInput = formatColorToHex(newColor, alpha < 1f)
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Converts HSV components to a Jetpack Compose [Color].
 */
private fun hsvToColor(hue: Float, saturation: Float, value: Float, alpha: Float = 1f): Color {
    val hsv = floatArrayOf(
        hue.coerceIn(0f, 360f),
        saturation.coerceIn(0f, 1f),
        value.coerceIn(0f, 1f),
    )
    val alphaInt = (alpha.coerceIn(0f, 1f) * 255).roundToInt()
    val argb = android.graphics.Color.HSVToColor(alphaInt, hsv)
    return Color(argb)
}

/**
 * Formats a [Color] into a HEX representation string (#RRGGBB or #AARRGGBB).
 */
private fun formatColorToHex(color: Color, includeAlpha: Boolean = false): String {
    val argb = color.toArgb()
    return if (includeAlpha) {
        String.format("#%08X", argb)
    } else {
        String.format("#%06X", 0xFFFFFF and argb)
    }
}

/**
 * Parses a 6-digit (#RRGGBB) or 8-digit (#AARRGGBB) hex string into a [Color].
 * Returns null if the format is invalid or malformed.
 */
private fun parseHexColor(input: String): Color? {
    val clean = input.trim().removePrefix("#")
    if (clean.length != 6 && clean.length != 8) return null
    return try {
        val parsed = clean.toLong(16)
        if (clean.length == 6) {
            Color((0xFF000000L or parsed).toInt())
        } else {
            Color(parsed.toInt())
        }
    } catch (_: Exception) {
        null
    }
}
