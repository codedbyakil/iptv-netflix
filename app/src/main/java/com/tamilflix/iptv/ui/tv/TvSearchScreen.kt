package com.tamilflix.iptv.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTvTheme

@Composable
fun TvSearchScreen(channels: List<Channel>, onPlay: (Channel) -> Unit, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val filtered = if (query.isBlank()) emptyList() else channels.filter { it.name.contains(query, ignoreCase = true) }
    
    TamilFlixTvTheme {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF16171D)).padding(48.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF16171D)).border(width = 1.5.dp, color = Color(0xFF2B2C37), shape = RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFFBDBECB)) }
                Icon(Icons.Default.Search, null, tint = Color(0xFFBDBECB), modifier = Modifier.padding(end = 8.dp))
                BasicTextField(value = query, onValueChange = { query = it }, modifier = Modifier.weight(1f).focusRequester(focusRequester), textStyle = LocalTextStyle.current.copy(color = Color(0xFFBDBECB)), cursorBrush = SolidColor(Color(0xFFE50914)), singleLine = true, decorationBox = { innerTextField -> if (query.isEmpty()) Text("Search channels...", color = Color(0xFFBDBECB)); innerTextField() })
                if (query.isNotBlank()) TextButton(onClick = { query = "" }) { Text("Clear", color = Color(0xFFE50914)) }
            }
            
            LazyColumn(contentPadding = PaddingValues(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.name + it.url }) { channel ->
                    Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onPlay(channel) }, color = Color(0xFF1A1A1A)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A2A))) { if (channel.logoUrl != null) { /* AsyncImage here if needed */ } else { Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(channel.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFE50914)) } } }
                            Column(modifier = Modifier.weight(1f)) { Text(channel.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFFF5F5F5)); Text(channel.group, style = MaterialTheme.typography.bodySmall, color = Color(0xFFBDBDBD)) }
                            Text("▶", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFE50914))
                        }
                    }
                }
                if (query.isNotBlank() && filtered.isEmpty()) { item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(48.dp)); Text("No results for \"$query\"", color = Color.Gray, modifier = Modifier.padding(top = 12.dp)) } } } }
            }
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
