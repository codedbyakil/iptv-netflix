package com.tamilflix.iptv.ui.tv

// === ALL REQUIRED IMPORTS ===
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable  // <-- THIS WAS MISSING!
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
import androidx.compose.ui.draw.scale
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
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, null); Text("Search", modifier = Modifier.padding(start = 8.dp)) }
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
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(categoryChannels, key = { "${it.group}_${it.name}" }) { channel ->
                                AndroidTvCard(
                                    channel = channel,
                                    onClick = { onPlay(channel) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// OFFICIAL Android TV Card (from developer.android.com/design/ui/tv)
@Composable
fun AndroidTvCard(channel: Channel, onClick: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val scale = if (isFocused) 1.04f else 1.0f
    
    Column(
        modifier = Modifier
            .width(200.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { focusState -> isFocused = focusState.isFocused }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isFocused) 16f else 4f
                shape = androidx.compose.ui.graphics.RectangleShape
                clip = true
            }
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                brush = if (isFocused) Brush.horizontalGradient(listOf(Color(0xFFE50914), Color(0xFFFFD700))) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                brush = Brush.verticalGradient(colors = listOf(if (isFocused) Color(0xFF2A2A2A) else Color(0xFF1F1F1F), if (isFocused) Color(0xFF1F1F1F) else Color(0xFF141414))),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)  // <-- Now with proper import
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Thumbnail
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(Color(0xFF2A2A2A))) {
            if (channel.logoUrl != null && channel.logoUrl.startsWith("http")) {
                AsyncImage(model = channel.logoUrl, contentDescription = channel.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF0A0A0A))))) {
                    Text(text = channel.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE50914))
                }
            }
        }
        // Content
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.Start) {
            Text(text = channel.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium, color = if (isFocused) Color.White else Color(0xFFF5F5F5)), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(text = channel.group, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBDBDBD), fontWeight = FontWeight.Normal), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
