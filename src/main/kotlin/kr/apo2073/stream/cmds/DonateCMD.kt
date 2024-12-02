package kr.apo2073.stream.cmds

import kr.apo2073.stream.*
import kr.apo2073.stream.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class DonateCMD(plugin: JavaPlugin) : TabExecutor {
    private val chk = Stream.instance!!

    init {
        plugin.getCommand("후원")?.apply {
            tabCompleter = this@DonateCMD
            setExecutor(this@DonateCMD)
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("chk.donation")) {
            sender.sendMessage(createHoverMessage("§l[§c*§f]§r 해당 명령어를 실행할 권한이 없습니다", "[ 권한 :: §cchk.donation §f]"))
            return true
        }

        if (sender !is Player) return false

        if (args.isNullOrEmpty()) return false

        val action = args[0]
        when (action) {
            "reload" -> {
                chk.reloadConfig()
                sender.sendMessage(Component.text("§l[§a*§f]§r config 파일을 리로드 했습니다"))
            }
            "등록", "등록해제", "설정" -> {
                if (args.size < 3 && action == "등록") return false // Needs platform and ID
                if (args.size < 2 && action == "설정") return false // Needs amount

                val platform = if (action == "등록") args[1] else ""
                val chID = if (action == "등록") args[2] else ""
                val amount = if (action == "설정") args[1].toIntOrNull() else null
                val commandToSet = if (action == "설정") args.drop(2).joinToString(" ") else ""

                val file = when {
                    platform.contains("치지직") -> File("${chk.dataFolder}/chzzk_channel", "${sender.uniqueId}.yml")
                    platform.contains("아프리카") -> File("${chk.dataFolder}/afreeca_channel", "${sender.uniqueId}.yml")
                    else -> File("${chk.dataFolder}/yt_channel", "${sender.uniqueId}.yml")
                }

                val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)

                when (action) {
                    "등록" -> {
                        try {
                            if (Cconfig.getString(chID) != null) {
                                sender.sendMessage("§l[§c*§f]§r 이 채널은 이미 다른 플레이어가 등록한 채널입니다.")
                                return true
                            }

                            if ((cht[sender.uniqueId] != null && chzzk[sender.uniqueId] != null) || tn[sender.uniqueId] != null) {
                                sender.sendMessage("§l[§c*§f]§r 한 채널만 등록할 수 있습니다.")
                                return true
                            }

                            config.set("channelID", chID)
                            /*config.set("channelName", chzzk[sender.uniqueId]?.getChannel(chID)?.channelName
                                ?: afGetName(chID))*/
                            config.set("owner", sender.name)
                            config.set("message", "streamer")
                            config.set(chID, sender.uniqueId.toString())
                            config.set(sender.uniqueId.toString(), chID)
                            config.save(file)

                            Cconfig.set(chID, sender.uniqueId.toString())
                            connectionSave()

                            if (platform.contains("치지직")) chk.ChkBuilder(sender.uniqueId, chID)
                            if (platform.contains("아프리카")) AfBuilder(sender.uniqueId, chID)
                        } catch (e: Exception) {
                            sender.sendMessage("§l[§c*§f]§r ${e.message}")
                        }
                    }
                    "등록해제" -> {
                        val channelName = config.get("channelName") ?: run {
                            sender.sendMessage("§l[§c*§f]§r 등록된 채널이 없습니다")
                            return true
                        }

                        sender.sendMessage("§l[§a*§f]§r 채널 ${channelName}§f(이)가 등록이 해제되었습니다")
                        val channelId = config.getString("channelID")
                        config.set(channelId.toString(), null)
                        config.save(file)
                        file.delete()
                    }
                    "설정" -> {
                        if (amount == null) {
                            sender.sendMessage(Component.text("§l[§c*§f]§r 올바른 금액을 입력하세요"))
                            return true
                        }

                        chk.config.set("donation-event.$amount", commandToSet)
                        chk.saveConfig()
                        sender.sendMessage("§l[§a*§f]§r 후원 이벤트 §6$amount§f을 명령어 §a$commandToSet§f(으)로 설정했습니다")
                    }
                }
            }
            else -> return false
        }
        return true
    }

    private fun createHoverMessage(message: String, hoverText: String): Component {
        return Component.text(message).hoverEvent(HoverEvent.showText(Component.text(hoverText).decorate(TextDecoration.BOLD)))
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>?): MutableList<String> {
        val tab = mutableListOf<String>()
        when (args?.size) {
            1 -> tab.addAll(listOf("등록", "등록해제", "reload", "설정"))
            2 -> if (args[0] == "등록") tab.addAll(listOf("치지직", "아프리카", "유튜브"))
            3 -> if (args[0] == "등록") tab.add("스트리머ID") else if (args[0] == "설정") tab.add("명령어")
        }
        return tab
    }
}
