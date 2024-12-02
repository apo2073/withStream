package kr.apo2073.stream.util

import kr.apo2073.stream.Stream
import kr.apo2073.stream.chzzk
import kr.apo2073.stream.events.performCommandAsOP
import kr.apo2073.stream.util.events.ChzzkChatEvent
import kr.apo2073.stream.util.events.ChzzkDonationEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.scheduler.BukkitRunnable
import xyz.r2turntrue.chzzk4j.chat.ChatEventListener
import xyz.r2turntrue.chzzk4j.chat.ChatMessage
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import xyz.r2turntrue.chzzk4j.chat.DonationMessage
import java.io.File
import java.time.Duration
import java.util.*

class ChzzkEventCall:ChatEventListener {
    private var strm=Stream.instance!!

    override fun onChat(msg: ChatMessage, chat: ChzzkChat) {
        val uuid = UUID.fromString(Cconfig.getString(chat.channelId)) ?: return
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getPluginManager().callEvent(ChzzkChatEvent(msg, chat, Bukkit.getPlayer(uuid)))
            }
        }.runTask(strm)

    }

    override fun onDonationChat(msg: DonationMessage, chat: ChzzkChat) {
        val uuid = UUID.fromString(Cconfig.getString(chat.channelId) ?: return) ?: return
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getPluginManager().callEvent(ChzzkDonationEvent(msg, chat, Bukkit.getPlayer(uuid)))
            }
        }
    }

    override fun onError(ex: Exception) {
        this.strm.logger.warning(ex.toString())
    }
}