package com.tamilflix.iptv.ui.phone
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.AppTheme
@Composable fun HomeScreen(channels: List<Channel>, dark: Boolean, onPlay: (Channel) -> Unit, onSettings: () -> Unit) { AppTheme(dark) { Column { Row { Text("TamilFlix"); IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, null) } }; LazyColumn { items(channels) { c -> Row(onClick = { onPlay(c) }) { AsyncImage(c.logoUrl, null); Text(c.name) } } } } } }
