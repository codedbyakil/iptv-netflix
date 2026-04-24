package com.tamilflix.iptv.ui.settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tamilflix.iptv.ui.theme.AppTheme
@Composable fun SettingsScreen(dark: Boolean, onToggle: () -> Unit, onBack: () -> Unit) { AppTheme(dark) { Column { Row { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }; Text("Settings") }; Switch(checked = dark, onCheckedChange = { onToggle() }) } } }
