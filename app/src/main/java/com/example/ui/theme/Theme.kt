package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun parseColorFromHex(hex: String, fallback: Color = AccentViolet): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(colorInt or 0xFF000000L)
        } else if (clean.length == 8) {
            Color(colorInt)
        } else fallback
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun SalaryTrackerTheme(
    accentColorHex: String = "#8B5CF6",
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val primaryColor = parseColorFromHex(accentColorHex, AccentViolet)
    val secondaryColor = AccentCyan

    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            secondary = secondaryColor,
            onSecondary = Color.Black,
            secondaryContainer = secondaryColor.copy(alpha = 0.25f),
            onSecondaryContainer = Color.White,
            background = DarkBg,
            onBackground = TextPrimary,
            surface = DarkSurface,
            onSurface = TextPrimary,
            surfaceVariant = DarkCardBorder,
            onSurfaceVariant = TextMuted,
            outline = DarkCardBorder
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = primaryColor,
            secondary = secondaryColor,
            onSecondary = Color.Black,
            secondaryContainer = secondaryColor.copy(alpha = 0.15f),
            onSecondaryContainer = Color.Black,
            background = LightBg,
            onBackground = LightText,
            surface = LightSurface,
            onSurface = LightText,
            surfaceVariant = LightBorder,
            onSurfaceVariant = LightMuted,
            outline = LightBorder
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
