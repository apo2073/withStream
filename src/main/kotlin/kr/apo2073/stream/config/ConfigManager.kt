package kr.apo2073.stream.config

import kr.apo2073.stream.Stream
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException

object ConfigManager {
    private val CHANNEL_FOLDER = File(Stream.instance.dataFolder, "channel")

    init {
        if (!CHANNEL_FOLDER.exists()) {
            CHANNEL_FOLDER.mkdirs()
        }
    }

    private val configCache = mutableMapOf<Player, FileConfiguration>()

    private fun getConfigFile(player: Player): File {
        return File(CHANNEL_FOLDER, "${player.uniqueId}.yml")
    }

    fun getConfig(player: Player): FileConfiguration {
        return configCache.getOrPut(player) {
            val file = getConfigFile(player)
            try {
                if (!file.exists()) {
                    file.createNewFile()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            YamlConfiguration.loadConfiguration(file)
        }
    }

    fun saveConfig(player: Player) {
        try {
            val file = getConfigFile(player)
            val config = getConfig(player)
            config.save(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setValue(player: Player, path: String, value: Any) {
        val config = getConfig(player)
        config.set(path, value)
        saveConfig(player)
    }

    fun getValue(player: Player, path: String): Any? {
        return getConfig(player).get(path)
    }

    fun isExist(player: Player): Boolean {
        return getConfigFile(player).exists()
    }

    fun removeConfig(player: Player) {
        val file = getConfigFile(player)
        if (file.exists()) {
            file.delete()
            configCache.remove(player)
        }
    }
}


object ConnectionConfig {
    private val CONNECTION_FILE = File(Stream.instance.dataFolder, "channel/connection.yml")
    private var Cconfig = YamlConfiguration.loadConfiguration(CONNECTION_FILE)

    init {
        if (!CONNECTION_FILE.exists()) {
            CONNECTION_FILE.parentFile.mkdirs()
            CONNECTION_FILE.createNewFile()
        }
    }

    fun getConnectionConfig(): YamlConfiguration {
        return Cconfig
    }

    fun connectionSave() {
        try {
            Cconfig.save(CONNECTION_FILE)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setConnectionValue(path: String, value: Any?) {
        Cconfig.set(path, value)
        connectionSave()
    }

    fun getConnectionValue(path: String): Any? {
        return Cconfig.get(path)
    }

    fun removeCconfig() {
        if (CONNECTION_FILE.exists()) {
            CONNECTION_FILE.delete()
        }
    }
}
