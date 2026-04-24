package com.tamilflix.iptv.ui.tv
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
fun TvHomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    TamilFlixTheme {
        Column(modifier = Modifier.fillMaxSize().background(NetflixDark.background).padding(48.dp)) {
            Text("TamilFlix TV", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = NetflixDark.primary), modifier = Modifier.padding(bottom = 24.dp))
            if (channels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NetflixDark.primary) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    items(channels.groupBy { it.group }.toList()) { (group, groupChannels) ->
                        Text(group, style = MaterialTheme.typography.titleLarge.copy(color = Color.White))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(groupChannels) { channel ->
                                Surface(onClick = { onChannelClick(channel) }, color = NetflixDark.surface, modifier = Modifier.width(200.dp).height(100.dp).focusable()) {
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
fun TvPlayerScreen(channel: Channel, onBack: () -> Unit) {
    TamilFlixTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Text("TV Playing: ${channel.name}", color = Color.White, modifier = Modifier.padding(48.dp))
        }
    }
}
