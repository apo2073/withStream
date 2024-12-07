package kr.apo2073.stream

import kr.apo2073.stream.config.ConnectionConfig.removeCconfig
import com.outstandingboy.donationalert.platform.Toonation
import kr.apo2073.stream.cmds.Admin
import kr.apo2073.stream.cmds.ChannelCmds
import kr.apo2073.stream.cmds.DonationEvent
import kr.apo2073.stream.cmds.Reload
import kr.apo2073.stream.events.ChzzkListener
import me.taromati.afreecatv.AfreecatvAPI
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import xyz.r2turntrue.chzzk4j.Chzzk
import xyz.r2turntrue.chzzk4j.ChzzkBuilder
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import java.util.*

lateinit var chzzk: Chzzk
lateinit var cht: MutableMap<UUID, ChzzkChat>
lateinit var tn: MutableMap<UUID, Toonation>
lateinit var af:MutableMap<UUID, AfreecatvAPI>
class Stream : JavaPlugin() {
    companion object { lateinit var instance: Stream }

    override fun onEnable() {
        instance=this

        logger.info("""
                                
                        
            .__  __  .__      _________ __                                 
    __  _  _|__|/  |_|  |__  /   _____//  |________   ____ _____    _____  
    \ \/ \/ /  \   __\  |  \ \_____  \\   __\_  __ \_/ __ \\__  \  /     \ 
     \     /|  ||  | |   Y  \/        \|  |  |  | \/\  ___/ / __ \|  Y Y  \
      \/\_/ |__||__| |___|  /_______  /|__|  |__|    \___  >____  /__|_|  /
                          \/        \/                   \/     \/      \/ 
       
                 
            §aVersion: v1.2
            §aAuthor: apo2073
            
        """.trimIndent())

        saveDefaultConfig()

        chzzk = ChzzkBuilder().build()
        cht = mutableMapOf()
        tn= mutableMapOf()
        af= mutableMapOf()

        ChannelCmds(this)
        Reload(this)
        DonationEvent(this)
        Admin(this)

        server.pluginManager.registerEvents(ChzzkListener(), this)
    }

    override fun onDisable() {
        for (player in Bukkit.getOnlinePlayers()) {
            val uuid=player.uniqueId
            cht[uuid]?.closeBlocking() ?: continue
        }
        removeCconfig()
        Bukkit.getScheduler().cancelTasks(this)
        this.server.scheduler.cancelTasks(this)
    }
}