package kr.apo2073.stream.cmds

import kr.apo2073.stream.util.CconfigReload
import kr.apo2073.stream.util.DconfigReload
import kr.apo2073.stream.util.chk
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.time.Duration

class AdminCommand(private val plugin: JavaPlugin) : TabExecutor {

    companion object {
        const val PERMISSION = "chk.admin"
    }

    init {
        plugin.getCommand("strm")?.apply {
            setExecutor(this@AdminCommand)
            tabCompleter = this@AdminCommand
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(
                Component.text("§l[§c*§f]§r 해당 명령어를 실행할 권한이 없습니다").hoverEvent(
                    HoverEvent.showText(Component.text("[ 권한 :: §c$PERMISSION §f]").decorate(TextDecoration.BOLD))
                )
            )
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("§l[§c*§f]§r 올바른 사용법을 확인하세요")
            return true
        }

        val trgP = Bukkit.getPlayer(args[0]) ?: run {
            sender.sendMessage("§l[§c*§f]§r 해당 플레이어가 존재하지 않습니다")
            return true
        }

        val uuid = trgP.uniqueId
        val directories = listOf("chzzk_channel", "afreeca_channel", "tn_channel")
        val file = directories.map { File("${plugin.dataFolder}/$it/${uuid}.yml") }.find { it.exists() }

        if (file == null) {
            sender.sendMessage("§l[§c*§f]§r 사용자 파일을 찾을 수 없습니다")
            return true
        }

        val config = YamlConfiguration.loadConfiguration(file)

        when {
            args.size < 2 -> {
                sender.sendMessage("§l[§c*§f]§r 올바른 인수를 입력하세요")
                return true
            }
            args[1] == "유저채널설정" -> {
                when {
                    args.size < 3 -> {
                        sender.sendMessage("§l[§c*§f]§r 올바른 인수를 입력하세요")
                        return true
                    }
                    args[2] == "채널이름" -> {
                        if (args.size < 4) {
                            sender.sendMessage("§l[§c*§f]§r 새 채널 이름을 입력하세요")
                            return true
                        }
                        val channelName = args[3].replace("&", "§")
                        config.set("channelName", channelName)
                        sender.sendMessage("§l[§a*§f]§r 플레이어 ${trgP.name}의 채널 이름이 $channelName(으)로 설정되었습니다.")
                    }
                    args[2] == "채팅포멧" -> {
                        if (args.size < 4) {
                            sender.sendMessage("§l[§c*§f]§r 채팅 포맷을 입력하세요")
                            return true
                        }
                        config.set("Chat-format", args[3])
                        sender.sendMessage("§l[§a*§f]§r 플레이어 ${trgP.name}의 채팅 포맷이 ${args[3]}(으)로 설정되었습니다.")
                    }
                    args[2] == "메세지대상" -> {
                        if (args.size < 4) {
                            sender.sendMessage("§l[§c*§f]§r 메시지 대상을 입력하세요")
                            return true
                        }
                        val target = args[3]
                        config.set("message", if (target.contains("모두")) "every" else "streamer")
                        sender.sendMessage("§l[§a*§f]§r 플레이어 ${trgP.name}의 채팅/후원 메시지 대상이 '${target}'로 설정되었습니다.")
                    }
                    else -> {
                        sender.sendMessage("§l[§c*§f]§r 알 수 없는 설정입니다")
                    }
                }
            }
            args[1] == "후원이벤트실행" -> {
                if (args.size < 3) {
                    sender.sendMessage("§l[§c*§f]§r 올바른 인수를 입력하세요")
                    return true
                }

                val eventId = args[2].toIntOrNull() ?: run {
                    sender.sendMessage("§l[§c*§f]§r 올바른 값을 입력하세요")
                    return true
                }

                DconfigReload()
                chk.reloadConfig()
                CconfigReload()
                val message = config.getString("message").toString()
                val channelName = "§l[ §r${
                    config.getString("channelName")
                        ?.replace("&", "§") ?: "알 수 없는 채널"
                } §f§l]§r"

                chk.reloadConfig()
                val donationF = chk.config.getString("donation.format")
                    ?.replace("&", "§")
                    ?.replace("{msg}", "관리자에 의해 실행됨")
                    ?.replace("{user}", "§cADMIN§f" ?: "[ 익명 ]")
                    ?.replace("{paid}", args[2])
                    ?.replace(Regex("\\{[^}]*\\}"), "(이모티콘)")?.trim()


                if (message.contains("streamer")) {
                    trgP.sendMessage(Component.text("${channelName}$donationF"))

                    val donationT = chk.config.getString("donation.tformat")
                        ?.replace("&", "§")
                        ?.replace("{msg}", "관리자에 의해 실행됨")
                        ?.replace("{user}", "§cADMIN§f")
                        ?.replace("{paid}", args[2]) ?: ""
                    val title = Title.title(
                        Component.text(""),
                        Component.text(donationT),
                        Title.Times./*times*/of(Duration.ofSeconds(0), Duration.ofSeconds(5), Duration.ofSeconds(0))
                    )
                    if (chk.config.getBoolean("view-title")) trgP.showTitle(title)
                } else {
                    for (pl in Bukkit.getOnlinePlayers()) {
                        pl.sendMessage(Component.text("${channelName}${donationF}"))
                    }
                }

                val eventCmd = chk.config.getString("donation-event.$eventId")
                    ?.replace("{player}", trgP.name)
                    ?.replace("{msg}", "관리자에 의해 실행됨")
                    ?.replace("{paid}", args[2])
                    ?.replace("{streamer}", trgP.name) ?: run {
                    sender.sendMessage("§l[§c*§f]§r 해당 이벤트를 찾을 수 없습니다")
                    return true
                }

                if (eventCmd.startsWith("$")) {
                    val commandToRun = eventCmd.removePrefix("$")
                    Bukkit.dispatchCommand(
                        chk.server.consoleSender,
                        "execute as ${trgP.name} run $commandToRun"
                    )
                } else {
                    trgP.performCommand(eventCmd)
                }

            }
            else -> {
                sender.sendMessage("§l[§c*§f]§r 알 수 없는 명령입니다")
            }
        }

        try {
            config.save(file)
        } catch (e: Exception) {
            plugin.logger.warning("파일 저장 실패: ${e.message}")
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        val tab = mutableListOf<String>()

        when (args.size) {
            1 -> {
                tab.addAll(Bukkit.getOnlinePlayers().map { it.name })
            }
            2 -> {
                tab.add("유저채널설정")
                tab.add("후원이벤트실행")
            }
            3 -> {
                when (args[1]) {
                    "유저채널설정" -> {
                        tab.add("채널이름")
                        tab.add("메세지대상")
                    }
                    "후원이벤트실행" -> {
                        tab.addAll((1000..10000 step 1000).map { it.toString() })
                    }
                }
            }
            4 -> {
                when (args[2]) {
                    "채널이름" -> {
                        tab.add("새채널이름")
                    }
                    "메세지대상" -> {
                        tab.add("스트리머에게만")
                        tab.add("모두에게")
                    }
                }
            }
        }

        return tab
    }
}
