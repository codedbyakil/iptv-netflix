package com.tamilflix.iptv.ui.settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tamilflix.iptv.ui.theme.AppTheme
@Composable fun SettingsScreen(dark: Boolean, onToggle: () -> Unit, onBack: () -> Unit) {
    AppTheme(dark) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                Text("Settings", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column { Text("Dark Mode", style = MaterialTheme.typography.bodyLarge); Text(if (dark) "Enabled" else "Disabled", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray) }
                Switch(checked = dark, onCheckedChange = { onToggle() })
            }
        }
    }
}
