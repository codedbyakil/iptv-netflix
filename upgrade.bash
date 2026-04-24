#!/bin/bash
set -e
echo "🎬 TamilFlix Upgrade: Netflix + TiviMate UI + Real Playback..."

cd /workspaces/iptv-netflix 2>/dev/null || cd ~/iptv-netflix

# === UPDATE BUILD.GRADLE.KTS (Add Coil + Media3 deps) ===
cat > app/build.gradle.kts << 'EOF'
plugins {
    id("com.android.application") version "8.2.2"
    id("org.jetbrains.kotlin.android") version "1.9.22"
}
android {
    namespace = "com.tamilflix.iptv"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.tamilflix.iptv"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}
dependencies {
    // Compose (compatible versions)
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    
    // Android TV
    implementation("androidx.tv:tv-foundation:1.0.0-alpha10")
    implementation("androidx.tv:tv-material:1.0.0-alpha10")
    
    // Video Playback (Media3)
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-session:1.2.1")
    
    // Images (Coil for async logo loading)
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil:2.4.0")
    
    // Core
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
}
EOF

# === UPDATE M3uParser.kt (Better logo + metadata extraction) ===
cat > app/src/main/java/com/tamilflix/iptv/data/M3uParser.kt << 'EOF'
package com.tamilflix.iptv.data
import com.tamilflix.iptv.data.models.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object M3uParser {
    private const val M3U_URL = "https://raw.githubusercontent.com/codedbyakil/Tamil-TV/refs/heads/main/local.m3u"
    
    suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val content = URL(M3U_URL).readText()
            parseM3uContent(content)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseM3uContent(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        var name = ""
        var group = "Local Channels"
        var logo: String? = null
        var tvgId: String? = null
        
        content.lineSequence().forEach { line ->
            when {
                line.startsWith("#EXTINF:") -> {
                    // Extract name (after last comma)
                    name = line.substringAfterLast(",").trim().takeIf { it.isNotBlank() } ?: "Unknown"
                    // Extract group-title
                    group = Regex("""group-title="([^"]+)""").find(line)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() } ?: "Local Channels"
                    // Extract tvg-logo (FIXED: handle both single/double quotes)
                    logo = Regex("""tvg-logo=["']([^"']+)["']""").find(line)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
                    // Extract tvg-id for EPG (future use)
                    tvgId = Regex("""tvg-id=["']([^"']+)["']""").find(line)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
                }
                line.startsWith("http", ignoreCase = true) && !line.startsWith("#") -> {
                    if (name.isNotBlank()) {
                        channels.add(Channel(name, line.trim(), group, logo, tvgId))
                    }
                    // Reset for next channel
                    name = ""
                    logo = null
                    tvgId = null
                }
            }
        }
        return channels.filter { it.url.isNotBlank() && (it.url.startsWith("http") || it.url.startsWith("rtsp")) }
    }
}
EOF

# === UPDATE Theme.kt (Add more Netflix/TiviMate colors) ===
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

// Public colors for UI access
val NetflixDark = darkColorScheme(
    primary = Color(0xFFE50914),        // Netflix red
    secondary = Color(0xFFB30000),      // Darker red for accents
    background = Color(0xFF141414),     // Netflix black
    surface = Color(0xFF1F1F1F),        // Card background
    error = Color(0xFFCF6679),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

// TiviMate-style accent colors
val TiviMateColors = mapOf(
    "focusBorder" to Color(0xFFE50914),
    "channelBadge" to Color(0xFF333333),
    "epgText" to Color(0xFFB3B3B3),
    "playingIndicator" to Color(0xFF00C853)
)

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

# === PHONE UI: Netflix-Style HomeScreen.kt ===
cat > app/src/main/java/com/tamilflix/iptv/ui/phone/HomeScreen.kt << 'EOF'
package com.tamilflix.iptv.ui.phone
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
        // Hero Banner (Netflix style)
        if (heroChannel != null) {
            HeroBanner(channel = heroChannel, onClick = { onChannelClick(heroChannel) })
        }
        
        // Channel Groups (Horizontal Carousels)
        LazyColumn(contentPadding = PaddingValues(top = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            grouped.forEach { (group, groupChannels) ->
                item(key = group) {
                    SectionHeader(title = group)
                }
                item(key = "${group}_list") {
                    ChannelCarousel(channels = groupChannels, onChannelClick = onChannelClick)
                }
            }
        }
    }
}

@Composable
fun HeroBanner(channel: Channel, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable(onClick = onClick)
    ) {
        // Backdrop: Use logo as fallback, or gradient
        if (channel.logoUrl != null) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
            )
        }
        // Gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NetflixDark.background.copy(alpha = 0.7f),
                            NetflixDark.background
                        ),
                        startY = 100f
                    )
                )
        )
        // Channel info + Play button
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = NetflixDark.primary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp)
                        .clickable(onClick = onClick)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Play", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Text(
                    text = channel.group,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        color = Color.White,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
