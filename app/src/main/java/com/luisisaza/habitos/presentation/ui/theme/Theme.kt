package com.luisisaza.habitos.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PaletteOlive,
    onPrimary = PaletteWhite,
    primaryContainer = PaletteSage,
    onPrimaryContainer = PaletteOliveDark,
    secondary = PaletteCopper,
    onSecondary = PaletteWhite,
    secondaryContainer = PalettePeach,
    onSecondaryContainer = PaletteCharcoal,
    tertiary = PaletteSageDark,
    onTertiary = PaletteOliveDark,
    background = PaletteCream,
    onBackground = PaletteCharcoal,
    surface = PaletteWhite,
    onSurface = PaletteSoftCharcoal,
    surfaceVariant = PaletteSage,
    onSurfaceVariant = PaletteWarmGray,
    outline = PaletteOutline,
    outlineVariant = PaletteOutline,
    error = ErrorRed,
    onError = PaletteWhite,
    errorContainer = HabitBadContainer,
    onErrorContainer = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PaletteSageDark,
    onPrimary = PaletteOliveDark,
    primaryContainer = PaletteOlive,
    onPrimaryContainer = PaletteSage,
    secondary = PalettePeach,
    onSecondary = PaletteCharcoal,
    secondaryContainer = PaletteCopper,
    tertiary = PaletteSage,
    background = DarkBackground,
    onBackground = PaletteSage,
    surface = DarkSurface,
    onSurface = PaletteSage,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = PaletteOutline,
    outline = PaletteWarmGray,
    error = ErrorRed,
    onError = PaletteWhite
)

@Composable
fun HabitosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HabitosTypography,
        content = content
    )
}
