package com.safepath.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = PurplePrimary,
    onPrimary        = Color.White,
    primaryContainer = PurpleDark,
    secondary        = PinkAccent,
    onSecondary      = Color.White,
    background       = BackgroundDark,
    onBackground     = TextPrimary,
    surface          = SurfaceDark,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline          = DividerColor,
    error            = RiskyRed,
    onError          = Color.White
)

@Composable
fun SafePathTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = SafePathTypography,
        content     = content
    )
}
