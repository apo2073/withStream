package kr.apo2073.stream

import com.outstandingboy.donationalert.platform.Toonation
import kr.apo2073.stream.cmds.AdminCommand
import kr.apo2073.stream.cmds.ChannelCmds
import kr.apo2073.stream.cmds.DonationEventCmd
import kr.apo2073.stream.cmds.ReloadCmd
import kr.apo2073.stream.events.ChzzkListener
import kr.apo2073.stream.events.PlayerEvent
import kr.apo2073.stream.util.*
import me.taromati.afreecatv.AfreecatvAPI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.r2turntrue.chzzk4j.Chzzk
import xyz.r2turntrue.chzzk4j.ChzzkBuilder
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import java.io.File
import java.util.*

lateinit var chzzk: MutableMap<UUID, Chzzk>
lateinit var cht: MutableMap<UUID, ChzzkChat>
lateinit var tn: MutableMap<UUID, Toonation>
lateinit var af:MutableMap<UUID, AfreecatvAPI>
class Stream : JavaPlugin() {
    override fun onEnable() {
        if (instance!=null) return
        instance=this

        logger.info("""
            
 __      __.__  __  .__      _________ __                                 
/  \    /  \__|/  |_|  |__  /   _____//  |________   ____ _____    _____  
\   \/\/   /  \   __\  |  \ \_____  \\   __\_  __ \_/ __ \\__  \  /     \ 
 \        /|  ||  | |   Y  \/        \|  |  |  | \/\  ___/ / __ \|  Y Y  \
  \__/\  / |__||__| |___|  /_______  /|__|  |__|    \___  >____  /__|_|  /
       \/                \/        \/                   \/     \/      \/   By.apo2073
        """.trimIndent())
        saveDefaultConfig()
        DconfigReload()
        CconfigReload()

        chzzk = mutableMapOf()
        cht = mutableMapOf()
        tn= mutableMapOf()
        af= mutableMapOf()

        //DonateCMD(this) // 아이곰님 주문용
        ChannelCmds(this)
        ReloadCmd(this)
        PlayerEvent(this)
        DonationEventCmd(this)
        AdminCommand(this)
        
        server.pluginManager.registerEvents(ChzzkListener(), this)
    }

    fun ChkBuilder(uuid:UUID, id: String) {
        val player=Bukkit.getPlayer(uuid) ?:return
        try {
            chzzk[uuid]= ChzzkBuilder()
                .build()
            val chz= chzzk[uuid]
            var cht= cht[uuid]
            val ch= chz?.getChannel(id) // 문제
            cht = chz?.chat(id)?.withChatListener(ChzzkEventCall())?.build()
            cht?.connectBlocking()
            player.sendMessage(Component.text("§l[§a*§f]§r 채널 ${ch?.channelName}( ${ch?.followerCount} 팔로워 )에 연결했습니다."))

            val file= File("${this.dataFolder}/chzzk_channel", "${uuid}.yml")
            val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
            config.save(file)

        } catch (e:Exception) {
            player.sendMessage(Component.text("§l[§c*§f]§r ${e.message.toString()}"))
            Cconfig.set(id, null)
            connectionSave()
        }
    }

    companion object {
        var instance: Stream?= null
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
