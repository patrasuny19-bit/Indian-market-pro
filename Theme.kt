package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MarketGreen,
    onPrimary = TerminalBg,
    primaryContainer = MarketGreenGlow,
    onPrimaryContainer = MarketGreen,
    secondary = MarketGold,
    onSecondary = TerminalBg,
    secondaryContainer = TerminalBorder,
    onSecondaryContainer = MarketGold,
    tertiary = MarketCyan,
    onTertiary = TerminalBg,
    background = TerminalBg,
    onBackground = TextPrimary,
    surface = TerminalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TerminalCard,
    onSurfaceVariant = TextSecondary,
    outline = TerminalBorder,
    outlineVariant = TerminalBorderLight,
    error = MarketRed,
    onError = TextPrimary,
    errorContainer = MarketRedGlow,
    onErrorContainer = MarketRed
)

private val LightColorScheme = lightColorScheme(
    primary = MarketGreenDark,
    onPrimary = LightSurface,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = Color(0xFF14532D),
    secondary = Color(0xFFB45309),
    onSecondary = LightSurface,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFF0369A1),
    onTertiary = LightSurface,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = MarketRedDark,
    onError = LightSurface,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun IndiaMarketTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
