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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTvTheme

@Composable
fun TvHomeScreen(
    channels: List<Channel>,
    onPlay: (Channel) -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit
) {
    TamilFlixTvTheme {
        val grouped = channels.groupBy { it.group.ifEmpty { "Other" } }
        
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
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, null); Text("Search", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    Surface(onClick = onSettings, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.focusable()) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) { Text("⚙️"); Text("Settings", modifier = Modifier.padding(start = 8.dp)) }
                    }
                }
            }
            
            // Category rows
            LazyColumn(verticalArrangement = Arrangement.spacedBy(28.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                grouped.forEach { (category, categoryChannels) ->
                    item(key = "header_$category") {
                        Text(category, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                    }
                    item(key = "row_$category") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(categoryChannels, key = { "${it.group}_${it.name}" }) { channel ->
                                // Auto-focus the very first card of the first row
                                val isFirstCard = category == grouped.keys.firstOrNull() && channel == categoryChannels.firstOrNull()
                                TvFocusCard(
                                    channel = channel,
                                    onClick = { onPlay(channel) },
                                    autoFocus = isFirstCard
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// YouTube-Style Card with Instant D-pad Focus
@Composable
fun TvFocusCard(channel: Channel, onClick: () -> Unit, autoFocus: Boolean = false) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    // Auto-focus first card on load (YouTube style)
    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            // Small delay ensures the UI is ready to accept focus
            kotlinx.coroutines.delay(500)
            focusRequester.requestFocus()
        }
    }
    
    Column(
        modifier = Modifier
            .width(220.dp)
            .focusRequester(focusRequester) // Allows programmatic focus
            .focusable() // CRITICAL: Enables D-pad focus without clicking
            .onFocusChanged { focusState ->
                // Updates IMMEDIATELY when D-pad moves to this card
                isFocused = focusState.isFocused
            }
            .border(
                // Always visible red border when focused
                width = if (isFocused) 4.dp else 0.dp,
                color = if (isFocused) Color(0xFFE50914) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                // Background brightens when focused
                color = if (isFocused) Color(0xFF1A1A1A) else Color(0xFF141414),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick) // OK press plays channel
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
                .clip(RoundedCornerShape(8.dp))
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
        
        // Channel Name
        Text(
            text = channel.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
                color = if (isFocused) Color.White else Color(0xFFF5F5F5)
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Group
        Text(
            text = channel.group,
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBDBDBD)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
