package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PayStoryPrimary,
    onPrimary = PayStoryOnPrimary,
    primaryContainer = PayStoryPrimaryContainer,
    onPrimaryContainer = PayStoryOnPrimaryContainer,
    secondary = PayStorySecondary,
    onSecondary = PayStoryOnSecondary,
    secondaryContainer = PayStorySecondaryContainer,
    onSecondaryContainer = PayStoryOnSecondaryContainer,
    tertiary = PayStoryTertiary,
    onTertiary = PayStoryOnTertiary,
    tertiaryContainer = PayStoryTertiaryContainer,
    onTertiaryContainer = PayStoryOnTertiaryContainer,
    background = PayStoryBackground,
    onBackground = PayStoryOnBackground,
    surface = PayStorySurface,
    onSurface = PayStoryOnSurface,
    surfaceVariant = PayStorySurfaceVariant,
    onSurfaceVariant = PayStoryOnSurfaceVariant,
    surfaceContainer = PayStorySurfaceVariant,
    surfaceContainerLow = PayStorySurface,
    surfaceContainerHigh = PayStorySurfaceContainerHigh,
    outline = PayStoryOutline,
    outlineVariant = PayStoryOutlineVariant,
    error = PayStoryError,
    onError = PayStoryOnError,
    errorContainer = PayStoryErrorContainer,
    onErrorContainer = PayStoryOnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C49),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF6FFBBE),
    onPrimaryContainer = Color(0xFF002113),
    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2FBEF),
    onSecondaryContainer = Color(0xFF003824),
    tertiary = Color(0xFF8A5100),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDDB8),
    onTertiaryContainer = Color(0xFF2A1700),
    background = Color(0xFFFBFDFA),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFF2F5F2),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDEE5E0),
    onSurfaceVariant = Color(0xFF414944),
    outline = Color(0xFF717973),
    outlineVariant = Color(0xFFC1C9C3),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
