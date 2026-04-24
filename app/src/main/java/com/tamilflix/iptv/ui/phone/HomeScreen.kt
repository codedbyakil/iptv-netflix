package com.tamilflix.iptv.ui.phone
import androidx.compose.runtime.Composable
import com.tamilflix.iptv.data.models.Channel
@Composable fun HomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) { /* TODO: Netflix carousel */ }
@Composable fun PlayerScreen(channel: Channel, onBack: () -> Unit) { /* TODO: ExoPlayer */ }
