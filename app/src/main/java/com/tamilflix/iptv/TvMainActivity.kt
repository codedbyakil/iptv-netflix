package com.tamilflix.iptv
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.tamilflix.iptv.data.M3uParser
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.tv.TvHomeScreen
import com.tamilflix.iptv.ui.tv.TvPlayerScreen

class TvMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var selectedChannel by remember { mutableStateOf<Channel?>(null) }
            
            LaunchedEffect(Unit) {
                channels = M3uParser.fetchChannels()
            }
            
            if (selectedChannel != null) {
                TvPlayerScreen(
                    channel = selectedChannel!!,
                    onBack = { selectedChannel = null }
                )
            } else {
                TvHomeScreen(
                    channels = channels,
                    onChannelClick = { selectedChannel = it }
                )
            }
        }
    }
}
