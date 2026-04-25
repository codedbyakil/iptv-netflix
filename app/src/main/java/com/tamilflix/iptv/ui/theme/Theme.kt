package com.tamilflix.iptv.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// TV-optimized dark theme (high contrast for 10-foot UI)
val TvDarkColors = darkColorScheme(
    primary = Color(0xFFE50914),      // Netflix red - high visibility
    secondary = Color(0xFFB30000),
    background = Color(0xFF0A0A0A),   // Deeper black for OLED TVs
    surface = Color(0xFF1A1A1A),      // Card background
    onPrimary = Color.White,
    onBackground = Color(0xFFEEEEEE), // Slightly off-white for readability
    onSurface = Color(0xFFEEEEEE)
)

@Composable
fun TamilFlixTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TvDarkColors, content = content)
}
