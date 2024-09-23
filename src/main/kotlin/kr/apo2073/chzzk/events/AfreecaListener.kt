package kr.apo2073.chzzk.events

import kr.apo2073.chzzk.Chk
import kr.apo2073.chzzk.util.*
import me.taromati.afreecatv.event.implement.DonationChatEvent
import me.taromati.afreecatv.event.implement.MessageChatEvent
import me.taromati.afreecatv.listener.AfreecatvListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.time.Duration
import java.util.*

class AfreecaListener():AfreecatvListener {
    val chk=Chk.instance!!
    override fun onMessageChat(e: MessageChatEvent) {
        chk.reloadConfig()
        DconfigReload()
        CconfigReload()
        if (!chk.config.getBoolean("채팅")) return
        val uuid = UUID.fromString(Cconfig.getString(e.channelId) ?: return) ?: return
        val file= File("${Chk.instance!!}/afreeca_channel", "${uuid}.yml")
        if (!file.exists()) return
        val config = YamlConfiguration.loadConfiguration(file)
        val sponsorL= Dconfig.getStringList("sponsor")
        val message=config.getString("message") ?: "streamer"
        chk.reloadConfig()

        var chatFormat=
            if (config.getString("Chat-format")==""
                || config.getString("Chat-format").isNullOrEmpty()) chk.config.getString("chat.format")  ?: "{user} : {msg}"
            else config.getString("Chat-format") ?: "{user} : {msg}"

        chatFormat=chatFormat
            .replace("&","§")
            .replace("{msg}", e.message)
            .replace("{user}", if (e.nickname in sponsorL) {
                "§e${e.nickname}§f"
            } else {
                e.nickname ?: "[ 익명 ]"
            })
            .replace("{plat}", if (chk.config.getBoolean("color")) {
                if (chk.config.getBoolean("en")) {
                    "§AfreecaTV§f"
                } else {
                    "§아프리카§f"
                }
            } else {
                if (chk.config.getBoolean("en")) {
                    "AfreecaTv"
                } else {
                    "아프리카"
                }
            })
            .replace(Regex("\\{[^}]*\\}"), "§7(이모티콘)§f").trim()
        val channelName="§l[ §r${config.getString("channelName")
            ?.replace("&","§") ?: return
        } §f§l]§r"
        if (message.contains("streamer")) {
            val player= Bukkit.getPlayer(uuid)
            player?.sendMessage(Component.text("${channelName}${chatFormat}"))
        } else {
            for (pl in Bukkit.getOnlinePlayers()) {
                pl.sendMessage(Component.text("${channelName}${chatFormat}"))
            }
        }
    }

    override fun onDonationChat(e: DonationChatEvent) {
        DconfigReload()
        chk.reloadConfig()
        CconfigReload()
        if (!chk.config.getBoolean("후원")) return
        val uuid=UUID.fromString(Cconfig.getString(e.channelId) ?: return) ?: return
        val file= File("${Chk.instance!!}/afreeca_channel", "${uuid}.yml")
        if (!file.exists()) return
        val config = YamlConfiguration.loadConfiguration(file)
        val message=config.getString("message").toString()
        val channelName="§l[ §r${config.getString("channelName")
            ?.replace("&","§") ?: "알 수 없는 채널"} §f§l]§r"

        chk.reloadConfig()
        val donationF=chk.config.getString("donation.format")
            ?.replace("&","§")
            ?.replace("{msg}", e.message)
            ?.replace("{user}", e.nickname ?: "[ 익명 ]")
            ?.replace("{chs}", e.balloonAmount.toString())
            ?.replace(Regex("\\{[^}]*\\}"), "(이모티콘)")?.trim()
            ?.replace("{plat}", if (chk.config.getBoolean("color")) {
                if (chk.config.getBoolean("en")) {
                    "§AfreecaTV§f"
                } else {
                    "§아프리카§f"
                }
            } else {
                if (chk.config.getBoolean("en")) {
                    "AfreecaTv"
                } else {
                    "아프리카"
                }
            })

        if (message.contains("streamer")) {
            val player=Bukkit.getPlayer(uuid) ?: return
            player.sendMessage(Component.text("${channelName}$donationF"))

            val donationT=chk.config.getString("donation.tformat")
                ?.replace("&","§")
                ?.replace("{msg}", e.message)
                ?.replace("{user}", e.nickname ?: "[ 익명 ]")
                ?.replace("{chs}", e.balloonAmount.toString())
                ?.replace("{plat}", if (chk.config.getBoolean("color")) {
                    if (chk.config.getBoolean("en")) {
                        "§AfreecaTV§f"
                    } else {
                        "§아프리카§f"
                    }
                } else {
                    if (chk.config.getBoolean("en")) {
                        "AfreecaTv"
                    } else {
                        "아프리카"
                    }
                })
                ?: return
            val title= Title.title(Component.text(""), Component.text(donationT)
                , Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(5), Duration.ofSeconds(0)))
            if (chk.config.getBoolean("view-title")) player.showTitle(title)
        } else {
            for (pl in Bukkit.getOnlinePlayers()) {
                pl.sendMessage(Component.text("${channelName}${donationF}"))
            }
        }

        val sponsorL= config.getStringList("sponsor").toMutableList()
        sponsorL.add(e.nickname ?: "익명 ${Math.random()}")
        config.set("sponsor", sponsorL)
        config.save(file)

        val dcl= Dconfig.getStringList("donated-channel")
        if (e.channelId !in dcl) {
            dcl.add(e.nickname)
        }
        Dconfig.set("donated-channel", dcl)
        DconfigSave()

        val player=Bukkit.getPlayer(uuid) ?: return
        val eventCmd=chk.config.getString("donation-event.${e.balloonAmount}") ?: return
        eventCmd.replace("{player}", player.name)
            .replace("{msg}", e.message)
            .replace("{paid}", e.balloonAmount.toString())
            .replace("{streamer}", player.name)

        player.performCommand(eventCmd)
    }
}