package com.tamilflix.iptv.ui.phone
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import kotlinx.coroutines.delay

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
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    
    // Auto-detect TV or landscape → suggest TV mode
    LaunchedEffect(configuration.orientation) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            delay(1000) // Wait for initial render
            // Show subtle hint (in real app: snackbar)
        }
    }
    
    TamilFlixTheme(darkTheme = dark) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // Top App Bar (Google M3 style)
            TopAppBar(
                title = { Text("TamilFlix", style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    if (showSearch) {
                        // Search mode
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search channels...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true,
                            modifier = Modifier
                                .width(200.dp)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                        TextButton(onClick = { showSearch = false; searchQuery = "" }) { Text("Cancel") }
                    } else {
                        // Normal mode
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                        // TV mode hint (if landscape)
                        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            IconButton(onClick = onTvMode) {
                                Icon(Icons.Default.Tv, "TV Mode", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
            
            // Smooth scrolling channel grid
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.name + it.url }) { channel ->
                    ChannelCard(channel = channel, onClick = { onPlay(channel) })
                }
                // Empty state
                if (filtered.isEmpty() && searchQuery.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Text("No channels found", color = Color.Gray, modifier = Modifier.padding(top = 12.dp))
                                Text("Try: Sun TV, Vijay, Polimer", style = MaterialTheme.typography.bodySmall, color = Color.Gray.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
            
            // Scroll-to-top hint (subtle)
            if (listState.firstVisibleItemIndex > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                        .clickable { listState.animateScrollToItem(0) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("↑ Top", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
