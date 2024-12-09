package kr.apo2073.stream.events

import kr.apo2073.stream.Stream
import kr.apo2073.stream.chzzk
import kr.apo2073.stream.config.ConfigManager.getConfig
import kr.apo2073.stream.config.ConnectionConfig.getConnectionConfig
import kr.apo2073.stream.utilities.Donators
import kr.apo2073.stream.utilities.versions.Managers.performCommandAsOP
import kr.apo2073.stream.utilities.versions.Managers.prefix
import kr.apo2073.stream.utilities.versions.Managers.sendMessage
import kr.apo2073.stream.utilities.versions.Managers.showTitle
import me.taromati.afreecatv.event.implement.DonationChatEvent
import me.taromati.afreecatv.event.implement.MessageChatEvent
import me.taromati.afreecatv.listener.AfreecatvListener
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import java.util.*

class AfreecaListener : AfreecatvListener {

    private val stream = Stream.instance
    private val enableColor = stream.config.getBoolean("color")
    private val isChattingEnabled = stream.config.getBoolean("채팅")
    private val isDonationEnabled = stream.config.getBoolean("후원")
    val BoP = stream.config.getBoolean("BoolOrPay")

    private fun getChannelName(config: FileConfiguration?, chatChannelId: String?): String {
        return config?.getString("afreeca.channelName")?.replace("&", "§")
            ?: (chzzk.getChannel(chatChannelId)?.channelName ?: "알 수 없음")
    }

    private fun getPlatformName(): String {
        val platform = if (stream.config.getBoolean("en")) "Afreeca" else "아프리카"
        return if (enableColor) "§9$platform§f" else platform
    }

    override fun onMessageChat(e: MessageChatEvent) {
        stream.reloadConfig()
        if (!isChattingEnabled) return

        val uuid = UUID.fromString(getConnectionConfig().getString(e.channelId) ?: return) ?: return
        val config =getConfig(Bukkit.getPlayer(uuid) ?: return)
        val message = config.getString("message") ?: "streamer"
        stream.reloadConfig()

        var chatFormat = stream.config.getString("chat.format") ?: "{user} : {msg}"

        chatFormat = chatFormat
            .replace("&", "§")
            .replace("{msg}", e.message)
            .replace("{user}", e.nickname ?: "[ 익명 ]")
            .replace("{plat}", getPlatformName())
            .replace(Regex("\\{[^}]*}"), "§7(이모티콘)§f").trim()

        val channelName = "§l[ §r${getChannelName(config, e.channelId)} §f§l]§r"

        if (message.contains("streamer")) {
            val player = Bukkit.getPlayer(uuid) ?: return
            sendMessage(Component.text("$channelName$chatFormat"), player)
        } else {
            Bukkit.getOnlinePlayers().forEach { sendMessage(Component.text("$channelName$chatFormat"), it) }
        }
    }

    override fun onDonationChat(e: DonationChatEvent) {
        stream.reloadConfig()
        if (!stream.config.getBoolean("후원")) return

        val uuid = UUID.fromString(getConnectionConfig().getString(e.channelId) ?: return) ?: return
        val config = getConfig(Bukkit.getPlayer(uuid) ?: return)
        val message = config.getString("message").toString()
        val channelName = "§l[ §r${getChannelName(config, e.channelId)} §f§l]§r"

        fun getAmount():String {
            if (BoP) {
                return e.balloonAmount.toString()
            } else {
                return e.payAmount.toString()
            }
        }
        stream.reloadConfig()

        val donationF = stream.config.getString("donation.format")
            ?.replace("&", "§")
            ?.replace("{msg}", e.message)
            ?.replace("{user}", e.nickname ?: "[ 익명 ]")
            ?.replace("{paid}", getAmount())
            ?.replace(Regex("\\{[^}]*}"), "(이모티콘)")?.trim()
            ?.replace("{plat}", getPlatformName())

        if (message.contains("streamer")) {
            val player = Bukkit.getPlayer(uuid) ?: return
            sendMessage(prefix.append(Component.text("$channelName$donationF")), player)

            val donationT = stream.config.getString("donation.tformat")
                ?.replace("&", "§")
                ?.replace("{msg}", e.message)
                ?.replace("{user}", e.nickname ?: "[ 익명 ]")
                ?.replace("{paid}", getAmount())
                ?.replace("{plat}",getPlatformName())
                ?: ""
            showTitle("", donationT, player)
        } else {
            Bukkit.getOnlinePlayers().forEach { sendMessage(Component.text("$channelName$donationF"), it) }
        }
        

        val player = Bukkit.getPlayer(uuid) ?: return
        val eventCommand = stream.config.getString("donation-event.${e.payAmount}") ?: return
        val parsedCommand = eventCommand
            .replace("{player}", player.name)
            .replace("{msg}", e.message)
            .replace("{paid}", getAmount())
            .replace("{streamer}", player.name)

        if (parsedCommand.startsWith("$")) {
            val command = parsedCommand.removePrefix("$")
            Bukkit.dispatchCommand(stream.server.consoleSender, command)
        } else {
            player.performCommandAsOP(parsedCommand)
        }
        
        Donators().addDonator(player, e.nickname, e.payAmount)
    }
}
