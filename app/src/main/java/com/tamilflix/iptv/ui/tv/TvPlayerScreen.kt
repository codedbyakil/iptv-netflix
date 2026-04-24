package com.tamilflix.iptv.ui.tv
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import com.tamilflix.iptv.ui.theme.NetflixDark
import com.tamilflix.iptv.ui.theme.TiviMateColors

@Composable
fun TvPlayerScreen(channel: Channel, onBack: () -> Unit) {
    TamilFlixTheme {
        val context = LocalContext.current
        val player = remember { ExoPlayer.Builder(context).build() }
        DisposableEffect(channel.url) {
            val mediaItem = MediaItem.fromUri(channel.url)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            onDispose { player.stop(); player.release() }
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true; controllerShowTimeoutMs = 8000; setShowNextButton(false); setShowPreviousButton(false); setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS) } }, modifier = Modifier.fillMaxSize())
            Row(modifier = Modifier.align(Alignment.TopStart).padding(32.dp).background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.medium).padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "← BACK", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                Text(text = channel.name, color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), maxLines = 1)
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(32.dp).background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.medium).padding(20.dp)) {
                Text(text = "📺 ${channel.group}", color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                Text(text = "🔗 HLS/DASH Stream", color = Color.Gray, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                Text(text = "🎮 Use D-pad to navigate • OK to select • BACK to exit", color = TiviMateColors.epgText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
