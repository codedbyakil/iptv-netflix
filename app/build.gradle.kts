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
        versionCode = 3
        versionName = "1.2"
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
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.tv:tv-foundation:1.0.0-alpha10")
    implementation("androidx.tv:tv-material:1.0.0-alpha10")
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.1")
    // DataStore for settings
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