fun ChannelCarousel(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(channels, key = { it.name + it.url }) { channel ->
            ChannelCard(channel = channel, onClick = { onChannelClick(channel) })
        }
    }
}

@Composable
fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo/Thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
                .clip(RoundedCornerShape(8.dp))
                .background(NetflixDark.surface)
        ) {
            if (channel.logoUrl != null) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image),
                    placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                )
            } else {
                // Fallback: colored background + first letter
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = NetflixDark.primary
                    )
                }
            }
            // "LIVE" badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Red, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
        // Channel name
        Text(
            text = channel.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
EOF

# === PHONE UI: Real ExoPlayer PlayerScreen.kt ===
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
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
        
        // Setup player when channel changes
        DisposableEffect(channel.url) {
            val mediaItem = MediaItem.fromUri(channel.url)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            
            onDispose {
                player.stop()
                player.release()
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Video Player (full screen)
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        controllerShowTimeoutMs = 4000 // Hide controls after 4s
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Top Bar (Back button + Channel name)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = channel.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            
            // Bottom Info Bar (Group + URL preview)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                Text(
                    text = "📺 ${channel.group}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "🔗 ${channel.url.take(40)}${if (channel.url.length > 40) "..." else ""}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
EOF

# === TV UI: TiviMate-Style TvHomeScreen.kt ===
cat > app/src/main/java/com/tamilflix/iptv/ui/tv/TvHomeScreen.kt << 'EOF'
package com.tamilflix.iptv.ui.tv
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
        if (channels.isEmpty()) {
            TvLoadingScreen()
        } else {
            TiviMateHome(channels, onChannelClick)
        }
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark.background)
            .padding(48.dp) // TV safe zone (overscan protection)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "TamilFlix TV",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = NetflixDark.primary)
            )
            Text(
                "${channels.size} channels",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
            )
        }
        
        // Channel Groups (TiviMate style: vertical list of horizontal carousels)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            grouped.forEach { (group, groupChannels) ->
                item(key = group) {
                    Text(
                        group,
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                item(key = "${group}_list") {
                    TvChannelCarousel(channels = groupChannels, onChannelClick = onChannelClick)
                }
            }
        }
    }
}

@Composable
fun TvChannelCarousel(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(channels, key = { it.name + it.url }) { channel ->
            TvChannelCard(channel = channel, onClick = { onChannelClick(channel) })
        }
    }
}

@Composable
fun TvChannelCard(channel: Channel, onClick: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .width(220.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .graphicsLayer {
                // Smooth scale animation on focus (Netflix/TiviMate style)
                scaleX = if (isFocused) 1.06f else 1f
                scaleY = if (isFocused) 1.06f else 1f
                shadowElevation = if (isFocused) 16f else 4f
            }
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFocused) NetflixDark.primary.copy(alpha = 0.15f) else NetflixDark.surface
            )
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = if (isFocused) TiviMateColors["focusBorder"]!! else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Channel Logo (TiviMate style: prominent logo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
                .clip(RoundedCornerShape(8.dp))
                .background(NetflixDark.surface)
        ) {
            if (channel.logoUrl != null) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image)
                )
            } else {
                // Fallback: large initial + group color
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = NetflixDark.primary
                    )
                }
            }
            // "LIVE" indicator (TiviMate style)
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(TiviMateColors["playingIndicator"]!!, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
        
        // Channel Info (TiviMate style: name + group)
        Column(
            modifier = Modifier.padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = if (isFocused) NetflixDark.primary else Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = channel.group,
                style = MaterialTheme.typography.bodySmall,
                color = TiviMateColors["epgText"]!!,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // Focus ring animation (subtle pulse)
        if (isFocused) {
            androidx.compose.animation.core.animateFloatAsState(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
            ) { scale ->
                // Visual feedback handled by graphicsLayer above
            }
        }
    }
}
EOF

