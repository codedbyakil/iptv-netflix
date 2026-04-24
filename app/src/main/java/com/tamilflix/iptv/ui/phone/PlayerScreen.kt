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
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(channel: Channel, onBack: () -> Unit) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(30000, 60000, 15000, 30000) // Pre-buffer: 30s min, 60s max
                    .setBackBufferDurationMs(5000, true)
                    .build()
            )
            .build()
            .apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }
    
    var error by remember { mutableStateOf<PlaybackException?>(null) }
    var isBuffering by remember { mutableStateOf(false) }
    
    DisposableEffect(channel.url) {
        player.setMediaItem(MediaItem.fromUri(channel.url))
        player.prepare()
        
        val listener = object : Player.Listener {
            override fun onIsLoadingChanged(isLoading: Boolean) { isBuffering = isLoading }
            override fun onPlayerError(e: PlaybackException) { error = e }
        }
        player.addListener(listener)
        
        onDispose {
            player.removeListener(listener)
            player.stop()
            player.release()
        }
    }
    
    // Auto-retry on error after 2s
    LaunchedEffect(error) {
        if (error != null) {
            delay(2000)
            player.prepare()
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
                        controllerShowTimeoutMs = 4000
                        keepScreenOn = true
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Buffering indicator
            if (isBuffering && !player.isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.7f), MaterialTheme.shapes.medium)
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text("Buffering...", color = Color.White, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
            
            // Error overlay
            error?.let { err ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Red.copy(alpha = 0.8f), MaterialTheme.shapes.medium)
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️ Stream error", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text(err.message ?: "Unknown error", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                        Button(onClick = { player.prepare() }, modifier = Modifier.padding(top = 12.dp)) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }
            
            // Top bar
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.medium)
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
