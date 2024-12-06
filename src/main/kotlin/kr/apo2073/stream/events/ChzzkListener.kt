package kr.apo2073.stream.events

import kr.apo2073.stream.Stream
import kr.apo2073.stream.chzzk
import kr.apo2073.stream.util.Dconfig
import kr.apo2073.stream.util.Managers.sendMessage
import kr.apo2073.stream.util.Managers.showTitle
import kr.apo2073.stream.util.events.ChzzkChatEvent
import kr.apo2073.stream.util.events.ChzzkDonationEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File
import java.util.*

class ChzzkListener : Listener {
    private val stream = Stream.instance!!
    private val enableColor = stream.config.getBoolean("color")
    private val isChattingEnabled = stream.config.getBoolean("채팅")
    private val isDonationEnabled = stream.config.getBoolean("후원")

    private fun loadConfigFile(uuid: String): FileConfiguration? {
        val file = File("${stream.dataFolder}/chzzk_channel", "$uuid.yml")
        if (!file.exists()) return null
        return YamlConfiguration.loadConfiguration(file)
    }

    private fun getChannelName(config: FileConfiguration?, uuid: UUID, chatChannelId: String?): String {
        return config?.getString("channelName")?.replace("&", "§")
            ?: (chzzk[uuid]?.getChannel(chatChannelId)?.channelName ?: "알 수 없음")
    }

    private fun getPlatformName(): String {
        val platform = if (stream.config.getBoolean("en")) "Chzzk" else "치지직"
        return if (enableColor) "§a$platform§f" else platform
    }

    @EventHandler
    fun onChat(event: ChzzkChatEvent) {
        val player = event.player ?: return
        val uuid = player.uniqueId
        if (!isChattingEnabled) return

        try {
            val config = loadConfigFile(uuid.toString()) ?: return
            val channelName = "§l[ §r${getChannelName(config, uuid, event.chat.channelId)} §f§l]§r"

            var chatFormat = config.getString("Chat-format")
                ?: stream.config.getString("chat.format") ?: "{user} : {msg}"

            val sponsorList = Dconfig.getStringList("sponsor")
            chatFormat = chatFormat.replace("&", "§")
                .replace("{msg}", event.message.content)
                .replace(
                    "{user}",
                    if (event.message.profile?.nickname in sponsorList) {
                        "§e${event.message.profile?.nickname}§f"
                    } else {
                        event.message.profile?.nickname ?: "[ 익명 ]"
                    }
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
            val config = loadConfigFile(uuid.toString()) ?: return
            val channelName = "§l[ §r${getChannelName(config, uuid, event.chat.channelId)} §f§l]§r"

            val donationFormat = stream.config.getString("donation.format")
                ?.replace("&", "§")
                ?.replace("{msg}", event.message.content)
                ?.replace("{user}", event.message.profile?.nickname ?: "[ 익명 ]")
                ?.replace("{paid}", event.message.payAmount.toString())
                ?.replace("{plat}", getPlatformName())
                ?.replace(Regex("\\{[^}]*}"), "(이모티콘)")
                ?: return

            val messageTarget = config.getString("message") ?: "streamer"
            if (messageTarget.contains("streamer")) {
                sendMessage(Component.text("$channelName$donationFormat"), player)

                val donationTitle = stream.config.getString("donation.tformat")
                    ?.replace("&", "§")
                    ?.replace("{msg}", event.message.content)
                    ?.replace("{user}", event.message.profile?.nickname ?: "[ 익명 ]")
                    ?.replace("{paid}", event.message.payAmount.toString())
                    ?.replace("{plat}", getPlatformName())
                    ?: return
                showTitle("", donationTitle, player)
            } else {
                Bukkit.getOnlinePlayers().forEach { sendMessage(Component.text("$channelName$donationFormat"), it) }
            }

            val sponsorList = config.getStringList("sponsor").toMutableList()
            sponsorList.add(event.message.profile?.nickname ?: "익명 ${Math.random()}")
            config.set("sponsor", sponsorList)
            config.save(File("${stream.dataFolder}/chzzk_channel", "$uuid.yml"))

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
            stream.logger.warning("${e.message}")
            e.printStackTrace()
        }
    }
}