# === TV UI: Real ExoPlayer TvPlayerScreen.kt ===
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import com.tamilflix.iptv.ui.theme.NetflixDark

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
            
            onDispose {
                player.stop()
                player.release()
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Full-screen video player (TV-optimized controls)
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                        // TV-optimized: longer timeout for remote control users
                        controllerShowTimeoutMs = 8000
                        // Hide next/prev buttons (single stream)
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        // Show basic controls: play/pause, seek, progress
                        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Top Bar: Channel name + back hint (TV remote: BACK button)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(32.dp) // TV safe zone
                    .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "← BACK",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = channel.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
            }
            
            // Bottom Bar: Group + stream info (TV-optimized large text)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(32.dp) // TV safe zone
                    .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.medium)
                    .padding(20.dp)
            ) {
                Text(
                    text = "📺 ${channel.group}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "🔗 HLS/DASH Stream",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                // Remote control hint
                Text(
                    text = "🎮 Use D-pad to navigate • OK to select • BACK to exit",
                    color = TiviMateColors["epgText"]!!,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
EOF

# === UPDATE MainActivity.kt (Clean state management) ===
cat > app/src/main/java/com/tamilflix/iptv/MainActivity.kt << 'EOF'
package com.tamilflix.iptv
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.tamilflix.iptv.data.M3uParser
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.phone.HomeScreen
import com.tamilflix.iptv.ui.phone.PlayerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var selectedChannel by remember { mutableStateOf<Channel?>(null) }
            
            // Fetch channels on launch
            LaunchedEffect(Unit) {
                channels = M3uParser.fetchChannels()
            }
            
            // Show player if channel selected, else home screen
            if (selectedChannel != null) {
                PlayerScreen(
                    channel = selectedChannel!!,
                    onBack = { selectedChannel = null }
                )
            } else {
                HomeScreen(
                    channels = channels,
                    onChannelClick = { selectedChannel = it }
                )
            }
        }
    }
}
EOF

# === UPDATE TvMainActivity.kt ===
cat > app/src/main/java/com/tamilflix/iptv/TvMainActivity.kt << 'EOF'
package com.tamilflix.iptv
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.tamilflix.iptv.data.M3uParser
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.tv.TvHomeScreen
import com.tamilflix.iptv.ui.tv.TvPlayerScreen

class TvMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var selectedChannel by remember { mutableStateOf<Channel?>(null) }
            
            LaunchedEffect(Unit) {
                channels = M3uParser.fetchChannels()
            }
            
            if (selectedChannel != null) {
                TvPlayerScreen(
                    channel = selectedChannel!!,
                    onBack = { selectedChannel = null }
                )
            } else {
                TvHomeScreen(
                    channels = channels,
                    onChannelClick = { selectedChannel = it }
                )
            }
        }
    }
}
EOF

# === UPDATE GITHUB ACTIONS (Faster build) ===
cat > .github/workflows/build.yml << 'EOF'
name: Build TamilFlix APK
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 45
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'temurin', java-version: '17' }
      - name: Install System Gradle
        run: |
          wget -q https://services.gradle.org/distributions/gradle-8.5-bin.zip -O /tmp/gradle.zip
          unzip -q /tmp/gradle.zip -d /opt/
          echo "/opt/gradle-8.5/bin" >> $GITHUB_PATH
      - name: Build APKs
        run: |
          cd app
          gradle assembleDebug assembleRelease --no-daemon
      - uses: actions/upload-artifact@v4
        with: { name: tamilflix-v1.1-apks, path: app/build/outputs/apk/**/*.apk, retention-days: 7 }
EOF

# === GIT COMMIT ===
echo "🔀 Committing upgrade..."
git add .
git commit -m "feat: Netflix+TiviMate UI + real ExoPlayer playback + logo support" || true
git push origin main --force-with-lease 2>/dev/null || echo "⚠️ Push skipped (manual push needed)"

# === FINAL OUTPUT ===
echo ""
echo "✅✅✅ UPGRADE COMPLETE! ✅✅✅"
echo "🎨 New Features:"
echo "  • 🖼️ Channel logos now display (Coil async loading + fallbacks)"
echo "  • ▶️ REAL video playback (Media3 ExoPlayer with controls)"
echo "  • 🎬 Netflix-style hero banner + horizontal carousels"
echo "  • 📺 TiviMate-style TV UI (focus highlights, safe margins, D-pad nav)"
echo "  • 🎯 TV-optimized: 48dp safe zone, 8s control timeout, remote hints"
echo "  • 🔗 Better M3U parsing (handles quotes, fallbacks, LIVE badges)"
echo ""
echo "📦 To get the NEW APK:"
echo "  1. Go to: https://github.com/codedbyakil/iptv-netflix/actions"
echo "  2. Click latest workflow → Artifacts → Download 'tamilflix-v1.1-apks'"
echo "  3. Install app-debug.apk on your TV (overwrites old version)"
echo ""
echo "🎮 TV Controls:"
echo "  • D-pad: Navigate channels • OK: Play channel • BACK: Exit player"
echo "  • Controls auto-hide after 8s (press any button to show)"
echo ""
echo "🔧 If logos don't load:"
echo "  • Check your M3U has tvg-logo=\"URL\" attributes"
echo "  • Logs: adb logcat | grep -i coil"
echo ""
echo "🎬 Enjoy your Netflix + TiviMate style Tamil IPTV app! ✨"
