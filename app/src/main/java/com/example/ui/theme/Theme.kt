package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun KashifMobileTheme(
    content: @Composable () -> Unit
) {
    val palette = currentPaletteState.value
    val colorScheme: ColorScheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = Color.Black,
            primaryContainer = palette.container,
            onPrimaryContainer = palette.onContainer,
            secondary = palette.secondary,
            onSecondary = Color.Black,
            secondaryContainer = palette.surfaceVariant,
            onSecondaryContainer = palette.textPrimary,
            tertiary = palette.tertiary,
            onTertiary = Color.Black,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceVariant,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.border,
            outlineVariant = palette.border,
            error = DangerRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.container,
            onPrimaryContainer = palette.onContainer,
            secondary = palette.secondary,
            onSecondary = Color.White,
            secondaryContainer = palette.surfaceVariant,
            onSecondaryContainer = palette.textPrimary,
            tertiary = palette.tertiary,
            onTertiary = Color.White,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceVariant,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.border,
            outlineVariant = palette.border,
            error = DangerRed,
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
