package com.tamilflix.iptv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.tamilflix.iptv.data.M3uParser
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.tv.TvHomeScreen
import com.tamilflix.iptv.ui.tv.TvPlayerScreen
import com.tamilflix.iptv.ui.tv.TvSettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var screen by remember { mutableStateOf("home") }
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var selectedChannel by remember { mutableStateOf<Channel?>(null) }
            
            LaunchedEffect(Unit) {
                channels = M3uParser.fetchChannels()
            }
            
            when (screen) {
                "home" -> TvHomeScreen(
                    channels = channels,
                    onPlay = { selectedChannel = it; screen = "player" },
                    onSettings = { screen = "settings" }
                )
                "player" -> {
                    selectedChannel?.let { channel ->
                        TvPlayerScreen(channel = channel, onBack = { screen = "home" })
                    } ?: run { screen = "home" }
                }
                "settings" -> TvSettingsScreen(
                    onBack = { screen = "home" }
                )
            }
        }
    }
}
