package kr.apo2073.stream.utilities

import kr.apo2073.stream.config.ConfigManager.getConfig
import kr.apo2073.stream.config.ConfigManager.setValue
import org.bukkit.entity.Player

class Donators {
    fun addDonator(player: Player, author:String, amount:Int) {
        setValue(player,"donators.$author", amount)
    }
    fun getDonator(player: Player, author: String): String? {
        return getConfig(player).getString("donators.$author")
    }
    fun getDonators(player: Player): String {
        val map= getConfig(player).getMapList("donators")
        return map.joinToString(", ") {it.keys.toString()}
    }
}