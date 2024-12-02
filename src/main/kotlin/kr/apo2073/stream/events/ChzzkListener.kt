package kr.apo2073.stream.events

import kr.apo2073.stream.Stream
import kr.apo2073.stream.chzzk
import kr.apo2073.stream.util.CconfigReload
import kr.apo2073.stream.util.Dconfig
import kr.apo2073.stream.util.DconfigReload
import kr.apo2073.stream.util.DconfigSave
import kr.apo2073.stream.util.events.ChzzkChatEvent
import kr.apo2073.stream.util.events.ChzzkDonationEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File
import java.time.Duration

class ChzzkListener:Listener {
    private var strm= Stream.instance!!
    
    @EventHandler
    fun onChat(e: ChzzkChatEvent) {
        val msg=e.message
        val chat=e.chat
        val uuid=e.player?.uniqueId ?: return

        try {
            strm.reloadConfig()
            DconfigReload()
            CconfigReload()
            if (!strm.config.getBoolean("채팅")) {
                return
            }

            val file = File("${strm.dataFolder}/chzzk_channel", "${uuid}.yml")
            if (!file.exists()) {
                return
            }

            val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
            val sponsorL = Dconfig.getStringList("sponsor")
            val message = config.getString("message") ?: "streamer"

            var chatFormat = if (config.getString("Chat-format").isNullOrEmpty()) {
                strm.config.getString("chat.format") ?: "{user} : {msg}"
            } else {
                config.getString("Chat-format") ?: "{user} : {msg}"
            }

            chatFormat = chatFormat
                .replace("&", "§")
                .replace("{msg}", msg.content)
                .replace("{user}", if (msg.profile?.nickname in sponsorL) {
                    "§e${msg.profile!!.nickname}§f"
                } else {
                    msg.profile?.nickname ?: "[ 익명 ]"
                })
                .replace("{plat}", if (strm.config.getBoolean("color")) {
                    if (strm.config.getBoolean("en")) {
                        "§aChzzk§f"
                    } else {
                        "§a치지직§f"
                    }
                } else {
                    if (strm.config.getBoolean("en")) {
                        "Chzzk"
                    } else {
                        "치지직"
                    }
                })
                .replace(Regex("\\{[^}]*\\}"), "§7(이모티콘)§f").trim()

            val channelName = "§l[ §r${config.getString("channelName")?.replace("&", "§")
                ?: chzzk[uuid]?.getChannel(chat.channelId)?.channelName ?: "알 수 없음"} §f§l]§r"

            if (message.contains("streamer")) {
                val player = Bukkit.getPlayer(uuid)
                player?.sendMessage(Component.text("${channelName}${chatFormat}"))
            } else {
                for (pl in Bukkit.getOnlinePlayers()) {
                    pl.sendMessage(Component.text("${channelName}${chatFormat}"))
                }
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
    @EventHandler
    fun onDonation(e:ChzzkDonationEvent) {
        val msg=e.message
        val chat=e.chat
        val uuid=e.player?.uniqueId ?: return

        try {
            DconfigReload()
            strm.reloadConfig()
            CconfigReload()
            if (!strm.config.getBoolean("후원")) return
            val file = File("${strm.dataFolder}/chzzk_channel", "${uuid}.yml")
            if (!file.exists()) return
            val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
            val message = config.getString("message").toString()
            val channelName = "§l[ §r${
                config.getString("channelName")
                    ?.replace("&", "§") ?: "알 수 없는 채널"
            } §f§l]§r"

            strm.reloadConfig()
            val donationF = strm.config.getString("donation.format")
                ?.replace("&", "§")
                ?.replace("{msg}", msg.content)
                ?.replace("{user}", msg.profile?.nickname ?: "[ 익명 ]")
                ?.replace("{paid}", msg.payAmount.toString())
                ?.replace(Regex("\\{[^}]*\\}"), "(이모티콘)")?.trim()
                ?.replace(
                    "{plat}", if (strm.config.getBoolean("color")) {
                        if (kr.apo2073.stream.util.chk.config.getBoolean("en")) {
                            "§aChzzk§f"
                        } else {
                            "§a치지직§f"
                        }
                    } else {
                        if (strm.config.getBoolean("en")) {
                            "Chzzk"
                        } else {
                            "치지직"
                        }
                    }
                )

            if (message.contains("streamer")) {
                val player = Bukkit.getPlayer(uuid) ?: return
                player.sendMessage(Component.text("${channelName}$donationF"))

                val donationT = strm.config.getString("donation.tformat")
                    ?.replace("&", "§")
                    ?.replace("{msg}", msg.content)
                    ?.replace("{user}", msg.profile?.nickname ?: "[ 익명 ]")
                    ?.replace("{paid}", msg.payAmount.toString())
                    ?.replace(
                        "{plat}", if (strm.config.getBoolean("color")) {
                            if (kr.apo2073.stream.util.chk.config.getBoolean("en")) {
                                "§aChzzk§f"
                            } else {
                                "§a치지직§f"
                            }
                        } else {
                            if (kr.apo2073.stream.util.chk.config.getBoolean("en")) {
                                "Chzzk"
                            } else {
                                "치지직"
                            }
                        }
                    )
                    ?: return
                val title = Title.title(
                    Component.text(""),
                    Component.text(donationT),
                    Title.Times.times/*of*/(Duration.ofSeconds(0), Duration.ofSeconds(5), Duration.ofSeconds(0))
                )
                if (strm.config.getBoolean("view-title")) player.showTitle(title)
            } else {
                for (pl in Bukkit.getOnlinePlayers()) {
                    pl.sendMessage(Component.text("${channelName}${donationF}"))
                }
            }

            val sponsorL = config.getStringList("sponsor").toMutableList()
            sponsorL.add(msg.profile?.nickname ?: "익명 ${Math.random()}")
            config.set("sponsor", sponsorL)
            config.save(file)

            val dcl = Dconfig.getStringList("donated-channel")
            if (chat.channelId !in dcl) {
                dcl.add(chat.channelId)
            }
            Dconfig.set("donated-channel", dcl)
            DconfigSave()

            val player = Bukkit.getPlayer(uuid) ?: return
            val eventCmd = strm.config.getString("donation-event.${msg.payAmount}") ?: return
            eventCmd.replace("{player}", player.name)
                .replace("{msg}", msg.content.toString())
                .replace("{paid}", msg.payAmount.toString())
                .replace("{streamer}", player.name)

            if (eventCmd.startsWith("$")) {
                val commandToRun = eventCmd.removePrefix("$")
                Bukkit.dispatchCommand(
                    strm.server.consoleSender,
                    commandToRun
                )
            } else {
                player.performCommandAsOP(eventCmd)
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
}