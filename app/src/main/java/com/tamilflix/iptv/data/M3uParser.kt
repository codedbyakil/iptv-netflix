package com.tamilflix.iptv.data
import com.tamilflix.iptv.data.models.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
object M3uParser {
    suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        try {
            URL("https://raw.githubusercontent.com/codedbyakil/Tamil-TV/refs/heads/main/local.m3u").readText().lines().fold(mutableListOf<Channel>() to "" to "" to "") { (list, name, group, logo), line ->
                when {
                    line.startsWith("#EXTINF:") -> {
                        val n = line.substringAfterLast(",").trim().takeIf { it.isNotEmpty() } ?: name
                        val g = Regex("group-title=\"([^\"]+)\"").find(line)?.groupValues?.get(1) ?: group
                        val l = Regex("tvg-logo=\"([^\"]+)\"").find(line)?.groupValues?.get(1) ?: logo
                        list to n to g to l
                    }
                    line.startsWith("http") -> { if (name.isNotEmpty()) list.add(Channel(name, line, group, logo)); list to "" to group to logo }
                    else -> list to name to group to logo
                }
            }.first.filter { it.url.startsWith("http") }
        } catch (e: Exception) { emptyList() }
    }
}
