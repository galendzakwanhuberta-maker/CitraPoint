package com.citra.point

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF1E3A8A)
val CitraRed = Color(0xFFC0392B)
val WaGreen = Color(0xFF22C55E)

private val Light = lightColorScheme(
    primary = Navy, onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF), onPrimaryContainer = Navy,
    background = Color.White, surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9), outlineVariant = Color(0xFFE2E8F0)
)
private val Dark = darkColorScheme(
    primary = Color(0xFF9DB4FF), onPrimary = Color(0xFF0B1B4D),
    primaryContainer = Navy, onPrimaryContainer = Color.White
)

@Composable
fun CitraTheme(dark: Boolean, content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
