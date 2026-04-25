package com.tamilflix.iptv.ui.tv

import android.content.Context
import android.net.Uri
import android.os.PowerManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTvTheme
import kotlinx.coroutines.delay

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun TvPlayerScreen(channel: Channel, onBack: () -> Unit, channels: List<Channel>, onChannelChange: (Channel) -> Unit) {
    val context = LocalContext.current
    val wakeLock = remember { (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "TamilFlix:Player").apply { setReferenceCounted(false) } }
    
    val player = remember {
        val dataSourceFactory = DefaultDataSource.Factory(context)
        ExoPlayer.Builder(context).setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory)).setLoadControl(DefaultLoadControl.Builder().setBufferDurationsMs(60000, 120000, 30000, 60000).build()).setHandleAudioBecomingNoisy(true).setWakeMode(C.WAKE_MODE_LOCAL).build().apply { playWhenReady = true }
    }
    
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    DisposableEffect(channel.url) {
        wakeLock.acquire(10*60*1000L)
        isLoading = true; error = null
        try {
            val uri = Uri.parse(channel.url); val mediaItem = MediaItem.fromUri(uri)
            val dataSourceFactory = DefaultDataSource.Factory(context)
            val mediaSource: MediaSource = when {
                channel.url.endsWith(".m3u8", ignoreCase = true) -> HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
                channel.url.endsWith(".mpd", ignoreCase = true) -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
                channel.url.startsWith("rtsp://", ignoreCase = true) -> RtspMediaSource.Factory().createMediaSource(mediaItem)
                else -> DefaultMediaSourceFactory(context).createMediaSource(mediaItem)
            }
            player.setMediaSource(mediaSource); player.prepare()
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) { if (state == Player.STATE_READY) isLoading = false }
                override fun onPlayerError(e: PlaybackException) { isLoading = false; error = e.message ?: "Playback error" }
            }
            player.addListener(listener)
            onDispose { player.removeListener(listener); player.stop(); player.release(); if (wakeLock.isHeld) wakeLock.release() }
        } catch (e: Exception) { isLoading = false; error = "Failed: ${e.message}"; onDispose { if (wakeLock.isHeld) wakeLock.release() } }
    }
    
    LaunchedEffect(error) { if (error != null) { delay(4000); try { player.prepare() } catch (_: Exception) {} } }
    
    TamilFlixTvTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true; controllerShowTimeoutMs = 5000; keepScreenOn = true; setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER); resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL }}, modifier = Modifier.fillMaxSize())
            if (isLoading && error == null) { Box(modifier = Modifier.align(Alignment.Center)) { CircularProgressIndicator(color = Color(0xFFE50914), modifier = Modifier.size(48.dp)) } }
            error?.let { msg -> Box(modifier = Modifier.align(Alignment.Center).background(Color(0xFFB00020).copy(alpha = 0.95f), MaterialTheme.shapes.medium).padding(32.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Error", color = Color.White); Text(msg, modifier = Modifier.padding(vertical = 16.dp)); Button(onClick = { try { player.prepare() } catch (_: Exception) {} }) { Text("Retry") } } } }
        }
    }
}
