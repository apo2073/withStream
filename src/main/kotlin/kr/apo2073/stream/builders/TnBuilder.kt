package kr.apo2073.stream.builders

import com.outstandingboy.donationalert.platform.Toonation
import kr.apo2073.stream.Stream
import kr.apo2073.stream.config.ConfigManager.getConfig
import kr.apo2073.stream.config.ConnectionConfig.connectionSave
import kr.apo2073.stream.tn
import kr.apo2073.stream.utilities.versions.Managers.performCommandAsOP
import kr.apo2073.stream.utilities.versions.Managers.showTitle
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

private val strm=Stream.instance
private val enableColor = strm.config.getBoolean("color")
private val isChattingEnabled = strm.config.getBoolean("채팅")
private val isDonationEnabled = strm.config.getBoolean("후원")
private fun platformName():String {
    val platform = if (strm.config.getBoolean("en")) "Toonation" else "투네이션"
    return if (enableColor) "§b$platform§f" else platform
}
fun TonBuilder(uuid: UUID, key: String) {
    try {
        tn[uuid] = Toonation(key)
        strm.reloadConfig()
        connectionSave()

        tn[uuid]?.subscribeMessage { m ->
            if (!isChattingEnabled) return@subscribeMessage
            val config: FileConfiguration = getConfig(Bukkit.getPlayer(uuid) ?: return@subscribeMessage)
            val message = config.getString("message") ?: "streamer"
            strm.reloadConfig()

            var chatFormat =
                if (config.getString("Chat-format") == ""
                    || config.getString("Chat-format").isNullOrEmpty()
                ) config.getString("chat.format") ?: "{user} : {msg}"
                else config.getString("Chat-format") ?: "{user} : {msg}"

            chatFormat = chatFormat
                .replace("&", "§")
                .replace("{msg}", m.comment)
                .replace(
                    "{user}", m.nickName ?: "[ 익명 ]"
                )
                .replace(
                    "{plat}", platformName())
                .replace(Regex("\\{[^}]*\\}"), "§7(이모티콘)§f").trim()
            val channelName = "§l[ §r${
                config.getString("channelName")?.replace("&", "§")
                    ?: "알 수 없는 채널"
            } §f§l]§r"
            if (message.contains("streamer")) {
                val player = Bukkit.getPlayer(uuid)
                player?.sendMessage(Component.text("${channelName}${chatFormat}"))
            } else {
                Bukkit.broadcast(Component.text("${channelName}${chatFormat}"))
            }
        }

        tn[uuid]?.subscribeDonation { d ->
            if (!isDonationEnabled) return@subscribeDonation
            val file = File("${strm.dataFolder}/channel", "${uuid}.yml")
            if (!file.exists()) return@subscribeDonation
            val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
            val message = config.getString("message")
            val channelName = "§l[ §r${config.getString("channelName")?.replace("&", "§") ?: "알 수 없는 채널"} §f§l]§r"

            strm.reloadConfig()
            val donationF = strm.config.getString("donation.format")
                ?.replace("&", "§")
                ?.replace("{msg}", d.comment)
                ?.replace("{user}", d.nickName ?: "[ 익명 ]")
                ?.replace("{chs}", d.amount.toString())
                ?.replace(Regex("\\{[^}]*\\}"), "(이모티콘)")?.trim()
                ?.replace("{plat}", platformName())

            if (message == "streamer") {
                val player = Bukkit.getPlayer(uuid) ?: return@subscribeDonation
                player.sendMessage(Component.text("${channelName}$donationF"))

                val donationT = strm.config.getString("donation.tformat")
                    ?.replace("&", "§")
                    ?.replace("{msg}", d.comment)
                    ?.replace("{user}", d.nickName ?: "[ 익명 ]")
                    ?.replace("{chs}", d.amount.toString())
                    ?.replace("{plat}", platformName())
                    ?: return@subscribeDonation
                showTitle("", donationT, player)
            } else {
                Bukkit.broadcast(Component.text("${channelName}${donationF}"))
            }

            val player = Bukkit.getPlayer(uuid) ?: return@subscribeDonation
            val eventCmd = strm.config.getString("donation-event.${d.amount.toInt()}") ?: return@subscribeDonation

            player.performCommandAsOP(eventCmd)

        }
    } catch (e:Exception) {
        e.printStackTrace()
    }
}