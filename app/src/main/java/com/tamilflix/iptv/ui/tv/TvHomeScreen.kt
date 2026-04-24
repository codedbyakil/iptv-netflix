package com.tamilflix.iptv.ui.tv
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import com.tamilflix.iptv.ui.theme.NetflixDark
import com.tamilflix.iptv.ui.theme.TiviMateColors

@Composable
fun TvHomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    TamilFlixTheme {
        if (channels.isEmpty()) { TvLoadingScreen() } else { TiviMateHome(channels, onChannelClick) }
    }
}

@Composable
fun TvLoadingScreen() {
    Box(modifier = Modifier.fillMaxSize().background(NetflixDark.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NetflixDark.primary)
            Text("Loading Tamil channels...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun TiviMateHome(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    val grouped = channels.groupBy { it.group }
    Column(modifier = Modifier.fillMaxSize().background(NetflixDark.background).padding(48.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TamilFlix TV", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = NetflixDark.primary))
            Text("${channels.size} channels", style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            grouped.forEach { (group, groupChannels) ->
                item(key = group) { Text(group, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(vertical = 8.dp)) }
                item(key = "${group}_list") { TvChannelCarousel(channels = groupChannels, onChannelClick = onChannelClick) }
            }
        }
    }
}

@Composable
fun TvChannelCarousel(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
        items(channels, key = { it.name + it.url }) { channel -> TvChannelCard(channel = channel, onClick = { onChannelClick(channel) }) }
    }
}

@Composable
fun TvChannelCard(channel: Channel, onClick: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    Column(modifier = Modifier.width(220.dp).focusRequester(focusRequester).focusable().onFocusChanged { focusState -> isFocused = focusState.isFocused }.graphicsLayer { scaleX = if (isFocused) 1.06f else 1f; scaleY = if (isFocused) 1.06f else 1f; shadowElevation = if (isFocused) 16f else 4f }.clip(RoundedCornerShape(12.dp)).background(if (isFocused) NetflixDark.primary.copy(alpha = 0.15f) else NetflixDark.surface).border(width = if (isFocused) 3.dp else 0.dp, color = if (isFocused) TiviMateColors.focusBorder else Color.Transparent, shape = RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(8.dp)).background(NetflixDark.surface)) {
            if (channel.logoUrl != null) { AsyncImage(model = channel.logoUrl, contentDescription = channel.name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(8.dp)) } else { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = channel.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = NetflixDark.primary) } }
            if (isFocused) { Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(TiviMateColors.playingIndicator, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) } }
        }
        Column(modifier = Modifier.padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = channel.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = if (isFocused) NetflixDark.primary else Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(text = channel.group, style = MaterialTheme.typography.bodySmall, color = TiviMateColors.epgText, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
