package com.tamilflix.iptv.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.tv.foundation.ExperimentalTvFoundationApi
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
    isLoading: Boolean,
    loadError: String?,
    onPlay: (Channel) -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit
) {
    TamilFlixTvTheme {
        val grouped = channels.groupBy { it.group.ifEmpty { "Other" } }
        val firstFocusRequester = remember { FocusRequester() }
        
        LaunchedEffect(channels.isNotEmpty()) {
            if (channels.isNotEmpty()) {
                kotlinx.coroutines.delay(500)
                firstFocusRequester.requestFocus()
            }
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
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, null)
                            Text("Search", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    Surface(onClick = onSettings, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.focusable()) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Text("⚙️")
                            Text("Settings", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
            
            // Loading state (conditional rendering, NOT early return)
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Text("Loading channels...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
                    }
                }
            } else if (loadError != null) {
                // Error state
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️ Error", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text(loadError, color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                            Text("Retry", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            } else if (channels.isEmpty()) {
                // Empty state
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No channels found", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text("Check your internet connection", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
                    }
                }
            } else {
                // Channel list (TvLazyColumn for TV - official component)
                TvLazyColumn(
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    grouped.forEach { (category, categoryChannels) ->
                        item(key = "header_$category") {
                            Text(category, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                        }
                        item(key = "row_$category") {
                            // TvLazyRow for horizontal scrolling (official TV component)
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
}

// OFFICIAL Android TV Focus Card (from developer.android.com/design/ui/tv)
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
            .width(200.dp)  // Official TV card width
            .height(280.dp) // Fixed height for consistency
            .focusRequester(focusRequester)
            .focusable()  // CRITICAL: Enables D-pad focus
            .onFocusChanged { focusState ->
                // Update IMMEDIATELY when D-pad navigates (like your image)
                isFocused = focusState.isFocused
            }
            .graphicsLayer {
                // Official TV focus animation: scale + elevation
                scaleX = if (isFocused) 1.06f else 1.0f
                scaleY = if (isFocused) 1.06f else 1.0f
                shadowElevation = if (isFocused) 16f else 4f
                shape = androidx.compose.ui.graphics.RectangleShape
                clip = true
            }
            .border(
                // WHITE 4dp border when focused (like your image)
                width = if (isFocused) 4.dp else 0.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                // Subtle background brighten when focused
                color = if (isFocused) Color(0xFF2A2A2A) else Color(0xFF141414),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)  // OK press plays channel
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thumbnail area (16:9 aspect ratio - TV standard)
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
                    // Fallback: channel initial
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = channel.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE50914)
                        )
                    }
                }
            }
            
            // Text info below thumbnail (official TV typography)
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
