package kr.apo2073.stream.events

import kr.apo2073.stream.Stream
import kr.apo2073.stream.chzzk
import kr.apo2073.stream.config.ConfigManager.getConfig
import kr.apo2073.stream.config.ConnectionConfig.getConnectionConfig
import kr.apo2073.stream.util.Managers.performCommandAsOP
import kr.apo2073.stream.util.Managers.sendMessage
import kr.apo2073.stream.util.Managers.showTitle
import kr.apo2073.ytliv.data.Chatting
import kr.apo2073.ytliv.data.SuperChat
import kr.apo2073.ytliv.listener.YouTubeEventListener
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import java.util.*

class YouTubeListener: YouTubeEventListener {
    private val stream = Stream.instance
    private val enableColor = stream.config.getBoolean("color")
    private val isChattingEnabled = stream.config.getBoolean("채팅")
    private val isDonationEnabled = stream.config.getBoolean("후원")

    private fun getChannelName(config: FileConfiguration?, chatChannelId: String?): String {
        return config?.getString("youtube.channelName")?.replace("&", "§")
            ?: (chzzk.getChannel(chatChannelId)?.channelName ?: "알 수 없음")
    }

    private fun getPlatformName(): String {
        val platform = if (stream.config.getBoolean("en")) "Youtube" else "유튜브"
        return if (enableColor) "§c$platform§f" else platform
    }

    override fun onChat(chat: Chatting) {
        stream.reloadConfig()
        if (!isChattingEnabled) return

        val uuid = UUID.fromString(getConnectionConfig().getString(chat.videoId) ?: return) ?: return
        val config = getConfig(Bukkit.getPlayer(uuid) ?: return)
        val message = config.getString("message") ?: "streamer"

        var chatFormat = stream.config.getString("chat.format") ?: "{user} : {msg}"
        chatFormat = chatFormat
            .replace("&", "§")
            .replace("{msg}", chat.message)
            .replace("{user}", chat.author().name )
            .replace("{plat}", getPlatformName())
            .replace(Regex("\\{[^}]*}"), "§7(이모티콘)§f").trim()

        val channelName = "§l[ §r${getChannelName(config, chat.videoId)} §f§l]§r"

        if (message.contains("streamer")) {
            val player = Bukkit.getPlayer(uuid) ?: return
            sendMessage(Component.text("$channelName$chatFormat"), player)
        } else {
            Bukkit.getOnlinePlayers().forEach { sendMessage(Component.text("$channelName$chatFormat"), it) }
        }
    }

    override fun onSuperChat(superChat: SuperChat) {
        stream.reloadConfig()
        if (!isDonationEnabled) return

        val uuid = UUID.fromString(getConnectionConfig().getString(superChat.videoId) ?: return) ?: return
        val player = Bukkit.getPlayer(uuid) ?: return
        try {
            val config = getConfig(player)
            val channelName = "§l[ §r${config.getString("channelName")?.replace("&", "§") ?: return} §f§l]§r"

            var donationFormat = stream.config.getString("donation.format")
                ?: "&e{paid}원 &7>&f{user} &d:: &b{msg} &7- &r&l&6[ 후원 ]"
            donationFormat = donationFormat.replace("&", "§")
                .replace("{msg}", superChat.message)
                .replace("{user}", superChat.author().name)
                .replace("{paid}", superChat.amount)
                .replace("{plat}", getPlatformName())
                .replace(Regex("\\{[^}]*}"), "(이모티콘)")


            val messageTarget = config.getString("message") ?: "streamer"
            if (messageTarget.contains("streamer")) {
                sendMessage(Component.text("$channelName$donationFormat"), player)

                val donationTitle = stream.config.getString("donation.tformat")
                    ?.replace("&", "§")
                    ?.replace("{msg}", superChat.message)
                    ?.replace("{user}", superChat.author().name)
                    ?.replace("{paid}", superChat.amount)
                    ?.replace("{plat}", getPlatformName())
                    ?: ""
                showTitle("", donationTitle, player)
            } else {
                Bukkit.getOnlinePlayers().forEach { sendMessage(Component.text("$channelName$donationFormat"), it) }
            }

            val eventCommand = stream.config.getString("donation-event.${superChat.amount}") ?: return
            val parsedCommand = eventCommand
                .replace("{player}", player.name)
                .replace("{msg}", superChat.message)
                .replace("{paid}", superChat.amount)
                .replace("{streamer}", player.name)

            if (parsedCommand.startsWith("$")) {
                val command = parsedCommand.removePrefix("$")
                Bukkit.dispatchCommand(stream.server.consoleSender, command)
            } else {
                player.performCommandAsOP(parsedCommand)
            }
        } catch (e: Exception) {
            stream.logger.warning("오류 발생! - ${e.message}")
            e.printStackTrace()
        }
    }
}