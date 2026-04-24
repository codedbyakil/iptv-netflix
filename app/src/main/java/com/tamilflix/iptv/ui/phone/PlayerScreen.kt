package com.tamilflix.iptv.ui.phone
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tamilflix.iptv.data.models.Channel
import com.tamilflix.iptv.ui.theme.TamilFlixTheme
import com.tamilflix.iptv.ui.theme.NetflixDark

@Composable
fun PlayerScreen(channel: Channel, onBack: () -> Unit) {
    TamilFlixTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Now Playing", style = MaterialTheme.typography.titleLarge.copy(color = NetflixDark.primary))
                Text(channel.name, style = MaterialTheme.typography.headlineMedium.copy(color = Color.White))
                Text("(ExoPlayer integration - coming soon)", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            }
            FloatingActionButton(onClick = onBack, containerColor = NetflixDark.primary, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) { 
                Text("←", color = Color.White) 
            }
        }
    }
}
