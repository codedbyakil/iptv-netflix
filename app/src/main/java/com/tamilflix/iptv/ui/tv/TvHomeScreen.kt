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
        val grouped by remember(channels) { derivedStateOf { channels.groupBy { it.group.ifEmpty { "Other" } } } }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(48.dp)
        ) {
            // Header with Search + Settings (YouTube-style minimal)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TamilFlix TV", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // YouTube-style search button
                    Surface(
                        onClick = onSearch,
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurface)
                            Text("Search", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    // Settings button
                    Surface(
                        onClick = onSettings,
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚙️", style = MaterialTheme.typography.bodyLarge)
                            Text("Settings", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
            
            // Category rows (YouTube-style horizontal carousels)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(28.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                grouped.forEach { (category, categoryChannels) ->
                    // Category header (YouTube-style)
                    item(key = "header_$category") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                category,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Text(
                                " • ${categoryChannels.size} channels",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                    // Horizontal channel carousel
                    item(key = "row_$category") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(categoryChannels, key = { channel -> "${channel.group}_${channel.name}" }) { channel ->
                                YouTubeTvCard(channel = channel, onClick = { onPlay(channel) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// YouTube-style TV card with modern glow focus effect
@Composable
fun YouTubeTvCard(channel: Channel, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.04f else 1f,
        label = "cardScale"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.8f else 0f,
        label = "glowAlpha"
    )
    
    Column(
        modifier = Modifier
            .width(220.dp)
            .focusable()
            .onFocusChanged { focusState -> isFocused = focusState.isFocused }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isFocused) 12f else 4f
                shape = RoundedCornerShape(12.dp)
                clip = true
            }
            .background(
                color = if (isFocused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            // YouTube-style focus glow (modern red glow)
            .then(
                if (isFocused) Modifier.graphicsLayer {
                    ambientShadowColor = Color(0xFFE50914).copy(alpha = glowAlpha)
                    spotShadowColor = Color(0xFFE50914).copy(alpha = glowAlpha)
                } else Modifier
            )
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Color(0xFFE50914) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Thumbnail area (YouTube 16:9 ratio)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            if (channel.logoUrl != null && channel.logoUrl.startsWith("http")) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback: colored background + channel initial (YouTube style)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = channel.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // Duration/LIVE badge (YouTube style - bottom right)
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color(0xFFE50915), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        
        // Channel info below thumbnail (YouTube text style)
        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Channel name (YouTube: bold, white, 14sp)
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (isFocused) Color.White else Color(0xFFF5F5F5)
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // Group name (YouTube: gray, 12sp, lighter weight)
            Text(
                text = channel.group,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFBDBDBD), // YouTube gray
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
