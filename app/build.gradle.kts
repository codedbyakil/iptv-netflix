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
        versionCode = 9
        versionName = "2.3"
    }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}
dependencies {
    // Compose + Material 3
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.compose.foundation:foundation:1.5.4")
    
    // Media3 Core + Extensions (FIX: adds HLS/DASH/RTSP support)
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.2.1")  // HLS (.m3u8)
    implementation("androidx.media3:media3-exoplayer-dash:1.2.1") // DASH (.mpd)
    implementation("androidx.media3:media3-exoplayer-rtsp:1.2.1") // RTSP streams
    
    // Images + Utils
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
}
