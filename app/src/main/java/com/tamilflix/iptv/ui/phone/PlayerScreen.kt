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
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import kotlinx.coroutines.delay

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(channel: Channel, onBack: () -> Unit) {
    val context = LocalContext.current
    
    // Create player with proper media source factory for all formats
    val player = remember {
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
        
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(DefaultLoadControl.Builder()
                .setBufferDurationsMs(20000, 50000, 10000, 20000)
                .build())
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply { playWhenReady = true }
    }
    
    var error by remember { mutableStateOf<String?>(null) }
    var isReady by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    
    DisposableEffect(channel.url) {
        try {
            val uri = Uri.parse(channel.url)
            val mediaItem = MediaItem.fromUri(uri)
            
            // Try to detect format and create appropriate media source
            val mediaSource: MediaSource = when {
                channel.url.endsWith(".m3u8", ignoreCase = true) || 
                channel.url.contains("type=m3u8", ignoreCase = true) -> {
                    HlsMediaSource.Factory(DefaultDataSource.Factory(context)).createMediaSource(mediaItem)
                }
                channel.url.endsWith(".mpd", ignoreCase = true) -> {
                    DashMediaSource.Factory(DefaultDataSource.Factory(context)).createMediaSource(mediaItem)
                }
                channel.url.startsWith("rtsp://", ignoreCase = true) || 
                channel.url.startsWith("rtspt://", ignoreCase = true) -> {
                    RtspMediaSource.Factory().createMediaSource(mediaItem)
                }
                else -> {
                    // Fallback: let ExoPlayer auto-detect
                    DefaultMediaSourceFactory(context).createMediaSource(mediaItem)
                }
            }
            
            player.setMediaSource(mediaSource)
            player.prepare()
            
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = (state == Player.STATE_BUFFERING)
                    if (state == Player.STATE_READY) {
                        isReady = true
                        isBuffering = false
                    }
                }
                override fun onPlayerError(e: PlaybackException) {
                    error = when (e.errorCode) {
                        PlaybackException.ERROR_CODE_UNSPECIFIED -> "Unknown playback error"
                        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> "Format not supported: ${channel.url}"
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Network error - check connection"
                        PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE -> "Invalid stream format"
                        else -> e.message ?: "Playback failed"
                    }
                    isBuffering = false
                }
            }
            player.addListener(listener)
            
            onDispose {
                player.removeListener(listener)
                player.stop()
                player.release()
            }
        } catch (e: Exception) {
            error = "Failed to load: ${e.message ?: "Unknown error"}"
            onDispose { }
        }
    }
    
    // Auto-retry on certain errors
    LaunchedEffect(error) {
        if (error != null && !error!!.contains("not supported", ignoreCase = true)) {
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
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Buffering indicator
            if (isBuffering && !isReady && error == null) {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text("Buffering...", color = Color.White, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
            
            // Error overlay with helpful message
            error?.let { msg ->
                Box(
                    modifier = Modifier.align(Alignment.Center)
                        .background(Color(0xFFB00020).copy(alpha = 0.9f), MaterialTheme.shapes.medium)
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️ Stream Error", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text(msg, color = Color.White.copy(alpha = 0.9f), 
                            style = MaterialTheme.typography.bodySmall, 
                            modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Show format hint if format error
                        if (msg.contains("not supported", ignoreCase = true) || msg.contains("Format")) {
                            val format = when {
                                channel.url.endsWith(".m3u8") -> "HLS"
                                channel.url.endsWith(".mpd") -> "DASH"
                                channel.url.startsWith("rtsp") -> "RTSP"
                                else -> "Unknown"
                            }
                            Text("Detected format: $format", 
                                style = MaterialTheme.typography.bodySmall, 
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 8.dp))
                        }
                        
                        Button(
                            onClick = { 
                                try { 
                                    player.stop()
                                    player.prepare() 
                                } catch (_: Exception) {} 
                            }, 
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Retry", color = Color.Black)
                        }
                        TextButton(onClick = onBack) { 
                            Text("Back to list", color = Color.White) 
                        }
                    }
                }
            }
            
            // Top bar
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.7f), MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { 
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) 
                }
                Text(channel.name, color = Color.White, 
                    style = MaterialTheme.typography.titleMedium, 
                    maxLines = 1)
            }
        }
    }
}
