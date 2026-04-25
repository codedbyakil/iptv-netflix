package com.tamilflix.iptv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.tamilflix.iptv.data.M3uParser
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.tv.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var screen by remember { mutableStateOf("home") }
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var loadError by remember { mutableStateOf<String?>(null) }
            var selectedChannel by remember { mutableStateOf<Channel?>(null) }
            
            LaunchedEffect(Unit) {
                isLoading = true
                loadError = null
                lifecycleScope.launch {
                    try {
                        channels = M3uParser.fetchChannels()
                        isLoading = false
                    } catch (e: Exception) {
                        loadError = "Failed to load: ${e.message}"
                        isLoading = false
                        channels = emptyList()
                    }
                }
            }
            
            androidx.activity.compose.BackHandler(enabled = screen != "home") { screen = "home" }
            
            when (screen) {
                "home" -> TvHomeScreen(channels = channels, isLoading = isLoading, loadError = loadError, onPlay = { selectedChannel = it; screen = "player" }, onSettings = { screen = "settings" }, onSearch = { screen = "search" }, onRetry = { isLoading = true; loadError = null; lifecycleScope.launch { try { channels = M3uParser.fetchChannels(); isLoading = false } catch (e: Exception) { loadError = "Retry failed: ${e.message}"; isLoading = false } } })
                "player" -> selectedChannel?.let { channel -> TvPlayerScreen(channel = channel, onBack = { screen = "home" }, channels = channels, onChannelChange = { newChannel -> selectedChannel = newChannel }) } ?: run { screen = "home" }
                "settings" -> TvSettingsScreen(onBack = { screen = "home" })
                "search" -> TvSearchScreen(channels = channels, onPlay = { selectedChannel = it; screen = "player" }, onBack = { screen = "home" })
            }
        }
    }
}
