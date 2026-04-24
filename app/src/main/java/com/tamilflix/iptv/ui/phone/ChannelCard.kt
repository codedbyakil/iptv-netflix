package com.tamilflix.iptv.ui.phone
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tamilflix.iptv.data.models.Channel

@Composable
fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "pressScale")
    
    Column(
        modifier = Modifier
            .width(140.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                onClick = onClick,
                indication = null, // Disable default ripple for custom animation
                interactionSource = remember { androidx.compose.interaction.MutableInteractionSource() }
                    .also { source ->
                        // Track press state for scale animation
                        androidx.compose.animation.core.LaunchedEffect(source) {
                            source.interactions.collect { interaction ->
                                when (interaction) {
                                    is androidx.compose.foundation.interaction.PressInteraction.Press -> isPressed = true
                                    is androidx.compose.foundation.interaction.PressInteraction.Release,
                                    is androidx.compose.foundation.interaction.PressInteraction.Cancel -> isPressed = false
                                }
                            }
                        }
                    }
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo container with smooth corner radius
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            if (channel.logoUrl != null) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    // Smooth fade-in animation
                    alignment = Alignment.Center
                )
            } else {
                // Fallback: gradient background + initial
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // LIVE badge with pulse animation
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color(0xFFE53935), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
        // Channel name with smooth truncation
        Text(
            text = channel.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
        // Group tag (subtle)
        Text(
            text = channel.group,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
