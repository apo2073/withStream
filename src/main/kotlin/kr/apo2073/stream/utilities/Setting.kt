package kr.apo2073.stream.utilities

import kr.apo2073.stream.*
import kr.apo2073.stream.builders.yt
import kr.apo2073.stream.builders.yyt
import kr.apo2073.stream.cmds.Admin
import kr.apo2073.stream.cmds.ChannelCmds
import kr.apo2073.stream.cmds.DonationEvent
import kr.apo2073.stream.cmds.Reload
import kr.apo2073.stream.config.ConnectionConfig.removeCconfig
import kr.apo2073.stream.events.ChzzkListener
import kr.apo2073.stream.utilities.papi.StrmPapi
import org.bukkit.Bukkit
import xyz.r2turntrue.chzzk4j.ChzzkBuilder

class Setting(var strm:Stream) {
    fun onEnable() {
        strm.saveDefaultConfig()

        chzzk = ChzzkBuilder().build()
        cht = mutableMapOf()
        tn = mutableMapOf()
        af = mutableMapOf()
        yt = mutableMapOf()
        yyt = mutableMapOf()

        ChannelCmds(strm)
        Reload(strm)
        DonationEvent(strm)
        Admin(strm)

        strm.server.pluginManager.registerEvents(ChzzkListener(), strm)
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI")!=null) {
            StrmPapi().register()
        }
    }
    fun onDisable() {
        Bukkit.getScheduler().cancelTasks(strm)
        strm.server.scheduler.cancelTasks(strm)
        for (player in Bukkit.getOnlinePlayers()) {
            val uuid=player.uniqueId
            cht[uuid]?.closeBlocking().also { cht.remove(uuid) } ?: continue
            tn.remove(uuid)
            af.remove(uuid)
            yt[uuid]?.stop().also { yt.remove(uuid) } ?: continue
        }
        removeCconfig()
    }
}