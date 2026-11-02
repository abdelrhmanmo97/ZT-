package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val DarkBg = Color(0xFF0B1020)
val DarkSurface = Color(0xFF141A2E)
val DarkCardBorder = Color(0xFF232B47)
val DarkFriday = Color(0xFF1A2140)

val TextPrimary = Color(0xFFEEF0FF)
val TextMuted = Color(0xFF8B93B8)

val AccentViolet = Color(0xFF8B5CF6)
val AccentCyan = Color(0xFF22D3EE)
val AccentGreen = Color(0xFF10B981)
val AccentAmber = Color(0xFFF59E0B)
val AccentRose = Color(0xFFF43F5E)
val AccentBlue = Color(0xFF3B82F6)

val WarningOrange = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF10B981)

val LightBg = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE2E8F0)
val LightText = Color(0xFF0F172A)
val LightMuted = Color(0xFF64748B)

data class ThemePreset(
    val nameArabic: String,
    val primaryHex: String,
    val primary: Color,
    val secondary: Color
)

val THEME_PRESETS = listOf(
    ThemePreset("بنفسجي وسماوي", "#8B5CF6", AccentViolet, AccentCyan),
    ThemePreset("أزرق بحري", "#3B82F6", AccentBlue, Color(0xFF38BDF8)),
    ThemePreset("زمردي أخضر", "#10B981", AccentGreen, Color(0xFF34D399)),
    ThemePreset("ذهبي كهرماني", "#F59E0B", AccentAmber, Color(0xFFFBBF24)),
    ThemePreset("وردي ياقوتي", "#F43F5E", AccentRose, Color(0xFFFB7185))
)
