package kr.apo2073.stream.util

import kr.apo2073.stream.Stream
import kr.apo2073.stream.util.events.ChzzkChatEvent
import kr.apo2073.stream.util.events.ChzzkDonationEvent
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import xyz.r2turntrue.chzzk4j.chat.ChatEventListener
import xyz.r2turntrue.chzzk4j.chat.ChatMessage
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import xyz.r2turntrue.chzzk4j.chat.DonationMessage
import java.util.*

class ChzzkEvents: ChatEventListener {
    private var strm=Stream.instance!!

    override fun onChat(msg: ChatMessage, chat: ChzzkChat) {
        val uuid = UUID.fromString(Cconfig.getString(chat.channelId) ?: return)
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getPluginManager().callEvent(ChzzkChatEvent(msg, chat, Bukkit.getPlayer(uuid) ?: run {
                    this.cancel()
                    return
                }))
            }
        }.runTask(strm)

    }

    override fun onDonationChat(msg: DonationMessage, chat: ChzzkChat) {
        val uuid = UUID.fromString(Cconfig.getString(chat.channelId) ?: return)
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getPluginManager().callEvent(ChzzkDonationEvent(msg, chat, Bukkit.getPlayer(uuid) ?: run {
                    this.cancel()
                    return
                }))
            }
        }
    }

    override fun onError(ex: Exception) {
        ex.printStackTrace()
    }
}