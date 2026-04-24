package com.tamilflix.iptv.ui.phone
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun HomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    TamilFlixTheme {
        Column(modifier = Modifier.fillMaxSize().background(NetflixDark.background)) {
            Text("TamilFlix", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = NetflixDark.primary), modifier = Modifier.padding(16.dp))
            if (channels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NetflixDark.primary)
                        Text("Loading channels...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(channels.groupBy { it.group }.toList()) { (group, groupChannels) ->
                        Text(group, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(vertical = 8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(groupChannels) { channel ->
                                Surface(onClick = { onChannelClick(channel) }, color = NetflixDark.surface, modifier = Modifier.width(140.dp).height(80.dp).padding(vertical = 4.dp)) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
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
fun PlayerScreen(channel: Channel, onBack: () -> Unit) {
    TamilFlixTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Now Playing", style = MaterialTheme.typography.titleLarge.copy(color = NetflixDark.primary))
                Text(channel.name, style = MaterialTheme.typography.headlineMedium.copy(color = Color.White))
                Text("(ExoPlayer integration - coming soon)", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            }
            FloatingActionButton(onClick = onBack, containerColor = NetflixDark.primary, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) { Text("←", color = Color.White) }
        }
    }
}
