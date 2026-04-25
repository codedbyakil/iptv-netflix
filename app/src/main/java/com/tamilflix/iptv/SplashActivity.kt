package com.tamilflix.iptv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TamilFlixSplash { startActivity(Intent(this, MainActivity::class.java)); finish() } }
    }
}

@Composable
fun TamilFlixSplash(onFinish: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val fadeAlpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(800), label = "fade")
    LaunchedEffect(Unit) { visible = true; delay(2000); onFinish() }
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "TamilFlix TV", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp, color = Color(0xFFE50914)), modifier = Modifier.graphicsLayer { alpha = fadeAlpha })
            if (visible) { CircularProgressIndicator(color = Color(0xFFE50914), strokeWidth = 3.dp, modifier = Modifier.padding(top = 24.dp).size(40.dp).graphicsLayer { alpha = fadeAlpha }) }
        }
        Text(text = " 𝒞☯𝒹𝑒𝒹 𝐵𝓎 𝒜𝒦𝐼𝐿 ", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBDBDBD), fontWeight = FontWeight.Medium), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp).graphicsLayer { alpha = fadeAlpha })
    }
}
