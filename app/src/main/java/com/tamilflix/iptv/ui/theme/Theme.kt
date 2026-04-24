package com.tamilflix.iptv.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
val DarkColors = darkColorScheme(
    primary = Color(0xFFE50914),
    background = Color(0xFF141414),
    surface = Color(0xFF1F1F1F),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)
val LightColors = lightColorScheme(
    primary = Color(0xFFE50914),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF141414),
    onSurface = Color(0xFF141414)
)
@Composable
fun AppTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
