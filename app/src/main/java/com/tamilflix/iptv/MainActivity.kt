package com.tamilflix.iptv
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.tamilflix.iptv.data.M3uParser
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.phone.HomeScreen
import com.tamilflix.iptv.ui.phone.PlayerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var selected by remember { mutableStateOf<Channel?>(null) }
            LaunchedEffect(Unit) { channels = M3uParser.fetchChannels() }
            if (selected != null) {
                PlayerScreen(channel = selected!!, onBack = { selected = null })
            } else {
                HomeScreen(channels = channels, onChannelClick = { channel -> selected = channel })
            }
        }
    }
}
