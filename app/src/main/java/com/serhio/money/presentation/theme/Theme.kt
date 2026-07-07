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

private val MoneyDarkColorScheme = ColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    secondary = Green500,
    onSecondary = Color.White,
    tertiary = Color(0xFF03DAC5),
    error = Red500,
    onError = Color.White,
    background = Color.Black,
    onBackground = Color.White
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
