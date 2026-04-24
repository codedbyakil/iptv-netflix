#!/bin/bash
set -e
echo "🎬 TamilFlix Setup - Starting (Fixed Compose Plugin)..."

# === CONFIG ===
REPO="iptv-netflix"
PKG="com.tamilflix.iptv"
M3U="https://raw.githubusercontent.com/codedbyakil/Tamil-TV/refs/heads/main/local.m3u"
cd /workspaces 2>/dev/null || cd ~

# === CREATE PROJECT STRUCTURE ===
echo "📁 Creating project structure..."
mkdir -p "$REPO" && cd "$REPO"
mkdir -p app/src/main/java/com/tamilflix/iptv/{data/models,ui/{theme,phone,tv},util}
mkdir -p app/src/main/res/{values,mipmap-anydpi-v26,drawable}
mkdir -p .github/workflows gradle/wrapper

# === ANDROIDMANIFEST.XML ===
cat > app/src/main/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-feature android:name="android.hardware.touchscreen" android:required="false"/>
    <uses-feature android:name="android.software.leanback" android:required="false"/>
    <application android:allowBackup="true" android:icon="@mipmap/ic_launcher" android:label="@string/app_name" android:theme="@style/Theme.TamilFlix" android:banner="@drawable/tv_banner" android:usesCleartextTraffic="true">
        <activity android:name=".MainActivity" android:exported="true"><intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/></intent-filter></activity>
        <activity android:name=".TvMainActivity" android:exported="true"><intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LEANBACK_LAUNCHER"/></intent-filter></activity>
    </application>
</manifest>
EOF

# === APP BUILD.GRADLE.KTS (FIXED: No compose compiler plugin - AGP handles it) ===
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
        versionCode = 1
        versionName = "1.0"
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
    kotlinOptions {
        jvmTarget = "17"
        // Enable Compose compiler via Kotlin options (AGP 8.2+ handles the rest)
        freeCompilerArgs += listOf(
            "-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
        )
    }
    buildFeatures {
        compose = true  // This enables Compose - no separate plugin needed with AGP 8.2+
    }
}
dependencies {
    // Compose BOM for version alignment
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    
    // Android TV
    implementation("androidx.tv:tv-material:1.0.0")
    implementation("androidx.tv:tv-foundation:1.0.0")
    
    // Video Playback (Media3)
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    
    // Images & Networking
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
}
EOF

# === ROOT BUILD.GRADLE.KTS (No compose plugin here either) ===
cat > build.gradle.kts << 'EOF'
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}
EOF

# === SETTINGS.GRADLE.KTS ===
cat > settings.gradle.kts << 'EOF'
pluginManagement { 
    repositories { 
        google() 
        mavenCentral() 
        gradlePluginPortal() 
    } 
}
dependencyResolutionManagement { 
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) 
    repositories { 
        google() 
        mavenCentral() 
    } 
}
rootProject.name = "TamilFlix"
include(":app")
EOF

# === GRADLE.PROPERTIES ===
cat > gradle.properties << 'EOF'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
android.enableJetifier=false
EOF

# === GRADLE WRAPPER (Properties only - system Gradle used in CI) ===
cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
networkTimeout=10000
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF
echo '#!/bin/sh' > gradlew
echo 'echo "Use system Gradle in CI: gradle build"' > gradlew
chmod +x gradlew

# === KOTLIN SOURCE FILES ===
cat > app/src/main/java/com/tamilflix/iptv/data/models/Channel.kt << 'EOF'
package com.tamilflix.iptv.data.models
data class Channel(val name: String, val url: String, val group: String = "Local Channels", val logoUrl: String? = null, val id: String? = null)
EOF

