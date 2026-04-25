package com.tamilflix.iptv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
    val rotation by animateFloatAsState(targetValue = if (visible) 180f else 0f, animationSpec = tween(1000), label = "hourglassRotate")
    
    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        onFinish()
    }
    
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Simple hourglass animation (Compose-native)
            Box(modifier = Modifier.size(130.dp).background(Color(0xFF473C3C), shape = RoundedCornerShape(65.dp)).padding(16.dp)) {
                Box(modifier = Modifier.align(Alignment.Center).rotate(rotation).size(50.dp)) {
                    // Top cap
                    Box(modifier = Modifier.align(Alignment.TopCenter).size(44.dp, 6.dp).background(Color(0xFF999999), shape = RoundedCornerShape(3.dp)))
                    // Glass top
                    Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-25).dp).size(44.dp, 28.dp).background(Color(0xFF999999), shape = RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp)))
                    // Glass bottom
                    Box(modifier = Modifier.align(Alignment.BottomCenter).offset(y = 25.dp).size(44.dp, 28.dp).background(Color(0xFF999999), shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)))
                    // Sand stream (simple animated box)
                    if (visible) {
                        Box(modifier = Modifier.align(Alignment.Center).size(3.dp, 35.dp).background(Color.White).offset(y = (-17).dp))
                    }
                    // Sand fill (simple)
                    Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-8).dp).size(39.dp, 17.dp).background(Color.White, shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)))
                }
            }
            Text("TamilFlix TV", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE50914)), modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
            Text("coded by akil", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBDBDBD)))
        }
    }
}
