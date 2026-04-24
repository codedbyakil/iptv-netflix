package com.tamilflix.iptv.data
import com.tamilflix.iptv.data.models.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object M3uParser {
    private const val M3U_URL = "https://raw.githubusercontent.com/codedbyakil/Tamil-TV/refs/heads/main/local.m3u"
    
    suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val content = URL(M3U_URL).readText()
            parseM3uContent(content)
        } catch (e: Exception) { emptyList() }
    }
    
    private fun parseM3uContent(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        var name = ""
        var group = "Local Channels"
        var logo: String? = null
        
        content.lineSequence().forEach { line ->
            when {
                line.startsWith("#EXTINF:") -> {
                    name = Regex(""",\s*([^\r\n]+)""").find(line)?.groupValues?.get(1)?.trim() ?: "Unknown"
                    group = Regex("""group-title="([^"]+)""").find(line)?.groupValues?.get(1) ?: "Local Channels"
                    logo = Regex("""tvg-logo="([^"]+)""").find(line)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
                }
                line.startsWith("http") && !line.startsWith("#") -> {
                    if (name.isNotBlank()) {
                        channels.add(Channel(name, line.trim(), group, logo))
                    }
                    name = ""
                    logo = null
                }
            }
        }
        return channels.filter { it.url.isNotBlank() && it.url.startsWith("http") }
    }
}