cat > app/src/main/java/com/tamilflix/iptv/data/M3uParser.kt << 'EOF'
package com.tamilflix.iptv.data
import com.tamilflix.iptv.data.models.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
object M3uParser {
    private const val M3U_URL = "'"$M3U"'"
    suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        try { val content = URL(M3U_URL).readText(); parseM3uContent(content) } catch (e: Exception) { emptyList() }
    }
    private fun parseM3uContent(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        var name = ""; var group = "Local Channels"; var logo: String? = null
        content.lineSequence().forEach { line ->
            when {
                line.startsWith("#EXTINF:") -> {
                    name = Regex(""",\s*([^\r\n]+)""").find(line)?.groupValues?.get(1)?.trim() ?: "Unknown"
                    group = Regex("""group-title="([^"]+)""").find(line)?.groupValues?.get(1) ?: "Local Channels"
                    logo = Regex("""tvg-logo="([^"]+)""").find(line)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
                }
                line.startsWith("http") && !line.startsWith("#") -> { if (name.isNotBlank()) channels.add(Channel(name, line.trim(), group, logo)); name = ""; logo = null }
            }
        }
        return channels.filter { it.url.isNotBlank() && it.url.startsWith("http") }
    }
}
EOF

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
private val NetflixDark = darkColorScheme(primary = Color(0xFFE50914), background = Color(0xFF141414), surface = Color(0xFF1F1F1F), onPrimary = Color.White, onBackground = Color.White, onSurface = Color.White)
@Composable fun TamilFlixTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = NetflixDark.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
    MaterialTheme(colorScheme = NetflixDark, content = content)
}
EOF

