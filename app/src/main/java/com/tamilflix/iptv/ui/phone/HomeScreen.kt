package com.tamilflix.iptv.ui.phone
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import com.tamilflix.iptv.ui.theme.NetflixDark

@Composable
fun HomeScreen(channels: List<Channel>, darkTheme: Boolean, onChannelClick: (Channel) -> Unit, onSettingsClick: () -> Unit) {
    TamilFlixTheme(darkTheme = darkTheme) {
        if (channels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NetflixDark.primary)
                    Text("Loading...", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 16.dp))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(bottom = 16.dp)) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TamilFlix", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onBackground) }
                    }
                }
                channels.groupBy { it.group }.forEach { (group, groupChannels) ->
                    item { Text(group, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) }
                    item {
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(groupChannels, key = { it.name + it.url }) { channel ->
                                Column(modifier = Modifier.width(130.dp).clickable(onClick = { onChannelClick(channel) }), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)) {
                                        if (channel.logoUrl != null) AsyncImage(model = channel.logoUrl, contentDescription = channel.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        else Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(channel.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = NetflixDark.primary) }
                                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).background(Color.Red, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall) }
                                    }
                                    Text(channel.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
