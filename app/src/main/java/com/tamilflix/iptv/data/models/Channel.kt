package com.tamilflix.iptv.data.models
data class Channel(val name: String, val url: String, val group: String = "Local Channels", val logoUrl: String? = null, val id: String? = null)
