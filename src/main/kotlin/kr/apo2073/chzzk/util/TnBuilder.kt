package kr.apo2073.chzzk.util

import com.outstandingboy.donationalert.platform.Toonation
import kr.apo2073.chzzk.Chk
import kr.apo2073.chzzk.tn
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.time.Duration
import java.util.*

val chk=Chk.instance!!
fun TonBuilder(uuid: UUID, key: String) {
    tn[uuid]= Toonation(key)
    chk.reloadConfig()

    tn[uuid]?.subscribeMessage { m->
        if (!chk.config.getBoolean("채널")) return@subscribeMessage
        val file= File("${chk.dataFolder}/tn_channel", "${uuid}.yml")
        if (!file.exists()) return@subscribeMessage
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        val sponsorL=config.getStringList("sponsor")
        val message=config.getString("message") ?: "streamer"
        chk.reloadConfig()

        var chatFormat=
            if (config.getString("Chat-format")==""
                || config.getString("Chat-format").isNullOrEmpty()) config.getString("chat.format")  ?: "{user} : {msg}"
            else config.getString("Chat-format") ?: "{user} : {msg}"

        chatFormat=chatFormat
            .replace("&","§")
            .replace("{msg}", m.comment)
            .replace("{user}", if (sponsorL.contains(m.id)) {
                "§e${m.nickName}§f"
            } else {
                m.nickName ?: "[ 익명 ]"
            })
            .replace("{plat}", if (chk.config.getBoolean("color")) {
                    if (chk.config.getBoolean("en")) {
                        "§bToonation§f"
                    } else {
                        "§b투네이션§f"
                    }
                } else {
                if (chk.config.getBoolean("en")) {
                    "Toonation"
                } else {
                    "투네이션"
                }
                })
            .replace(Regex("\\{[^}]*\\}"), "§7(이모티콘)§f").trim()
        val channelName= "§l[ §r${config.getString("channelName")?.replace("&","§")
            ?: "알 수 없는 채널"} §f§l]§r"
        if (message.contains("streamer")) {
            val player= Bukkit.getPlayer(uuid)
            player?.sendMessage(Component.text("${channelName}${chatFormat}"))
        } else {
            Bukkit.broadcast(Component.text("${channelName}${chatFormat}"))
        }
    }

    tn[uuid]?.subscribeDonation { d->
        if (!chk.config.getBoolean("후원")) return@subscribeDonation
        val file= File("${chk.dataFolder}/chzzk_channel", "${uuid}.yml")
        if (!file.exists()) return@subscribeDonation
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        val message=config.getString("message")
        val channelName="§l[ §r${config.getString("channelName")?.replace("&","§") ?: "알 수 없는 채널"} §f§l]§r"

        chk.reloadConfig()
        val donationF=chk.config.getString("donation.format")
            ?.replace("&","§")
            ?.replace("{msg}", d.comment)
            ?.replace("{user}", d.nickName ?: "[ 익명 ]")
            ?.replace("{chs}", d.amount.toString())
            ?.replace(Regex("\\{[^}]*\\}"), "(이모티콘)")?.trim()
            ?.replace("{plat}", if (chk.config.getBoolean("color")) {
            if (chk.config.getBoolean("en")) {
                "§bToonation§f"
            } else {
                "§b투네이션§f"
            }
            } else {
            if (chk.config.getBoolean("en")) {
                "Toonation"
            } else {
                "투네이션"
            }
            })

        if (message=="streamer") {
            val player=Bukkit.getPlayer(uuid) ?: return@subscribeDonation
            player.sendMessage(Component.text("${channelName}$donationF"))

            val donationT=chk.config.getString("donation.tformat")
                ?.replace("&","§")
                ?.replace("{msg}", d.comment)
                ?.replace("{user}", d.nickName ?: "[ 익명 ]")
                ?.replace("{chs}", d.amount.toString())
                ?.replace("{plat}", if (chk.config.getBoolean("color")) {
                    if (chk.config.getBoolean("en")) {
                        "§bToonation§f"
                    } else {
                        "§b투네이션§f"
                    }
                } else {
                    if (chk.config.getBoolean("en")) {
                        "Toonation"
                    } else {
                        "투네이션"
                    }
                })
                ?: return@subscribeDonation
            val title= Title.title(Component.text(""), Component.text(donationT)
                , Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(5), Duration.ofSeconds(0)))
            if (chk.config.getBoolean("view-title")) player.showTitle(title)
        } else {
            Bukkit.broadcast(Component.text("${channelName}${donationF}"))
        }

        val player=Bukkit.getPlayer(uuid) ?: return@subscribeDonation
        val eventCmd=chk.config.getString("donation-event.${d.amount.toInt()}") ?: return@subscribeDonation

        player.performCommand(eventCmd)

        val sponsorL=config.getStringList("sponsor")
        sponsorL.add(d.id ?: return@subscribeDonation)
        try {
            config.save(file)
        } catch (e: Exception) {
            chk.logger.warning(e.message)
        }
    }
}