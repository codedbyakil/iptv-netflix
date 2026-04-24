package com.tamilflix.iptv
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.tamilflix.iptv.data.M3uParser
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.phone.HomeScreen
import com.tamilflix.iptv.ui.phone.PlayerScreen
import com.tamilflix.iptv.ui.settings.SettingsScreen
sealed class Screen { object Home : Screen(); object Settings : Screen(); data class Player(val c: Channel) : Screen() }
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var screen by remember { mutableStateOf<Screen>(Screen.Home) }
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var dark by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) { channels = M3uParser.fetchChannels() }
            when (screen) {
                is Screen.Home -> HomeScreen(channels, dark, { screen = Screen.Player(it) }, { screen = Screen.Settings })
                is Screen.Player -> PlayerScreen((screen as Screen.Player).c, { screen = Screen.Home })
                is Screen.Settings -> SettingsScreen(dark, { dark = !dark }, { screen = Screen.Home })
            }
        }
    }
}
