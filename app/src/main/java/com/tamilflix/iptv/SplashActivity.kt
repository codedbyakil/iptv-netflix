package com.tamilflix.iptv
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TamilFlix TV", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE50914)), modifier = Modifier.padding(bottom = 24.dp))
                    CircularProgressIndicator(color = Color(0xFFE50914))
                    Text("coded by akil", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBDBDBD)), modifier = Modifier.padding(top = 48.dp))
                }
            }
            LaunchedEffect(Unit) { delay(2000); startActivity(Intent(this@SplashActivity, MainActivity::class.java)); finish() }
        }
    }
}
