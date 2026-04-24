package com.tamilflix.iptv.ui.phone
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.AppTheme
@Composable fun PlayerScreen(channel: Channel, onBack: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val player = remember { ExoPlayer.Builder(ctx).build() }
    DisposableEffect(Unit) { player.setMediaItem(MediaItem.fromUri(channel.url)); player.prepare(); player.play(); onDispose { player.release() } }
    AppTheme(true) { Box { AndroidView({ PlayerView(it).apply { this.player = player; useController = true } }, Modifier.fillMaxSize()); IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = androidx.compose.ui.graphics.Color.White) } } }
}
