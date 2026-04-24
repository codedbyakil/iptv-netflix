package com.tamilflix.iptv.ui.theme
import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
val NetflixDark = darkColorScheme(primary = Color(0xFFE50914), secondary = Color(0xFFB30000), background = Color(0xFF141414), surface = Color(0xFF1F1F1F), onPrimary = Color.White, onBackground = Color.White, onSurface = Color.White)
val NetflixLight = lightColorScheme(primary = Color(0xFFE50914), secondary = Color(0xFFB30000), background = Color(0xFFFAFAFA), surface = Color(0xFFFFFFFF), onPrimary = Color.White, onBackground = Color(0xFF141414), onSurface = Color(0xFF141414))
object TiviMateColors { val focusBorder = Color(0xFFE50914); val channelBadge = Color(0xFF333333); val epgText = Color(0xFF757575); val playingIndicator = Color(0xFF00C853) }
@Composable fun TamilFlixTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) NetflixDark else NetflixLight
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
