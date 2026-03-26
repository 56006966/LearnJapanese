package com.example.learnjapanese.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFAF3E2D),
    onPrimary = Color.White,
    secondary = Color(0xFF1F5C5C),
    background = Color(0xFFF8F4EE),
    surface = Color(0xFFFFFBF6)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4A8),
    secondary = Color(0xFF93D4D2),
    background = Color(0xFF171311),
    surface = Color(0xFF221D1A)
)

@Composable
fun LearnJapaneseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
