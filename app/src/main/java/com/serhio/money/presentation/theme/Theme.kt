package com.serhio.money.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import androidx.wear.compose.material3.Shapes

val MoneyGold = Color(0xFFFFD166)
val MoneyGoldDim = Color(0xFFC79A31)
val Emerald = Color(0xFF00D18F)
val EmeraldDim = Color(0xFF008C61)
val Crimson = Color(0xFFFF5C7A)
val Night = Color(0xFF05070D)
val Ink = Color(0xFF0B1020)
val CardTop = Color(0xFF1A2134)
val CardBottom = Color(0xFF0C111D)
val StrokeGold = Color(0x66FFD166)
val SubtextGray = Color(0xFFB7C0D8)

// Backwards-compatible aliases used by the rest of the app.
val Blue500 = MoneyGold
val Blue700 = MoneyGoldDim
val Blue900 = Color(0xFF7A5A12)
val Green500 = Emerald
val Red500 = Crimson
val DarkSurface = Ink

private val DarkColorScheme = ColorScheme(
    primary = MoneyGold,
    primaryDim = MoneyGoldDim,
    onPrimary = Color(0xFF171006),
    primaryContainer = Color(0xFF4B3710),
    onPrimaryContainer = Color(0xFFFFE6A3),
    secondary = Emerald,
    secondaryDim = EmeraldDim,
    onSecondary = Color(0xFF001F16),
    secondaryContainer = Color(0xFF063D2E),
    onSecondaryContainer = Color(0xFFA9F5D7),
    tertiary = Color(0xFF8EA7FF),
    tertiaryDim = Color(0xFF6177C8),
    onTertiary = Color(0xFF061037),
    tertiaryContainer = Color(0xFF17245A),
    onTertiaryContainer = Color(0xFFDCE3FF),
    error = Crimson,
    errorDim = Color(0xFFC73D57),
    onError = Color.White,
    errorContainer = Color(0xFF5C1424),
    onErrorContainer = Color(0xFFFFD8DF),
    background = Night,
    onBackground = Color(0xFFF4F0E6),
    surfaceContainerLow = Color(0xFF0A0F1C),
    surfaceContainer = Ink,
    surfaceContainerHigh = CardTop,
    onSurface = Color(0xFFF6F2E8),
    onSurfaceVariant = SubtextGray,
    outline = Color(0xFF37415C),
    outlineVariant = StrokeGold
)

val MoneyTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        letterSpacing = (-0.6).sp,
        fontFamily = FontFamily.Default
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 23.sp,
        letterSpacing = (-0.4).sp,
        fontFamily = FontFamily.Default
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
        fontFamily = FontFamily.Default
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        fontFamily = FontFamily.Default
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 0.2.sp,
        fontFamily = FontFamily.Default
    )
)

val MoneyShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun MoneyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MoneyTypography,
        shapes = MoneyShapes,
        content = content
    )
}
