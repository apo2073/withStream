package kr.apo2073.chzzk.events

import kr.apo2073.aLib.Etc.bcast
import kr.apo2073.chzzk.Chk
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import xyz.r2turntrue.chzzk4j.chat.ChatEventListener
import xyz.r2turntrue.chzzk4j.chat.ChatMessage
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import xyz.r2turntrue.chzzk4j.chat.DonationMessage
import java.io.File
import java.time.Duration
import java.util.*

class ChzzkListeners(private val plugin: JavaPlugin):ChatEventListener {
    private var chk=Chk.instance!!

    override fun onChat(msg: ChatMessage, chat: ChzzkChat) {
        plugin.server.scheduler.runTask(plugin, Runnable {
            chk.reloadConfig()
            if (!chk.config.getBoolean("채팅")) return@Runnable
            val uuid = UUID.fromString(chk.config.getString(chat.channelId))
            val file= File("${plugin.dataFolder}/chzzk_channel", "${uuid}.yml")
            if (!file.exists()) return@Runnable

            val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
            val sponsorL=config.getStringList("sponsor")
            val message=config.getString("message") ?: "streamer"
            chk.reloadConfig()

            var chatFormat=
                if (config.getString("Chat-format")==""
                    || config.getString("Chat-format").isNullOrEmpty()) chk.config.getString("chat.format")  ?: "{user} : {msg}"
                else config.getString("Chat-format") ?: "{user} : {msg}"

            chatFormat=chatFormat
                .replace("&","§")
                .replace("{msg}", msg.content)
                .replace("{user}", if (sponsorL.contains(msg.userId)) {
                    "§e${msg.profile!!.nickname}§f"
                } else {
                    msg.profile?.nickname ?: "[ 익명 ]"
                })
                .replace(Regex("\\{[^}]*\\}"), "§7(이모티콘)§f").trim()
            val channelName="§l[ §r${config.getString("channelName")?.replace("&","§") ?: "알 수 없는 채널"} §f§l]§r"
            if (message.contains("streamer")) {
                val player=Bukkit.getPlayer(uuid)
                player?.sendMessage(Component.text("${channelName}${chatFormat}"))
                    ?: Bukkit.broadcast(Component.text("${channelName}${chatFormat}"))
            } else {
                Bukkit.broadcast(Component.text("${channelName}${chatFormat}"))
            }
        })
    }

    override fun onDonationChat(msg: DonationMessage, chat: ChzzkChat) {
        plugin.server.scheduler.runTask(plugin, Runnable {
            chk.reloadConfig()
            if (!chk.config.getBoolean("후원")) return@Runnable
            val uuid=UUID.fromString(chk.config.getString(chat.channelId))
            val file= File("${plugin.dataFolder}/chzzk_channel", "${uuid}.yml")
            if (!file.exists()) return@Runnable
            val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
            val message=config.getString("message")
            val channelName="§l[ §r${config.getString("channelName")?.replace("&","§") ?: "알 수 없는 채널"} §f§l]§r"

            chk.reloadConfig()
            val donationF=chk.config.getString("donation.format")
                ?.replace("&","§")
                ?.replace("{msg}", msg.content)
                ?.replace("{user}", msg.profile?.nickname ?: "[ 익명 ]")
                ?.replace("{chs}", msg.payAmount.toString())
                ?.replace(Regex("\\{[^}]*\\}"), "(이모티콘)")?.trim()

            if (message=="streamer") {
                val player=Bukkit.getPlayer(uuid) ?: return@Runnable
                player.sendMessage(Component.text("${channelName}$donationF"))

                val donationT=chk.config.getString("donation.tformat")
                    ?.replace("&","§")
                    ?.replace("{msg}", msg.content)
                    ?.replace("{user}", msg.profile?.nickname ?: "[ 익명 ]")
                    ?.replace("{chs}", msg.payAmount.toString()) ?: return@Runnable
                val title=Title.title(Component.text(""), Component.text(donationT)
                    , Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(5), Duration.ofSeconds(0)))
                if (chk.config.getBoolean("view-title")) player.showTitle(title)
            } else {
                Bukkit.broadcast(Component.text("${channelName}${donationF}"))
            }

            val player=Bukkit.getPlayer(uuid) ?: return@Runnable
            val eventCmd=chk.config.getString("donation-event.${msg.payAmount}") ?: return@Runnable

            player.performCommand(eventCmd)

            val sponsorL= config.getStringList("sponsor") ?: mutableListOf()
            config.set("sponsor", sponsorL.add(msg.userId))
            try {
                config.save(file)
            } catch (e: Exception) {
                plugin.logger.warning(e.message)
            }
        })
    }

    override fun onError(ex: Exception) {
        this.plugin.logger.warning(ex.toString())
    }
}