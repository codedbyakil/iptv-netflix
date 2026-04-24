package com.tamilflix.iptv.ui.phone
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import com.tamilflix.iptv.ui.theme.NetflixDark
@Composable
fun HomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    TamilFlixTheme {
        Column(modifier = Modifier.fillMaxSize().background(NetflixDark.background).padding(16.dp)) {
            Text("TamilFlix", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = NetflixDark.primary), modifier = Modifier.padding(bottom = 24.dp))
            if (channels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NetflixDark.primary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(channels.groupBy { it.group }.toList()) { (group, groupChannels) ->
                        Text(group, style = MaterialTheme.typography.titleLarge.copy(color = Color.White), modifier = Modifier.padding(vertical = 8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(groupChannels) { channel ->
                                Surface(onClick = { onChannelClick(channel) }, color = NetflixDark.surface, modifier = Modifier.width(140.dp).height(80.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text(channel.name, color = Color.White, maxLines = 2) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PlayerScreen(channel: Channel, onBack: () -> Unit) {
    TamilFlixTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Text("Playing: ${channel.name}\n\nURL: ${channel.url}\n\n(ExoPlayer integration coming soon)", color = Color.White, modifier = Modifier.padding(24.dp))
            FloatingActionButton(onClick = onBack, containerColor = NetflixDark.primary, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) { Text("←", color = Color.White) }
        }
    }
}