# === PLACEHOLDER UI FILES (Working Compose UI) ===
cat > app/src/main/java/com/tamilflix/iptv/ui/phone/HomeScreen.kt << 'EOF'
package com.tamilflix.iptv.ui.phone
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import com.tamilflix.iptv.ui.theme.NetflixDark
@Composable
fun HomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    TamilFlixTheme {
        Column(modifier = Modifier.fillMaxSize().background(NetflixDark.background)) {
            // Header
            Text("TamilFlix", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = NetflixDark.primary), modifier = Modifier.padding(16.dp))
            
            if (channels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NetflixDark.primary)
                        Text("Loading channels...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(channels.groupBy { it.group }.toList()) { (group, groupChannels) ->
                        Text(group, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(vertical = 8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(groupChannels) { channel ->
                                Surface(
                                    onClick = { onChannelClick(channel) },
                                    color = NetflixDark.surface,
                                    modifier = Modifier.width(140.dp).height(80.dp).padding(vertical = 4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                                        Text(channel.name, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PlayerScreen(channel: Channel, onBack: () -> Unit) {
    TamilFlixTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Now Playing", style = MaterialTheme.typography.titleLarge.copy(color = NetflixDark.primary))
                Text(channel.name, style = MaterialTheme.typography.headlineMedium.copy(color = Color.White))
                Text("URL: ${channel.url.take(50)}...", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                Text("(ExoPlayer integration - coming in next update)", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            }
            FloatingActionButton(onClick = onBack, containerColor = NetflixDark.primary, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) { 
                Text("←", color = Color.White, style = MaterialTheme.typography.titleLarge) 
            }
        }
    }
}
EOF
cp app/src/main/java/com/tamilflix/iptv/ui/phone/HomeScreen.kt app/src/main/java/com/tamilflix/iptv/ui/phone/PlayerScreen.kt

cat > app/src/main/java/com/tamilflix/iptv/ui/tv/TvHomeScreen.kt << 'EOF'
package com.tamilflix.iptv.ui.tv
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import com.tamilflix.iptv.ui.theme.NetflixDark
@Composable
fun TvHomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) {
    TamilFlixTheme {
        Column(modifier = Modifier.fillMaxSize().background(NetflixDark.background).padding(48.dp)) {
            Text("TamilFlix TV", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = NetflixDark.primary), modifier = Modifier.padding(bottom = 24.dp))
            if (channels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NetflixDark.primary)
                        Text("Loading channels...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    items(channels.groupBy { it.group }.toList()) { (group, groupChannels) ->
                        Text(group, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.SemiBold))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(groupChannels) { channel ->
                                Surface(
                                    onClick = { onChannelClick(channel) },
                                    color = NetflixDark.surface,
                                    modifier = Modifier.width(200.dp).height(100.dp).focusable().padding(vertical = 4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                        Text(channel.name, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun TvPlayerScreen(channel: Channel, onBack: () -> Unit) {
    TamilFlixTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Column(modifier = Modifier.padding(48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("TV: Now Playing", style = MaterialTheme.typography.titleLarge.copy(color = NetflixDark.primary))
                Text(channel.name, style = MaterialTheme.typography.headlineMedium.copy(color = Color.White))
                Text("(ExoPlayer TV integration - coming soon)", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            }
        }
    }
}
EOF
cp app/src/main/java/com/tamilflix/iptv/ui/tv/TvHomeScreen.kt app/src/main/java/com/tamilflix/iptv/ui/tv/TvPlayerScreen.kt

# === MAINACTIVITY.KT ===
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
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        setContent {
            var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
            var selected by remember { mutableStateOf<Channel?>(null) }
            LaunchedEffect(Unit) { channels = M3uParser.fetchChannels() }
            if (selected != null) PlayerScreen(channel = selected!!, onBack = { selected = null }) else HomeScreen(channels = channels, onChannelClick = { selected = it })
        }
    }
}
EOF

# === TVMAINACTIVITY.KT ===
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
            var selected by remember { mutableStateOf<Channel?>(null) }
            LaunchedEffect(Unit) { channels = M3uParser.fetchChannels() }
            if (selected != null) TvPlayerScreen(channel = selected!!, onBack = { selected = null }) else TvHomeScreen(channels = channels, onChannelClick = { selected = it })
        }
    }
}
EOF

# === RESOURCES ===
cat > app/src/main/res/values/strings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?><resources><string name="app_name">TamilFlix</string><string name="loading">Loading channels...</string><string name="error_load">Failed to load channels</string><string name="play">Play</string><string name="back">Back</string></resources>
EOF
cat > app/src/main/res/values/colors.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?><resources><color name="netflix_red">#FFE50914</color><color name="netflix_black">#FF141414</color><color name="netflix_surface">#FF1F1F1F</color></resources>
EOF
cat > app/src/main/res/values/themes.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?><resources><style name="Theme.TamilFlix" parent="android:Theme.Material.NoActionBar"><item name="android:statusBarColor">@color/netflix_black</item><item name="android:windowBackground">@color/netflix_black</item></style></resources>
EOF
cat > app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?><adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android"><background android:drawable="@color/netflix_red"/><foreground android:drawable="@drawable/ic_launcher_foreground"/></adaptive-icon>
EOF
cat > app/src/main/res/drawable/ic_launcher_foreground.xml << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108"><path android:pathData="M42,36 L72,54 L42,72 Z" android:fillColor="#FFFFFF"/></vector>
EOF
echo "TV Banner Placeholder (320x180px PNG)" > app/src/main/res/drawable/tv_banner.png

# === GITHUB ACTIONS WORKFLOW (System Gradle - Fixed Compose) ===
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
        with: { name: tamilflix-apks, path: app/build/outputs/apk/**/*.apk, retention-days: 7 }
EOF

# === GIT SETUP ===
echo "🔀 Setting up Git..."
git init -q 2>/dev/null || true
git add .
git commit -q -m "feat: TamilFlix - Fixed Compose plugin (AGP 8.2+ handles compose)"

# Handle remote sync
if git remote get-url origin >/dev/null 2>&1; then
    echo "🔄 Syncing with existing remote..."
    git pull origin main --allow-unrelated-histories --rebase=false 2>/dev/null || true
    git push -u origin main --force-with-lease
else
    echo "📤 Setting new remote..."
    git remote add origin "https://github.com/codedbyakil/$REPO.git" 2>/dev/null || true
    git push -u origin main
fi

# === FINAL OUTPUT ===
echo ""
echo "✅ SUCCESS! TamilFlix project created and pushed."
echo "🔧 Fixed: Removed compose compiler plugin (AGP 8.2+ handles Compose via buildFeatures)"
echo "📦 Next: Go to https://github.com/codedbyakil/$REPO/actions"
echo "⏱️  Wait ~3-5 minutes for APK build to complete"
echo "📥 Download: Actions → Artifacts → tamilflix-apks.zip"
echo "📺 Install on Android TV: Use 'Send Files to TV' app or adb install app-debug.apk"
echo ""
echo "🎨 Features included:"
echo "  • Netflix dark theme (#141414 bg + #E50914 red)"
echo "  • Phone + Android TV entry points (LEANBACK_LAUNCHER)"
echo "  • Auto-fetch M3U from your Tamil TV source"
echo "  • System Gradle CI (no wrapper JAR issues)"
echo "  • Direct plugin declarations + Compose via buildFeatures"
echo ""
echo "🔧 To add full ExoPlayer later: Replace PlayerScreen.kt with Media3 integration"
echo "🎬 Enjoy streaming Tamil channels on your TV! ✨"
