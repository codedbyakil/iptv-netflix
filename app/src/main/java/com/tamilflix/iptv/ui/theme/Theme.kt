package com.tamilflix.iptv.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TvDarkColors = darkColorScheme(
    primary = Color(0xFFE50914),
    background = Color(0xFF0A0A0A),
    surface = Color(0xFF1A1A1A),
    onPrimary = Color.White,
    onBackground = Color(0xFFEEEEEE),
    onSurface = Color(0xFFEEEEEE)
)

@Composable
fun TamilFlixTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TvDarkColors, content = content)
}
