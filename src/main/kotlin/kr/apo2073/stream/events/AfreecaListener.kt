package kr.apo2073.stream.events

import kr.apo2073.stream.Stream
import kr.apo2073.stream.util.*
import kr.apo2073.stream.util.Managers.prefix
import kr.apo2073.stream.util.Managers.sendMessage
import kr.apo2073.stream.util.Managers.showTitle
import me.taromati.afreecatv.event.implement.DonationChatEvent
import me.taromati.afreecatv.event.implement.MessageChatEvent
import me.taromati.afreecatv.listener.AfreecatvListener
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class AfreecaListener : AfreecatvListener {

    val chk = Stream.instance!!
    val BoP = chk.config.getBoolean("BoolOrPay")

    override fun onMessageChat(e: MessageChatEvent) {
        chk.reloadConfig()
        DconfigReload()
        CconfigReload()
        if (!chk.config.getBoolean("채팅")) return

        val uuid = UUID.fromString(Cconfig.getString(e.channelId) ?: return) ?: return
        val file = File("${Stream.instance!!}/afreeca_channel", "$uuid.yml")
        if (!file.exists()) return

        val config = YamlConfiguration.loadConfiguration(file)
        val sponsorL = Dconfig.getStringList("sponsor")
        val message = config.getString("message") ?: "streamer"
        chk.reloadConfig()

        var chatFormat = if (config.getString("Chat-format").isNullOrEmpty()) {
            chk.config.getString("chat.format") ?: "{user} : {msg}"
        } else {
            config.getString("Chat-format") ?: "{user} : {msg}"
        }

        chatFormat = chatFormat
            .replace("&", "§")
            .replace("{msg}", e.message)
            .replace("{user}", if (e.nickname in sponsorL) {
                "§e${e.nickname}§f"
            } else {
                e.nickname ?: "[ 익명 ]"
            })
            .replace("{plat}", if (chk.config.getBoolean("color")) {
                if (chk.config.getBoolean("en")) {
                    "§9AfreecaTV§f"
                } else {
                    "§9아프리카§f"
                }
            } else {
                if (chk.config.getBoolean("en")) {
                    "AfreecaTv"
                } else {
                    "아프리카"
                }
            })
            .replace(Regex("\\{[^}]*\\}"), "§7(이모티콘)§f").trim()

        val channelName = "§l[ §r${config.getString("channelName")?.replace("&", "§") ?: return} §f§l]§r"

        if (message.contains("streamer")) {
            val player = Bukkit.getPlayer(uuid) ?: return
            player.let { sendMessage(Component.text("$channelName$chatFormat"), it) }
        } else {
            Bukkit.getOnlinePlayers().forEach { sendMessage(Component.text("$channelName$chatFormat"), it) }
        }
    }

    override fun onDonationChat(e: DonationChatEvent) {
        DconfigReload()
        chk.reloadConfig()
        CconfigReload()
        if (!chk.config.getBoolean("후원")) return

        val uuid = UUID.fromString(Cconfig.getString(e.channelId) ?: return) ?: return
        val file = File("${Stream.instance!!}/afreeca_channel", "$uuid.yml")
        if (!file.exists()) return

        val config = YamlConfiguration.loadConfiguration(file)
        val message = config.getString("message").toString()
        val channelName = "§l[ §r${config.getString("channelName")?.replace("&", "§") ?: "알 수 없는 채널"} §f§l]§r"

        chk.reloadConfig()

        val donationF = chk.config.getString("donation.format")
            ?.replace("&", "§")
            ?.replace("{msg}", e.message)
            ?.replace("{user}", e.nickname ?: "[ 익명 ]")
            ?.replace("{paid}", if (BoP) {
                e.balloonAmount.toString()
            } else {
                e.payAmount.toString()
            })
            ?.replace(Regex("\\{[^}]*\\}"), "(이모티콘)")?.trim()
            ?.replace("{plat}", if (chk.config.getBoolean("color")) {
                if (chk.config.getBoolean("en")) {
                    "§9AfreecaTV§f"
                } else {
                    "§9아프리카§f"
                }
            } else {
                if (chk.config.getBoolean("en")) {
                    "AfreecaTv"
                } else {
                    "아프리카"
                }
            })

        if (message.contains("streamer")) {
            val player = Bukkit.getPlayer(uuid) ?: return
            sendMessage(prefix.append(Component.text("$channelName$donationF")), player)

            val donationT = chk.config.getString("donation.tformat")
                ?.replace("&", "§")
                ?.replace("{msg}", e.message)
                ?.replace("{user}", e.nickname ?: "[ 익명 ]")
                ?.replace("{paid}", if (BoP) {
                    e.balloonAmount.toString()
                } else {
                    e.payAmount.toString()
                })
                ?.replace("{plat}", if (chk.config.getBoolean("color")) {
                    if (chk.config.getBoolean("en")) {
                        "§9AfreecaTV§f"
                    } else {
                        "§9아프리카§f"
                    }
                } else {
                    if (chk.config.getBoolean("en")) {
                        "AfreecaTv"
                    } else {
                        "아프리카"
                    }
                })
                ?: return
            showTitle("", donationT, player)
        } else {
            Bukkit.getOnlinePlayers().forEach { sendMessage(Component.text("$channelName$donationF"), it) }
        }

        val sponsorL = config.getStringList("sponsor").toMutableList()
        sponsorL.add(e.nickname ?: "익명 ${Math.random()}")
        config.set("sponsor", sponsorL)
        config.save(file)

        val dcl = Dconfig.getStringList("donated-channel")
        if (e.channelId !in dcl) {
            dcl.add(e.nickname)
        }
        Dconfig.set("donated-channel", dcl)
        DconfigSave()

        val player = Bukkit.getPlayer(uuid) ?: return
        val eventCmd = chk.config.getString("donation-event.${e.balloonAmount}") ?: return
        eventCmd.replace("{player}", player.name)
            .replace("{msg}", e.message)
            .replace("{paid}", if (BoP) {
                e.balloonAmount.toString()
            } else {
                e.payAmount.toString()
            })
            .replace("{streamer}", player.name)

        player.performCommandAsOP(eventCmd)
    }
}


fun Player.performCommandAsOP(command:String) {
    val iisOP=this.isOp
    this.isOp=true
    this.performCommand(command)
    this.isOp=iisOP
}