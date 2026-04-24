package com.tamilflix.iptv.ui.tv
import androidx.compose.runtime.Composable
import com.tamilflix.iptv.data.models.Channel
@Composable fun TvHomeScreen(channels: List<Channel>, onChannelClick: (Channel) -> Unit) { /* TODO: TV carousel */ }
@Composable fun TvPlayerScreen(channel: Channel, onBack: () -> Unit) { /* TODO: TV ExoPlayer */ }
