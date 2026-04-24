package com.tamilflix.iptv.ui.phone

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    channels: List<Channel>,
    dark: Boolean,
    onPlay: (Channel) -> Unit,
    onSettings: () -> Unit,
    onTvMode: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val grouped = channels.groupBy { it.group.ifEmpty { "Other" } }
    val filtered = if (searchQuery.isBlank()) grouped else {
        grouped.mapValues { (_, list) -> list.filter { it.name.contains(searchQuery, ignoreCase = true) } }
            .filterValues { it.isNotEmpty() }
    }
    
    TamilFlixTheme(darkTheme = dark) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            TopAppBar(
                title = { Text("TamilFlix", fontWeight = FontWeight.Bold) },
                actions = {
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search channels...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true,
                            modifier = Modifier.width(200.dp).padding(end = 8.dp),
                            shape = RoundedCornerShape(24.dp)
                        )
                        TextButton(onClick = { showSearch = false; searchQuery = "" }) { Text("Cancel") }
                    } else {
                        IconButton(onClick = { showSearch = true }) { Icon(Icons.Default.Search, "Search") }
                        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            IconButton(onClick = onTvMode) { Icon(Icons.Default.Tv, "TV Mode", tint = MaterialTheme.colorScheme.primary) }
                        }
                        IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings") }
                    }
                }
            )
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                filtered.forEach { (category, categoryChannels) ->
                    item(key = "header_$category") {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = category, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                            Text(text = "  •  ${categoryChannels.size} channels", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    item(key = "row_$category") {
                        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(categoryChannels, key = { it.name + it.url }) { channel ->
                                ChannelCard(channel = channel, onClick = { onPlay(channel) })
                            }
                        }
                    }
                }
                if (filtered.isEmpty() && searchQuery.isNotBlank()) {
                    item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(48.dp)); Text("No channels found", color = Color.Gray, modifier = Modifier.padding(top = 12.dp)) } } }
                }
            }
        }
    }
}

// ChannelCard defined ONLY here (no duplicate file)
@Composable
fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    Column(modifier = Modifier.width(140.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).background(MaterialTheme.colorScheme.surface).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
            if (channel.logoUrl != null && channel.logoUrl.startsWith("http")) { AsyncImage(model = channel.logoUrl, contentDescription = channel.name, modifier = Modifier.fillMaxSize()) } else { Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(text = channel.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) } }
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color(0xFFE53935), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall) }
        }
        Text(text = channel.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
    }
}
