package kr.apo2073.chzzk

import com.outstandingboy.donationalert.platform.Toonation
import kr.apo2073.chzzk.cmds.ChannelCmds
import kr.apo2073.chzzk.cmds.ReloadCmd
import kr.apo2073.chzzk.events.ChzzkListeners
import kr.apo2073.chzzk.events.PlayerEvent
import kr.apo2073.chzzk.util.PlaceHolder
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.r2turntrue.chzzk4j.Chzzk
import xyz.r2turntrue.chzzk4j.ChzzkBuilder
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import java.io.File
import java.util.*


lateinit var chzzk: MutableMap<UUID,Chzzk>
lateinit var cht: MutableMap<UUID,ChzzkChat>
lateinit var tn: MutableMap<UUID, Toonation>
class Chk : JavaPlugin() {
    override fun onEnable() {
        if (instance!=null) return
        instance=this

        logger.info("WithChzzk Made BY.아포칼립스")
        saveDefaultConfig()

        chzzk = mutableMapOf()
        cht = mutableMapOf()
        tn= mutableMapOf()

        ChannelCmds(this)
        ReloadCmd(this)
        PlayerEvent(this)

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            val placeholderExpansion = PlaceHolder(this)
            if (placeholderExpansion.placeholderAPI != null) {
                placeholderExpansion.register()
            }
        }
    }

    fun ChkBuilder(uuid:UUID, id: String) {
        chzzk[uuid]=ChzzkBuilder()
            .build() ?: return
        val chz= chzzk[uuid] ?: return
        var cht= cht[uuid]
        val player=Bukkit.getPlayer(uuid) ?:return
        val ch= chz.getChannel(id) ?: return
        val file= File("${this.dataFolder}/chzzk_channel", "${uuid}.yml")
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        try {
            cht = chz.chat(id)?.withChatListener(ChzzkListeners(this))?.build()
            cht?.connectBlocking() ?: return
            player.sendMessage(Component.text("§l[§a*§f]§r 채널 ${ch.channelName}( ${ch.followerCount} 팔로워 )에 연결했습니다."))
            config.save(file)

        } catch (e:Exception) {
            player.sendMessage(Component.text(e.message.toString()))
        }
    }

    companion object {
        var instance: Chk?= null
    }

    override fun onDisable() {
        for (player in Bukkit.getOnlinePlayers()) {
            val uuid=player.uniqueId
            cht[uuid]?.closeBlocking() ?: continue
        }
    }
}
