package com.tamilflix.iptv

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.tamilflix.iptv.data.M3uParser
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.tv.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var screen by remember { mutableStateOf("home") }
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var selectedChannel by remember { mutableStateOf<Channel?>(null) }
            val prefs = remember { getSharedPreferences("tamilflix", Context.MODE_PRIVATE) }
            
            LaunchedEffect(Unit) { 
                channels = M3uParser.fetchChannels()
                // Auto-load last played channel
                val lastUrl = prefs.getString("last_channel_url", null)
                if (lastUrl != null) {
                    selectedChannel = channels.firstOrNull { it.url == lastUrl }
                    if (selectedChannel != null) screen = "player"
                }
            }
            
            // BACK button handler: always return to home, never exit app
            androidx.activity.compose.BackHandler(enabled = screen != "home") { screen = "home" }
            
            when (screen) {
                "home" -> TvHomeScreen(channels = channels, onPlay = { selectedChannel = it; screen = "player" }, onSettings = { screen = "settings" }, onSearch = { screen = "search" })
                "player" -> selectedChannel?.let { channel -> TvPlayerScreen(channel = channel, onBack = { screen = "home" }, channels = channels, onChannelChange = { newChannel -> selectedChannel = newChannel }) } ?: run { screen = "home" }
                "settings" -> TvSettingsScreen(onBack = { screen = "home" })
                "search" -> TvSearchScreen(channels = channels, onPlay = { selectedChannel = it; screen = "player" }, onBack = { screen = "home" })
            }
        }
    }
}
