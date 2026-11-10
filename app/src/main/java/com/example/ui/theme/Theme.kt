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

private val DarkColorScheme = darkColorScheme(
    primary = IbmBlueLight,
    onPrimary = Color.White,
    primaryContainer = NavySurfaceVariant,
    onPrimaryContainer = CyanAccent,
    secondary = CyanAccent,
    onSecondary = NavyDark,
    secondaryContainer = NavyCardBorder,
    onSecondaryContainer = Color.White,
    tertiary = TealAccent,
    onTertiary = NavyDark,
    background = NavyDark,
    onBackground = TextPrimaryDark,
    surface = NavySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = NavyCardBorder,
    error = ScoreLowRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = IbmBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SlateLightSurfaceVariant,
    onPrimaryContainer = IbmBluePrimary,
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = SlateLightSurfaceVariant,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = CyanAccent,
    onTertiary = Color.White,
    background = SlateLightBackground,
    onBackground = TextPrimaryLight,
    surface = SlateLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = SlateLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = SlateLightBorder,
    error = ScoreLowRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek enterprise dark mode
    dynamicColor: Boolean = false, // Keep consistent branding for hackathon presentation
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
