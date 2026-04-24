package com.tamilflix.iptv
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
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
            val configuration = LocalConfiguration.current
            
            LaunchedEffect(Unit) {
                channels = M3uParser.fetchChannels()
            }
            
            // Auto-switch to TV UI if landscape or TV device
            val isTvMode = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || 
                          resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_TYPE_MASK == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
            
            when (screen) {
                "home" -> HomeScreen(
                    channels = channels,
                    dark = dark,
                    onPlay = { selectedChannel = it; screen = "player" },
                    onSettings = { screen = "settings" },
                    onTvMode = { if (isTvMode) screen = "tv" } // Future: add TV screen
                )
                "player" -> selectedChannel?.let {
                    PlayerScreen(channel = it, onBack = { screen = "home" })
                } ?: run { screen = "home" }
                "settings" -> SettingsScreen(
                    dark = dark,
                    onToggle = { dark = !dark },
                    onBack = { screen = "home" }
                )
            }
        }
    }
}
