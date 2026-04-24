package com.tamilflix.iptv.data
import com.tamilflix.iptv.data.models.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
object M3uParser {
    suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val content = URL("https://raw.githubusercontent.com/codedbyakil/Tamil-TV/refs/heads/main/local.m3u").readText()
            val channels = mutableListOf<Channel>()
            var name = ""
            var group = ""
            var logo: String? = null
            for (line in content.lines()) {
                if (line.startsWith("#EXTINF:")) {
                    name = line.substringAfterLast(",").trim().takeIf { it.isNotEmpty() } ?: "Unknown"
                    group = Regex("group-title=\"([^\"]+)\"").find(line)?.groupValues?.get(1) ?: "Local"
                    logo = Regex("tvg-logo=\"([^\"]+)\"").find(line)?.groupValues?.get(1)?.takeIf { it.isNotEmpty() }
                } else if (line.startsWith("http", ignoreCase = true) && name.isNotEmpty()) {
                    channels.add(Channel(name, line.trim(), group, logo))
                    name = ""
                    logo = null
                }
            }
            channels.filter { it.url.startsWith("http") }
        } catch (e: Exception) { emptyList() }
    }
}
