package kr.apo2073.chzzk.events

import kr.apo2073.aLib.Etc.bcast
import kr.apo2073.chzzk.Chk
import kr.apo2073.chzzk.cht
import kr.apo2073.chzzk.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import xyz.r2turntrue.chzzk4j.chat.ChatEventListener
import xyz.r2turntrue.chzzk4j.chat.ChatMessage
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import xyz.r2turntrue.chzzk4j.chat.DonationMessage
import java.io.File
import java.time.Duration
import java.util.*
import kotlin.math.nextUp

class ChzzkListeners(private val plugin: JavaPlugin):ChatEventListener {
    private var chk=Chk.instance!!

    override fun onChat(msg: ChatMessage, chat: ChzzkChat) {
        object : BukkitRunnable() {
            override fun run() {
                chk.reloadConfig()
                DconfigReload()
                CconfigReload()
                if (!chk.config.getBoolean("채팅")) return
                val uuid = UUID.fromString(Cconfig.getString(chat.channelId))
                val file= File("${plugin.dataFolder}/chzzk_channel", "${uuid}.yml")
                if (!file.exists()) this.cancel()
                val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
                val sponsorL=Dconfig.getStringList("sponsor")
                val message=config.getString("message") ?: "streamer"
                chk.reloadConfig()

                var chatFormat=
                    if (config.getString("Chat-format")==""
                        || config.getString("Chat-format").isNullOrEmpty()) chk.config.getString("chat.format")  ?: "{user} : {msg}"
                    else config.getString("Chat-format") ?: "{user} : {msg}"

                chatFormat=chatFormat
                    .replace("&","§")
                    .replace("{msg}", msg.content)
                    .replace("{user}", if (msg.profile?.nickname in sponsorL) {
                        "§e${msg.profile!!.nickname}§f"
                    } else {
                        msg.profile?.nickname ?: "[ 익명 ]"
                    })
                    .replace("{plat}", if (chk.config.getBoolean("color")) {
                        if (chk.config.getBoolean("en")) {
                            "§aChzzk§f"
                        } else {
                            "§a치지직§f"
                        }
                    } else {
                        if (chk.config.getBoolean("en")) {
                            "Chzzk"
                        } else {
                            "치지직"
                        }
                    })
                    .replace(Regex("\\{[^}]*\\}"), "§7(이모티콘)§f").trim()
                val channelName="§l[ §r${config.getString("channelName")
                    ?.replace("&","§") ?: "알 수 없는 채널"} §f§l]§r"
                if (message.contains("streamer")) {
                    val player=Bukkit.getPlayer(uuid)
                    player?.sendMessage(Component.text("${channelName}${chatFormat}"))
                } else {
                    Bukkit.broadcast(Component.text("${channelName}${chatFormat}"))
                }
            }
        }.runTask(plugin)
    }

    override fun onDonationChat(msg: DonationMessage, chat: ChzzkChat) {
        object : BukkitRunnable() {
            override fun run() {
                chk.reloadConfig()
                DconfigReload()
                CconfigReload()
                if (!chk.config.getBoolean("후원")) return
                val uuid=UUID.fromString(Cconfig.getString(chat.channelId))
                val file= File("${plugin.dataFolder}/chzzk_channel", "${uuid}.yml")
                if (!file.exists()) this.cancel()
                val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
                val message=config.getString("message").toString()
                val channelName="§l[ §r${config.getString("channelName")
                    ?.replace("&","§") ?: "알 수 없는 채널"} §f§l]§r"

                chk.reloadConfig()
                val donationF=chk.config.getString("donation.format")
                    ?.replace("&","§")
                    ?.replace("{msg}", msg.content)
                    ?.replace("{user}", msg.profile?.nickname ?: "[ 익명 ]")
                    ?.replace("{chs}", msg.payAmount.toString())
                    ?.replace(Regex("\\{[^}]*\\}"), "(이모티콘)")?.trim()
                    ?.replace("{plat}", if (chk.config.getBoolean("color")) {
                        if (kr.apo2073.chzzk.util.chk.config.getBoolean("en")) {
                            "§aChzzk§f"
                        } else {
                            "§a치지직§f"
                        }
                    } else {
                        if (kr.apo2073.chzzk.util.chk.config.getBoolean("en")) {
                            "Chzzk"
                        } else {
                            "치지직"
                        }
                    })

                if (message.contains("streamer")) {
                    val player=Bukkit.getPlayer(uuid) ?: return
                    player.sendMessage(Component.text("${channelName}$donationF"))

                    val donationT=chk.config.getString("donation.tformat")
                        ?.replace("&","§")
                        ?.replace("{msg}", msg.content)
                        ?.replace("{user}", msg.profile?.nickname ?: "[ 익명 ]")
                        ?.replace("{chs}", msg.payAmount.toString())
                        ?.replace("{plat}", if (chk.config.getBoolean("color")) {
                            if (kr.apo2073.chzzk.util.chk.config.getBoolean("en")) {
                                "§aChzzk§f"
                            } else {
                                "§a치지직§f"
                            }
                        } else {
                            if (kr.apo2073.chzzk.util.chk.config.getBoolean("en")) {
                                "Chzzk"
                            } else {
                                "치지직"
                            }
                        })
                        ?: return
                    val title=Title.title(Component.text(""), Component.text(donationT)
                        , Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(5), Duration.ofSeconds(0)))
                    if (chk.config.getBoolean("view-title")) player.showTitle(title)
                } else {
                    Bukkit.broadcast(Component.text("${channelName}${donationF}"))
                }

                val sponsorL= config.getStringList("sponsor").toMutableList()
                sponsorL.add(msg.profile?.nickname ?: "익명 ${Math.random()}")
                config.set("sponsor", sponsorL)
                config.save(file)

                val dcl= Dconfig.getStringList("donated-channel")
                if (chat.channelId !in dcl) {
                    dcl.add(chat.channelId)
                }
                Dconfig.set("donated-channel", dcl)
                DconfigSave()

                val player=Bukkit.getPlayer(uuid) ?: return
                val eventCmd=chk.config.getString("donation-event.${msg.payAmount}") ?: return
                eventCmd.replace("{player}", player.name)
                    .replace("{msg}", msg.content)
                    .replace("{paid}", msg.payAmount.toString())
                    .replace("{streamer}", player.name)

                player.performCommand(eventCmd)
            }
        }.runTask(plugin)
    }

    override fun onError(ex: Exception) {
        this.plugin.logger.warning(ex.toString())
    }
}