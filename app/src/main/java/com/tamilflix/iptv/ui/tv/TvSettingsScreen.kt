package com.tamilflix.iptv.ui.tv

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tamilflix.iptv.ui.theme.TamilFlixTvTheme

@Composable
fun TvSettingsScreen(onBack: () -> Unit) {
    TamilFlixTvTheme {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(48.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = onBack, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) { Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) { Text("← Back", style = MaterialTheme.typography.bodyLarge) } }
            }
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) { Column(modifier = Modifier.padding(24.dp)) { Text("Settings", style = MaterialTheme.typography.titleLarge); Text("TamilFlix TV v3.0", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp)); Text("Optimized for Android TV", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
}
