package com.universalpiano.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.universalpiano.settings.AccentPalette

private data class AccentColors(
    val lightPrimary: Color,
    val lightContainer: Color,
    val darkPrimary: Color,
    val darkContainer: Color,
)

private fun AccentPalette.colors() = when (this) {
    AccentPalette.VIOLET -> AccentColors(
        lightPrimary = Color(0xFF5B3FD9),
        lightContainer = Color(0xFFE6DFFF),
        darkPrimary = Color(0xFFC8BCFF),
        darkContainer = Color(0xFF3F27B8),
    )
    AccentPalette.TEAL -> AccentColors(
        lightPrimary = Color(0xFF00796B),
        lightContainer = Color(0xFFB2EFE5),
        darkPrimary = Color(0xFF7FD8CA),
        darkContainer = Color(0xFF005047),
    )
    AccentPalette.AMBER -> AccentColors(
        lightPrimary = Color(0xFF9A5B00),
        lightContainer = Color(0xFFFFDDB0),
        darkPrimary = Color(0xFFFFB950),
        darkContainer = Color(0xFF6B3E00),
    )
}

internal fun lightScheme(accent: AccentPalette): ColorScheme {
    val c = accent.colors()
    return lightColorScheme(
        primary = c.lightPrimary,
        onPrimary = Color.White,
        primaryContainer = c.lightContainer,
        onPrimaryContainer = Color(0xFF1B1530),
        background = Color(0xFFFBF9FF),
        surface = Color(0xFFFBF9FF),
        surfaceVariant = Color(0xFFEDE9F5),
    )
}

internal fun darkScheme(accent: AccentPalette): ColorScheme {
    val c = accent.colors()
    return darkColorScheme(
        primary = c.darkPrimary,
        onPrimary = Color(0xFF1B1530),
        primaryContainer = c.darkContainer,
        onPrimaryContainer = Color(0xFFF1ECFF),
        background = Color(0xFF121016),
        surface = Color(0xFF121016),
        surfaceVariant = Color(0xFF2A2631),
    )
}
