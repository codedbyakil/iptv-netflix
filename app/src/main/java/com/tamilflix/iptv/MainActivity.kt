package com.tamilflix.iptv
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.tamilflix.iptv.data.M3uParser
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.settings.AppSettings
import com.tamilflix.iptv.ui.phone.HomeScreen
import com.tamilflix.iptv.ui.phone.PlayerScreen
import com.tamilflix.iptv.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.first
sealed class Screen {
    object Home : Screen()
    object Settings : Screen()
    data class Player(val channel: Channel) : Screen()
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var darkTheme by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                channels = M3uParser.fetchChannels()
                darkTheme = AppSettings.darkModeFlow.first()
            }
            LaunchedEffect(darkTheme) { AppSettings.setDarkMode(this@MainActivity, darkTheme) }
            when (val screen = currentScreen) {
                is Screen.Home -> HomeScreen(channels = channels, darkTheme = darkTheme, onChannelClick = { currentScreen = Screen.Player(it) }, onSettingsClick = { currentScreen = Screen.Settings })
                is Screen.Player -> PlayerScreen(channel = screen.channel, onBack = { currentScreen = Screen.Home })
                is Screen.Settings -> SettingsScreen(darkTheme = darkTheme, onThemeToggle = { darkTheme = !darkTheme }, onBack = { currentScreen = Screen.Home })
            }
        }
    }
}
