package com.tamilflix.iptv.ui.tv
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NetflixDark.primary)
                        Text("Loading channels...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    items(channels.groupBy { it.group }.toList()) { (group, groupChannels) ->
                        Text(group, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.SemiBold))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(groupChannels) { channel ->
                                Surface(onClick = { onChannelClick(channel) }, color = NetflixDark.surface, modifier = Modifier.width(200.dp).height(100.dp).focusable().padding(vertical = 4.dp)) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                        Text(channel.name, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
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
            Column(modifier = Modifier.padding(48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("TV: Now Playing", style = MaterialTheme.typography.titleLarge.copy(color = NetflixDark.primary))
                Text(channel.name, style = MaterialTheme.typography.headlineMedium.copy(color = Color.White))
                Text("(ExoPlayer TV - coming soon)", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            }
        }
    }
}
