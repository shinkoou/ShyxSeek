package com.shyxseek.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Amoled = darkColorScheme(
    primary = Color(0xFF6C3BFF),
    secondary = Color(0xFF9A9A9A),
    background = Color.Black,
    surface = Color(0xFF0D0D0D),
    surfaceVariant = Color(0xFF141414),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFFF1744)
)

@Composable
fun ShyxSeekTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Amoled, typography = MaterialTheme.typography, content = content)
}
