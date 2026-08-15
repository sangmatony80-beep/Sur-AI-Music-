package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Sophisticated Dark Colors
val SurBackgroundDark = Color(0xFF050505)
val SurSurfaceDark = Color(0xFF121214)
val SurSurfaceVariantDark = Color(0xFF1A1A1C)
val SurBorderDark = Color(0xFF2A2A2C)
val SurTextPrimary = Color(0xFFE4E3E0)
val SurTextSecondary = Color(0xFF9CA3AF)

val SurPrimaryDark = Color(0xFF7C3AED) // Purple
val SurSecondaryDark = Color(0xFF3B82F6) // Blue
val SurTertiaryDark = Color(0xFFDB2777) // Pink
val SurAmoledBlack = Color(0xFF000000)

val SurPrimaryLight = Color(0xFF7B1FA2)
val SurSecondaryLight = Color(0xFF00B0FF)
val SurTertiaryLight = Color(0xFFC51162)
val SurBackgroundLight = Color(0xFFF8F9FA)
val SurSurfaceLight = Color(0xFFFFFFFF)

// Theme Presets
enum class ThemeColorPreset(val primary: Color, val secondary: Color, val nameStr: String) {
    NeonPurple(Color(0xFF7C3AED), Color(0xFF3B82F6), "Neon Purple"),
    CyberpunkPink(Color(0xFFDB2777), Color(0xFF3B82F6), "Cyberpunk Pink"),
    ElectricBlue(Color(0xFF3B82F6), Color(0xFF7C3AED), "Electric Blue"),
    EmeraldGreen(Color(0xFF00C853), Color(0xFFFFD600), "Emerald Green"),
    SunsetOrange(Color(0xFFFF6D00), Color(0xFF651FFF), "Sunset Orange"),
    NeonCyan(Color(0xFF00B8D4), Color(0xFFFFEA00), "Neon Cyan"),
    GoldenYellow(Color(0xFFFFAB00), Color(0xFF304FFE), "Golden Yellow"),
    RubyRed(Color(0xFFD50000), Color(0xFF00B0FF), "Ruby Red")
}

