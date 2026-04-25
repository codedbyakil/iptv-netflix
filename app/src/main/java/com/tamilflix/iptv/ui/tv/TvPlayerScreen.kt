package com.tamilflix.iptv.ui.tv

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.PowerManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTvTheme
import kotlinx.coroutines.delay

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun TvPlayerScreen(
    channel: Channel,
    onBack: () -> Unit,
    channels: List<Channel>,
    onChannelChange: (Channel) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tamilflix", Context.MODE_PRIVATE) }
    
    // Save last played channel
    LaunchedEffect(channel.url) {
        prefs.edit()
            .putString("last_channel_url", channel.url)
            .putString("last_channel_name", channel.name)
            .apply()
    }
    
    // Wake lock
    val wakeLock = remember {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "TamilFlix:Player")
            .apply { setReferenceCounted(false) }
    }
    
    // Player
    val player = remember {
        val dataSourceFactory = DefaultDataSource.Factory(context)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory)
            )
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(60000, 120000, 30000, 60000)
                    .build()
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
            .apply { playWhenReady = true }
    }
    
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showControls by remember { mutableStateOf(false) }
    
    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }
    
    // Setup player
    DisposableEffect(channel.url) {
        wakeLock.acquire(10 * 60 * 1000L)
        isLoading = true
        error = null
        showControls = false
        
        try {
            val uri = Uri.parse(channel.url)
            val mediaItem = MediaItem.fromUri(uri)
            val dataSourceFactory = DefaultDataSource.Factory(context)
            
            val mediaSource: MediaSource = when {
                channel.url.endsWith(".m3u8", ignoreCase = true) ->
                    HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
                channel.url.endsWith(".mpd", ignoreCase = true) ->
                    DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
                channel.url.startsWith("rtsp://", ignoreCase = true) ->
                    RtspMediaSource.Factory().createMediaSource(mediaItem)
                else ->
                    DefaultMediaSourceFactory(context).createMediaSource(mediaItem)
            }
            
            player.setMediaSource(mediaSource)
            player.prepare()
            
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        isLoading = false
                        showControls = false
                    }
                }
                override fun onPlayerError(e: PlaybackException) {
                    isLoading = false
                    error = e.message ?: "Playback error"
                    showControls = true
                }
            }
            player.addListener(listener)
            
            onDispose {
                player.removeListener(listener)
                player.stop()
                player.release()
                if (wakeLock.isHeld) wakeLock.release()
            }
        } catch (e: Exception) {
            isLoading = false
            error = "Failed: ${e.message}"
            onDispose {
                if (wakeLock.isHeld) wakeLock.release()
            }
        }
    }
    
    // Auto-retry
    LaunchedEffect(error) {
        if (error != null) {
            delay(4000)
            try { player.prepare() } catch (_: Exception) {}
        }
    }
    
    TamilFlixTvTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Video player - PlayerView handles remote keys natively
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = showControls
                        controllerShowTimeoutMs = if (showControls) 5000 else 0
                        keepScreenOn = true
                        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        // PlayerView natively handles BACK button
                        setOnKeyListener { _, keyCode, event ->
                            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                                when (keyCode) {
                                    android.view.KeyEvent.KEYCODE_BACK -> {
                                        onBack()
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Loading
            if (isLoading && error == null) {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    CircularProgressIndicator(
                        color = Color(0xFFE50914),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // Error
            error?.let { msg ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            Color(0xFFB00020).copy(alpha = 0.95f),
                            MaterialTheme.shapes.medium
                        )
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text(msg, modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.9f))
                        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { try { player.prepare() } catch (_: Exception) {} },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                            ) {
                                Text("Retry", color = Color.Black)
                            }
                            OutlinedButton(
                                onClick = onBack,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Back")
                            }
                        }
                    }
                }
            }
            
            // Controls bar
            if (showControls) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(32.dp)
                        .background(
                            Color.Black.copy(alpha = 0.8f),
                            MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text("← BACK", color = Color.Gray)
                    Text(channel.name, color = Color.White, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }
    }
}
