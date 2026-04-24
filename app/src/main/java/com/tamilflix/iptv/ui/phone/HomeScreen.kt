package com.tamilflix.iptv.ui.phone

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    val filtered = if (searchQuery.isBlank()) channels else channels.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val configuration = LocalConfiguration.current
    
    TamilFlixTheme(darkTheme = dark) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            TopAppBar(
                title = { Text("TamilFlix") },
                actions = {
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search...") },
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
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.name + it.url }) { channel ->
                    ChannelCard(channel = channel, onClick = { onPlay(channel) })
                }
                if (filtered.isEmpty() && searchQuery.isNotBlank()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Text("No channels found", color = Color.Gray, modifier = Modifier.padding(top = 12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
