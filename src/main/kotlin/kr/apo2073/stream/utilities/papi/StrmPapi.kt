package kr.apo2073.stream.utilities.papi

import kr.apo2073.stream.Stream
import kr.apo2073.stream.config.ConfigManager.getConfig
import kr.apo2073.stream.utilities.Donators
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class StrmPapi: PlaceholderExpansion() {
    private val platforms= listOf("youtube", "chzzk", "afreeca", "toonation")
    override fun getIdentifier(): String ="stream"
    override fun getAuthor(): String = "아포칼립스"
    override fun getVersion(): String = Stream.instance.getVersion()

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player==null) return null
        if (params == "connection") {
            return getConnections(player)
        }
        val config= getConfig(player)
        for (platform in platforms) {
            if (params == platform) {
                return config.getString("$platform.channelName")
            }
            if (params=="${platform}_owner") {
                return config.getString("$platform.owner")
            }
            if (params=="${platform}_key") {
                return config.getString("$platform.channelID")
            }
        }
        if (params.contains("donator")) {
            val author=params.replace("donator_", "")
            return Donators().getDonator(player, author)
        }
        if (params=="donators") {
            return Donators().getDonators(player)
        }
        return null
    }
    
    private fun getConnections(player: Player): String {
        val config= getConfig(player)
        val list= mutableListOf<String>()
        platforms.forEach { 
            if (config.get(it)!=null) {
                list.add(it)
            }
        }
        return list.joinToString(", ")
    }
}