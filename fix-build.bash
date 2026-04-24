#!/bin/bash
set -e
echo "🔧 Fixing TamilFlix build errors..."

cd /workspaces/iptv-netflix 2>/dev/null || cd ~/iptv-netflix

# === 1. FIX build.gradle.kts (add missing icons dependency) ===
sed -i '/implementation("androidx.compose.material3:material3:1.1.2")/a\    implementation("androidx.compose.material:material-icons-extended:1.5.4")' app/build.gradle.kts

# === 2. FIX Theme.kt (make TiviMateColors properly accessible) ===
cat > app/src/main/java/com/tamilflix/iptv/ui/theme/Theme.kt << 'EOF'
package com.tamilflix.iptv.ui.theme
import android.app.Activity
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val NetflixDark = darkColorScheme(
    primary = Color(0xFFE50914),
    secondary = Color(0xFFB30000),
    background = Color(0xFF141414),
    surface = Color(0xFF1F1F1F),
    error = Color(0xFFCF6679),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

object TiviMateColors {
    val focusBorder = Color(0xFFE50914)
    val channelBadge = Color(0xFF333333)
    val epgText = Color(0xFFB3B3B3)
    val playingIndicator = Color(0xFF00C853)
}

@Composable
fun TamilFlixTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = NetflixDark.background.toArgb()
        window.navigationBarColor = NetflixDark.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
    MaterialTheme(colorScheme = NetflixDark, content = content)
}
EOF

# === 3. FIX Phone HomeScreen.kt imports ===
cat > app/src/main/java/com/tamilflix/iptv/ui/phone/HomeScreen.kt << 'EOF'
package com.tamilflix.iptv.ui.phone
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import com.tamilflix.iptv.ui.theme.NetflixDark

@Composable
fun HomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    TamilFlixTheme {
        if (channels.isEmpty()) {
            LoadingScreen()
        } else {
            NetflixHome(channels, onChannelClick)
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize().background(NetflixDark.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NetflixDark.primary)
            Text("Loading Tamil channels...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun NetflixHome(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    val grouped = channels.groupBy { it.group }
    val heroChannel = channels.firstOrNull()
    
    Column(modifier = Modifier.fillMaxSize().background(NetflixDark.background)) {
        if (heroChannel != null) {
            HeroBanner(channel = heroChannel, onClick = { onChannelClick(heroChannel) })
        }
        LazyColumn(contentPadding = PaddingValues(top = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            grouped.forEach { (group, groupChannels) ->
                item(key = group) { SectionHeader(title = group) }
                item(key = "${group}_list") { ChannelCarousel(channels = groupChannels, onChannelClick = onChannelClick) }
            }
        }
    }
}

@Composable
fun HeroBanner(channel: Channel, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(240.dp).clickable(onClick = onClick)) {
        if (channel.logoUrl != null) {
            AsyncImage(model = channel.logoUrl, contentDescription = channel.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, NetflixDark.background.copy(alpha = 0.7f), NetflixDark.background), startY = 100f)))
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = channel.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = NetflixDark.primary, shape = RoundedCornerShape(8.dp), modifier = Modifier.width(100.dp).height(40.dp).clickable(onClick = onClick)) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp))
                        Text("Play", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Text(text = channel.group, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
}

@Composable
fun ChannelCarousel(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(channels, key = { it.name + it.url }) { channel -> ChannelCard(channel = channel, onClick = { onChannelClick(channel) }) }
    }
}

@Composable
fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    Column(modifier = Modifier.width(140.dp).clickable(onClick = onClick).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(8.dp)).background(NetflixDark.surface)) {
            if (channel.logoUrl != null) {
                AsyncImage(model = channel.logoUrl, contentDescription = channel.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = channel.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = NetflixDark.primary) }
            }
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Red, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall) }
        }
        Text(text = channel.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
    }
}
EOF

# === 4. FIX Phone PlayerScreen.kt imports ===
cat > app/src/main/java/com/tamilflix/iptv/ui/phone/PlayerScreen.kt << 'EOF'
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
import com.tamilflix.iptv.ui.theme.NetflixDark

