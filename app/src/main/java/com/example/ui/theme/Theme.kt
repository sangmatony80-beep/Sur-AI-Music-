package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun SurMusicTheme(
    themeMode: String = "dark", // "dark", "light", "amoled", "system"
    themeColorPreset: ThemeColorPreset = ThemeColorPreset.NeonPurple,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark", "amoled" -> true
        "light" -> false
        else -> systemDark
    }

    val primaryColor = themeColorPreset.primary
    val secondaryColor = themeColorPreset.secondary

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == "amoled" -> darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = SurTertiaryDark,
            background = SurAmoledBlack,
            surface = SurAmoledBlack,
            onBackground = Color.White,
            onSurface = Color.White,
            surfaceVariant = Color(0xFF0F0F0F),
            onSurfaceVariant = Color(0xFFB0B0B0)
        )
        isDark -> darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = SurTertiaryDark,
            background = SurBackgroundDark,
            surface = SurSurfaceDark,
            onBackground = SurTextPrimary,
            onSurface = SurTextPrimary,
            surfaceVariant = SurSurfaceVariantDark,
            onSurfaceVariant = SurTextSecondary
        )
        else -> lightColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = SurTertiaryLight,
            background = SurBackgroundLight,
            surface = SurSurfaceLight,
            onBackground = Color(0xFF121212),
            onSurface = Color(0xFF121212),
            surfaceVariant = Color(0xFFEFEFEF),
            onSurfaceVariant = Color(0xFF49454F)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
