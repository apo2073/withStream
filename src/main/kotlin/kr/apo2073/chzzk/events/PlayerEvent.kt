package kr.apo2073.chzzk.events

import kr.apo2073.chzzk.cht
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PlayerEvent(private var plugin: JavaPlugin):Listener {

    @EventHandler
    fun onPlayerQuitEvent(e:PlayerQuitEvent) {
        val uuid=e.player.uniqueId
        if (cht[uuid]!=null) {
            val file= File("${plugin.dataFolder}/chzzk_channel", "${e.player.uniqueId}.yml")
            cht[e.player.uniqueId]?.closeBlocking()
            cht[e.player.uniqueId]?.closeAsync()
            file.delete()
        }
    }
}