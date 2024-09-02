package kr.apo2073.chzzk.util

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PlaceHolder(private val plugin: JavaPlugin): PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "chk"
    }

    override fun getAuthor(): String {
        return "APO2073"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }
    override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        if (player == null) {
            return null
        }

        if (identifier.equals("sponsor")) {
            val file= File("${plugin.dataFolder}/chzzk_channel", "${player.uniqueId}.yml")
            val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
            return config.getStringList("sponsor").joinToString(", ")
            //return "sponsor"
        }

        if (identifier.equals("donatedC")) {
            return chk.config.getStringList("donated-channel").joinToString(", ")
        }

        return null
    }
}
