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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var screen by remember { mutableStateOf("home") }
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var dark by remember { mutableStateOf(true) }
            var selectedChannel by remember { mutableStateOf<Channel?>(null) }
            
            LaunchedEffect(Unit) { channels = M3uParser.fetchChannels() }
            
            when (screen) {
                "home" -> HomeScreen(channels, dark, { selectedChannel = it; screen = "player" }, { screen = "settings" })
                "player" -> selectedChannel?.let { PlayerScreen(it) { screen = "home" } } ?: run { screen = "home" }
                "settings" -> SettingsScreen(dark, { dark = !dark }, { screen = "home" })
            }
        }
    }
}
