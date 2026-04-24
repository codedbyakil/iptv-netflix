package com.tamilflix.iptv.ui.phone
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
@Composable fun PlayerScreen(channel: Channel, onBack: () -> Unit) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(Unit) {
        player.setMediaItem(MediaItem.fromUri(channel.url))
        player.prepare()
        player.play()
        onDispose { player.stop(); player.release() }
    }
    TamilFlixTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true; controllerShowTimeoutMs = 4000; setShowNextButton(false); setShowPreviousButton(false); keepScreenOn = true } }, modifier = Modifier.fillMaxSize())
            Row(modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.medium).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { androidx.compose.material3.Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                Text(text = channel.name, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            }
        }
    }
}
