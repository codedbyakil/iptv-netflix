package com.tamilflix.iptv.ui.theme
import android.app.Activity
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Public colors for UI access
val NetflixDark = darkColorScheme(
    primary = Color(0xFFE50914),        // Netflix red
    secondary = Color(0xFFB30000),      // Darker red for accents
    background = Color(0xFF141414),     // Netflix black
    surface = Color(0xFF1F1F1F),        // Card background
    error = Color(0xFFCF6679),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

// TiviMate-style accent colors
val TiviMateColors = mapOf(
    "focusBorder" to Color(0xFFE50914),
    "channelBadge" to Color(0xFF333333),
    "epgText" to Color(0xFFB3B3B3),
    "playingIndicator" to Color(0xFF00C853)
)

@Composable
fun TamilFlixTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = NetflixDark.background.toArgb()
        window.navigationBarColor = NetflixDark.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
    MaterialTheme(colorScheme = NetflixDark, content = content)
}
