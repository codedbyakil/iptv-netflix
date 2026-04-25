package com.tamilflix.iptv.ui.tv

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTvTheme

@Composable
fun TvHomeScreen(channels: List<Channel>, onPlay: (Channel) -> Unit, onSettings: () -> Unit, onSearch: () -> Unit) {
    TamilFlixTvTheme {
        // Group channels once (derived state for performance)
        val grouped by remember(channels) { derivedStateOf { channels.groupBy { it.group.ifEmpty { "Other" } } } }
        
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(48.dp)) {
            // Header with Search + Settings
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TamilFlix TV", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(onClick = onSearch, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.height(40.dp)) { Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, null); Text("Search", modifier = Modifier.padding(start = 8.dp)) } }
                    Surface(onClick = onSettings, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) { Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) { Text("⚙️"); Text("Settings", modifier = Modifier.padding(start = 8.dp)) } }
                }
            }
            
            // Optimized category rows
            LazyColumn(verticalArrangement = Arrangement.spacedBy(28.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                grouped.forEach { (category, categoryChannels) ->
                    item(key = "header_$category") { Row(verticalAlignment = Alignment.CenterVertically) { Text(category, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)); Text(" • ${categoryChannels.size} channels", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), modifier = Modifier.padding(start = 12.dp)) } }
                    item(key = "row_$category") { LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(vertical = 8.dp)) { items(categoryChannels, key = { channel -> "${channel.group}_${channel.name}" }) { channel -> NetflixTvCard(channel = channel, onClick = { onPlay(channel) }) } } }
                }
            }
        }
    }
}

// Netflix-style card with focus animations (optimized)
@Composable
fun NetflixTvCard(channel: Channel, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.06f else 1f, animationSpec = spring(stiffness = Spring.StiffnessLow), label = "cardScale")
    
    Column(modifier = Modifier.width(240.dp).focusable().onFocusChanged { isFocused = it.isFocused }.graphicsLayer { scaleX = scale; scaleY = scale; shadowElevation = if (isFocused) 16f else 4f; shape = RoundedCornerShape(16.dp); clip = true }.background(brush = if (isFocused) Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), MaterialTheme.colorScheme.surface)) else Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)), shape = RoundedCornerShape(16.dp)).border(width = if (isFocused) 3.dp else 0.dp, brush = if (isFocused) Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)), shape = RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) { if (channel.logoUrl != null && channel.logoUrl.startsWith("http")) { AsyncImage(model = channel.logoUrl, contentDescription = channel.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } else { Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(channel.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary) } }; if (isFocused) { Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color(0xFFE53935), RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) } } }
        Text(channel.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal, brush = if (isFocused) Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)) else null), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 10.dp)); Text(channel.group, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}
