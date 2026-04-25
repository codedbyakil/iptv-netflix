package com.tamilflix.iptv.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTvTheme

@OptIn(ExperimentalTvFoundationApi::class)
@Composable
fun TvHomeScreen(
    channels: List<Channel>,
    onPlay: (Channel) -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit
) {
    TamilFlixTvTheme {
        val grouped = channels.groupBy { it.group.ifEmpty { "Other" } }
        val firstFocusRequester = remember { FocusRequester() }
        
        // Auto-focus first card on load
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(500)
            firstFocusRequester.requestFocus()
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(48.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TamilFlix TV", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(onClick = onSearch, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.height(40.dp).focusable()) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, null); Text("Search", modifier = Modifier.padding(start = 8.dp)) }
                    }
                    Surface(onClick = onSettings, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.focusable()) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) { Text("⚙️"); Text("Settings", modifier = Modifier.padding(start = 8.dp)) }
                    }
                }
            }
            
            // TvLazyColumn (TV-optimized)
            TvLazyColumn(
                verticalArrangement = Arrangement.spacedBy(28.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                grouped.forEach { (category, categoryChannels) ->
                    item(key = "header_$category") {
                        Text(category, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                    }
                    item(key = "row_$category") {
                        // TvLazyRow (TV-optimized)
                        TvLazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(categoryChannels, key = { "${it.group}_${it.name}" }) { channel ->
                                val isFirstCard = category == grouped.keys.firstOrNull() && channel == categoryChannels.firstOrNull()
                                TvFocusCard(
                                    channel = channel,
                                    onClick = { onPlay(channel) },
                                    autoFocus = isFirstCard,
                                    focusRequester = if (isFirstCard) firstFocusRequester else FocusRequester()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// TV Focus Card with proper focus handling
@Composable
fun TvFocusCard(
    channel: Channel,
    onClick: () -> Unit,
    autoFocus: Boolean = false,
    focusRequester: FocusRequester
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(280.dp)  // Fixed height for consistency
            .focusRequester(focusRequester)
            .focusable()  // CRITICAL: Enables TV D-pad focus
            .onFocusChanged { focusState ->
                // Update IMMEDIATELY when D-pad navigates
                isFocused = focusState.isFocused
            }
            .graphicsLayer {
                // Netflix-style scale animation
                scaleX = if (isFocused) 1.06f else 1.0f
                scaleY = if (isFocused) 1.06f else 1.0f
                shadowElevation = if (isFocused) 16f else 4f
                shape = RoundedCornerShape(12.dp)
                clip = true
            }
            .border(
                // WHITE border when focused (like your image)
                width = if (isFocused) 4.dp else 0.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (isFocused) Color(0xFF2A2A2A) else Color(0xFF141414),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thumbnail (16:9)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color(0xFF2A2A2A))
            ) {
                if (channel.logoUrl != null && channel.logoUrl.startsWith("http")) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = channel.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE50914)
                        )
                    }
                }
            }
            
            // Text info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                        color = if (isFocused) Color.White else Color(0xFFF5F5F5)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = channel.group,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBDBDBD)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
