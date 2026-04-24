package com.tamilflix.iptv.ui.theme
import android.app.Activity
import androidx.compose.material3.ExperimentalMaterial3Api
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF), secondary = Color(0xFFCCC2DC), tertiary = Color(0xFFEFB8C8),
    background = Color(0xFF1C1B1F), surface = Color(0xFF1C1B1F),
    onPrimary = Color(0xFF381E72), onSecondary = Color(0xFF332D41),
    onBackground = Color(0xFFE6E1E5), onSurface = Color(0xFFE6E1E5)
)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4), secondary = Color(0xFF625B71), tertiary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFE), surface = Color(0xFFFFFBFE),
    onPrimary = Color(0xFFFFFFFF), onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F), onSurface = Color(0xFF1C1B1F)
)

@Composable
fun TamilFlixTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = true, content: @Composable () -> Unit) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