@Composable
fun PlayerScreen(channel: Channel, onBack: () -> Unit) {
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
            AndroidView(factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true; setShowNextButton(false); setShowPreviousButton(false); controllerShowTimeoutMs = 4000 } }, modifier = Modifier.fillMaxSize())
            Row(modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White) }
                Text(text = channel.name, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium).padding(12.dp)) {
                Text(text = "📺 ${channel.group}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Text(text = "🔗 ${channel.url.take(40)}${if (channel.url.length > 40) "..." else ""}", color = Color.Gray, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
    }
}
EOF

# === 5. FIX TV HomeScreen.kt imports ===
cat > app/src/main/java/com/tamilflix/iptv/ui/tv/TvHomeScreen.kt << 'EOF'
package com.tamilflix.iptv.ui.tv
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import com.tamilflix.iptv.ui.theme.NetflixDark
import com.tamilflix.iptv.ui.theme.TiviMateColors

@Composable
fun TvHomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    TamilFlixTheme {
        if (channels.isEmpty()) { TvLoadingScreen() } else { TiviMateHome(channels, onChannelClick) }
    }
}

@Composable
fun TvLoadingScreen() {
    Box(modifier = Modifier.fillMaxSize().background(NetflixDark.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NetflixDark.primary)
            Text("Loading Tamil channels...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun TiviMateHome(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    val grouped = channels.groupBy { it.group }
    Column(modifier = Modifier.fillMaxSize().background(NetflixDark.background).padding(48.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TamilFlix TV", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = NetflixDark.primary))
            Text("${channels.size} channels", style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            grouped.forEach { (group, groupChannels) ->
                item(key = group) { Text(group, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(vertical = 8.dp)) }
                item(key = "${group}_list") { TvChannelCarousel(channels = groupChannels, onChannelClick = onChannelClick) }
            }
        }
    }
}

@Composable
fun TvChannelCarousel(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
        items(channels, key = { it.name + it.url }) { channel -> TvChannelCard(channel = channel, onClick = { onChannelClick(channel) }) }
    }
}

@Composable
fun TvChannelCard(channel: Channel, onClick: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    Column(modifier = Modifier.width(220.dp).focusRequester(focusRequester).focusable().onFocusChanged { focusState -> isFocused = focusState.isFocused }.graphicsLayer { scaleX = if (isFocused) 1.06f else 1f; scaleY = if (isFocused) 1.06f else 1f; shadowElevation = if (isFocused) 16f else 4f }.clip(RoundedCornerShape(12.dp)).background(if (isFocused) NetflixDark.primary.copy(alpha = 0.15f) else NetflixDark.surface).border(width = if (isFocused) 3.dp else 0.dp, color = if (isFocused) TiviMateColors.focusBorder else Color.Transparent, shape = RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(8.dp)).background(NetflixDark.surface)) {
            if (channel.logoUrl != null) { AsyncImage(model = channel.logoUrl, contentDescription = channel.name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(8.dp)) } else { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = channel.name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = NetflixDark.primary) } }
            if (isFocused) { Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(TiviMateColors.playingIndicator, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) } }
        }
        Column(modifier = Modifier.padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = channel.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = if (isFocused) NetflixDark.primary else Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(text = channel.group, style = MaterialTheme.typography.bodySmall, color = TiviMateColors.epgText, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
EOF

# === 6. FIX TV PlayerScreen.kt imports ===
cat > app/src/main/java/com/tamilflix/iptv/ui/tv/TvPlayerScreen.kt << 'EOF'
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
EOF

# === 7. Commit and push ===
echo "🔀 Committing fixes..."
git add .
git commit -m "fix: add missing imports + material-icons-extended dependency" || true
git push origin main --force-with-lease 2>/dev/null || echo "⚠️ Push manually if needed"

echo ""
echo "✅ Build fixes applied!"
echo "📦 Re-run workflow: https://github.com/codedbyakil/iptv-netflix/actions"
echo "⏱️  Wait ~3-4 minutes for new APK"
echo "📥 Download: Actions → Artifacts → tamilflix-v1.1-apks"
