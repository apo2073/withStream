package kr.apo2073.stream.events

import kr.apo2073.stream.Stream
import kr.apo2073.stream.chzzk
import kr.apo2073.stream.config.ConfigManager.getConfig
import kr.apo2073.stream.util.Managers.performCommandAsOP
import kr.apo2073.stream.util.Managers.sendMessage
import kr.apo2073.stream.util.Managers.showTitle
import kr.apo2073.stream.util.events.ChzzkChatEvent
import kr.apo2073.stream.util.events.ChzzkDonationEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChzzkListener : Listener {
    private val stream = Stream.instance
    private val enableColor = stream.config.getBoolean("color")
    private val isChattingEnabled = stream.config.getBoolean("채팅")
    private val isDonationEnabled = stream.config.getBoolean("후원")

    private fun getChannelName(config: FileConfiguration?, chatChannelId: String?): String {
        return config?.getString("chzzk.channelName")?.replace("&", "§")
            ?: (chzzk.getChannel(chatChannelId)?.channelName ?: "알 수 없음")
    }

    private fun getPlatformName(): String {
        val platform = if (stream.config.getBoolean("en")) "Chzzk" else "치지직"
        return if (enableColor) "§a$platform§f" else platform
    }

    @EventHandler
    fun onChat(event: ChzzkChatEvent) {
        val player = event.player ?: return
        if (!isChattingEnabled) return

        try {
            val config = getConfig(player)
            val channelName = "§l[ §r${getChannelName(config, event.chat.channelId)} §f§l]§r"

            var chatFormat = stream.config.getString("chat.format") ?: "&f {user} : {msg} &7- &r&l[ {plat} ]"

            chatFormat = chatFormat.replace("&", "§")
                .replace("{msg}", event.message.content)
                .replace(
                    "{user}",
                    event.message.profile?.nickname ?: "[ 익명 ]"
                )
                .replace("{plat}", getPlatformName())
                .replace(Regex("\\{[^}]*}"), "§7(이모티콘)§f").trim()

            val messageTarget = config.getString("message") ?: "streamer"
            if (messageTarget.contains("streamer")) {
                sendMessage(Component.text("$channelName$chatFormat"), player)
            } else {
                Bukkit.getOnlinePlayers().forEach { sendMessage(Component.text("$channelName$chatFormat"), it) }
            }
        } catch (e: Exception) {
            stream.logger.warning("${e.message}")
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onDonation(event: ChzzkDonationEvent) {
        val player = event.player ?: return
        val uuid = player.uniqueId
        if (!isDonationEnabled) return

        try {
            val config = getConfig(player)
            val channelName = "§l[ §r${getChannelName(config, event.chat.channelId)} §f§l]§r"

            var donationFormat = stream.config.getString("donation.format")
                ?: "&e{paid}원 &7>&f{user} &d:: &b{msg} &7- &r&l&6[ 후원 ]"
            donationFormat=donationFormat.replace("&", "§")
                .replace("{msg}", event.message.content)
                .replace("{user}", event.message.profile?.nickname ?: "[ 익명 ]")
                .replace("{paid}", event.message.payAmount.toString())
                .replace("{plat}", getPlatformName())
                .replace(Regex("\\{[^}]*}"), "(이모티콘)")


            val messageTarget = config.getString("message") ?: "streamer"
            if (messageTarget.contains("streamer")) {
                sendMessage(Component.text("$channelName$donationFormat"), player)

                val donationTitle = stream.config.getString("donation.tformat")
                    ?.replace("&", "§")
                    ?.replace("{msg}", event.message.content)
                    ?.replace("{user}", event.message.profile?.nickname ?: "[ 익명 ]")
                    ?.replace("{paid}", event.message.payAmount.toString())
                    ?.replace("{plat}", getPlatformName())
                    ?: ""
                showTitle("", donationTitle, player)
            } else {
                Bukkit.getOnlinePlayers().forEach { sendMessage(Component.text("$channelName$donationFormat"), it) }
            }

            val eventCommand = stream.config.getString("donation-event.${event.message.payAmount}") ?: return
            val parsedCommand = eventCommand
                .replace("{player}", player.name)
                .replace("{msg}", event.message.content)
                .replace("{paid}", event.message.payAmount.toString())
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
