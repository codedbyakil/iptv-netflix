package com.tamilflix.iptv.ui.phone
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun HomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    TamilFlixTheme {
        if (channels.isEmpty()) {
            LoadingScreen()
        } else {
            NetflixHome(channels, onChannelClick)
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize().background(NetflixDark.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NetflixDark.primary)
            Text("Loading Tamil channels...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun NetflixHome(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    val grouped = channels.groupBy { it.group }
    val heroChannel = channels.firstOrNull()
    
    Column(modifier = Modifier.fillMaxSize().background(NetflixDark.background)) {
        if (heroChannel != null) {
            HeroBanner(channel = heroChannel, onClick = { onChannelClick(heroChannel) })
        }
        LazyColumn(contentPadding = PaddingValues(top = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            grouped.forEach { (group, groupChannels) ->
                item(key = group) { SectionHeader(title = group) }
                item(key = "${group}_list") { ChannelCarousel(channels = groupChannels, onChannelClick = onChannelClick) }
            }
        }
    }
}

@Composable
fun HeroBanner(channel: Channel, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(240.dp).clickable(onClick = onClick)) {
        if (channel.logoUrl != null) {
            AsyncImage(model = channel.logoUrl, contentDescription = channel.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, NetflixDark.background.copy(alpha = 0.7f), NetflixDark.background), startY = 100f)))
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = channel.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = NetflixDark.primary, shape = RoundedCornerShape(8.dp), modifier = Modifier.width(100.dp).height(40.dp).clickable(onClick = onClick)) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp))
                        Text("Play", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Text(text = channel.group, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
}

@Composable
fun ChannelCarousel(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(channels, key = { it.name + it.url }) { channel -> ChannelCard(channel = channel, onClick = { onChannelClick(channel) }) }
    }
}

@Composable
fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    Column(modifier = Modifier.width(140.dp).clickable(onClick = onClick).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(8.dp)).background(NetflixDark.surface)) {
            if (channel.logoUrl != null) {
                AsyncImage(model = channel.logoUrl, contentDescription = channel.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = channel.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = NetflixDark.primary) }
            }
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Red, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall) }
        }
        Text(text = channel.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
    }
}
