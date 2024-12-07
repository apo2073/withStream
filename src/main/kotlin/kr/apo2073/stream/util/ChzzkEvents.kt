package kr.apo2073.stream.util

import kr.apo2073.stream.config.ConnectionConfig.getConnectionConfig
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
import java.util.function.Consumer

class ChzzkEvents: ChatEventListener {
    private var strm=Stream.instance

    override fun onChat(msg: ChatMessage, chat: ChzzkChat) {
        strm.server.scheduler.runTask(strm, Runnable {
            val uuid = UUID.fromString(getConnectionConfig().getString(chat.channelId) ?: return@Runnable)
            Bukkit.getPluginManager().callEvent(ChzzkChatEvent(msg, chat, Bukkit.getPlayer(uuid) ?: run {
                return@Runnable
            }))
        })
    }

    override fun onDonationChat(msg: DonationMessage, chat: ChzzkChat) {
        strm.server.scheduler.runTask(strm, Runnable {
            val uuid = UUID.fromString(getConnectionConfig().getString(chat.channelId) ?: return@Runnable)
            Bukkit.getPluginManager().callEvent(ChzzkDonationEvent(msg, chat, Bukkit.getPlayer(uuid) ?: run {
                return@Runnable
            }))
        })
    }

    override fun onError(ex: Exception) {
        ex.printStackTrace()
    }
}