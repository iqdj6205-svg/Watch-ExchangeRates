package com.serhio.money.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val Blue500 = Color(0xFF2196F3)
val Blue700 = Color(0xFF1976D2)
val Blue900 = Color(0xFF0D47A1)
val Green500 = Color(0xFF4CAF50)
val Red500 = Color(0xFFF44336)
val DarkSurface = Color(0xFF1C1B1F)
val DarkGray = Color(0xFF2C2C2C)
val LightGray = Color(0xFFE0E0E0)

val CardBackground = Color(0xFF1A1A1A)
val CardBackgroundDragging = Color(0xFF2A2A2A)
val SubtextGray = Color(0xFFB0B0B0)

private val MoneyDarkColorScheme = ColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Blue700,
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Green500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFF03DAC5),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF004D40),
    onTertiaryContainer = Color(0xFFB2DFDB),
    error = Red500,
    onError = Color.White,
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color(0xFFFFCDD2),
    background = Color.Black,
    onBackground = Color.White,
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF616161)
)

@Composable
fun MoneyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MoneyDarkColorScheme,
        content = content
    )
}
