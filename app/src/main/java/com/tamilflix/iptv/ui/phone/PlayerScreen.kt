package com.tamilflix.iptv.ui.phone

import android.content.Context
import android.net.Uri
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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import kotlinx.coroutines.delay

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(channel: Channel, onBack: () -> Unit) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply { playWhenReady = true }
    }
    
    var error by remember { mutableStateOf<String?>(null) }
    var isReady by remember { mutableStateOf(false) }
    
    DisposableEffect(channel.url) {
        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(channel.url))
            player.setMediaItem(mediaItem)
            player.prepare()
            
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) isReady = true
                }
                override fun onPlayerError(e: PlaybackException) {
                    error = e.message ?: "Playback error"
                }
            }
            player.addListener(listener)
            
            onDispose {
                player.removeListener(listener)
                player.stop()
                player.release()
            }
        } catch (e: Exception) {
            error = "Failed to load stream: ${e.message}"
        }
    }
    
    // Auto-retry on error
    LaunchedEffect(error) {
        if (error != null) {
            delay(3000)
            try { player.prepare() } catch (_: Exception) {}
        }
    }
    
    TamilFlixTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Video player
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                        controllerShowTimeoutMs = 5000
                        keepScreenOn = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Loading state
            if (!isReady && error == null) {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text("Loading stream...", color = Color.White, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
            
            // Error state
            error?.let { msg ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color(0xFFB00020).copy(alpha = 0.9f), MaterialTheme.shapes.medium)
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️ Stream Error", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text(msg, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
                        Button(onClick = { try { player.prepare() } catch (_: Exception) {} }, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                            Text("Retry", color = Color.Black)
                        }
                        TextButton(onClick = onBack) { Text("Back to list", color = Color.White) }
                    }
                }
            }
            
            // Top bar
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.7f), MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(channel.name, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            }
        }
    }
}
