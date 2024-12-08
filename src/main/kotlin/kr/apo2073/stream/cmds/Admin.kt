package kr.apo2073.stream.cmds

import kr.apo2073.stream.Stream
import kr.apo2073.stream.builders.strm
import kr.apo2073.stream.util.Managers.performCommandAsOP
import kr.apo2073.stream.util.Managers.prefix
import kr.apo2073.stream.util.Managers.sendMessage
import kr.apo2073.stream.util.Managers.showTitle
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Admin(private val plugin: JavaPlugin) : TabExecutor {

    companion object {
        const val PERMISSION = "stream.admin"
        const val NO_PERMISSION_MSG = "해당 명령어를 실행할 권한이 없습니다"
    }

    init {
        plugin.getCommand("strm")?.apply {
            setExecutor(this@Admin)
            tabCompleter = this@Admin
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission(PERMISSION)) {
            sendMessage(createHoverMessage(NO_PERMISSION_MSG, "권한 :: $PERMISSION"), sender as Player)
            return true
        }

        if (args.isEmpty()) {
            sendMessage(prefix.append(Component.text("올바른 사용법을 확인하세요")), sender as Player)
            return true
        }

        val targetPlayer = Bukkit.getPlayer(args[0]) ?: run {
            sendMessage(prefix.append(Component.text("해당 플레이어가 존재하지 않습니다")), sender as Player)
            return true
        }

        val targetFile = findConfigFile(targetPlayer.uniqueId) ?: run {
            sendMessage(prefix.append(Component.text("사용자 파일을 찾을 수 없습니다")), sender as Player)
            return true
        }

        val config = YamlConfiguration.loadConfiguration(targetFile)

        when (args.getOrNull(1)) {
            "유저채널설정" -> handleUserChannelSettings(sender as Player, args, targetPlayer, config)
            "후원이벤트실행" -> handleDonationEvent(sender as Player, args, targetPlayer, config)
            else -> sendMessage(prefix.append(Component.text("알 수 없는 명령입니다")), sender as Player)
        }

        saveConfig(config, targetFile)
        return true
    }

    private fun handleUserChannelSettings(
        sender: Player,
        args: Array<out String>,
        targetPlayer: Player,
        config: YamlConfiguration
    ) {
        val settingType = args.getOrNull(2) ?: run {
            sendMessage(prefix.append(Component.text("설정 타입을 입력하세요")), sender)
            return
        }

        val settingValue = args.getOrNull(3) ?: run {
            sendMessage(prefix.append(Component.text("설정 값을 입력하세요")), sender)
            return
        }

        when (settingType) {
            "채널이름" -> {
                val channelName = settingValue.replace("&", "§")
                config.set("channelName", channelName)
                sendMessage(prefix.append(Component.text("${targetPlayer.name}의 채널 이름이 $channelName(으)로 설정되었습니다.")), sender)
            }
            "메세지대상" -> {
                val target = if (settingValue.contains("모두")) "every" else "streamer"
                config.set("message", target)
                sendMessage(prefix.append(Component.text("${targetPlayer.name}의 메시지 대상이 '$target'로 설정되었습니다.")), sender)
            }
            else -> sendMessage(prefix.append(Component.text("알 수 없는 설정입니다")), sender)
        }
    }

    private fun handleDonationEvent(
        sender: Player,
        args: Array<out String>,
        targetPlayer: Player,
        config: YamlConfiguration
    ) {
        val amount = args.getOrNull(2)?.toIntOrNull() ?: run {
            sendMessage(prefix.append(Component.text("올바른 금액을 입력하세요")), sender)
            return
        }

        val donationMessage = createDonationMessage(config, amount)
        if (config.getString("message") == "streamer") {
            sendMessage(donationMessage, targetPlayer)
        } else {
            Bukkit.getOnlinePlayers().forEach { sendMessage(donationMessage, it) }
        }

        val eventCommand = strm.config.getString("donation-event.$amount") ?: run {
            sendMessage(prefix.append(Component.text("해당 이벤트를 찾을 수 없습니다")), sender)
            return
        }

        val donationTitle = Stream.instance!!.config.getString("donation.tformat")
            ?.replace("&", "§")
            ?.replace("{msg}", "관리자에 의해 실행됨")
            ?.replace("{user}", "§cADMIN§f")
            ?.replace("{paid}", amount.toString())
            ?: return
        showTitle("", donationTitle, targetPlayer)

        executeEventCommand(targetPlayer, eventCommand)
    }

    private fun createDonationMessage(config: YamlConfiguration, amount: Int): Component {
        val channelName = config.getString("channelName")?.replace("&", "§") ?: "알 수 없는 채널"
        val donationFormat = strm.config.getString("donation.format")
            ?.replace("{msg}", "관리자에 의해 실행됨")
            ?.replace("{user}", "§cADMIN§f")
            ?.replace("{paid}", amount.toString())
            ?.replace("&", "§")
            ?.replace(Regex("\\{[^}]*\\}"), "(이모티콘)") ?: "후원 메시지"

        return Component.text("§l[§r $channelName §f§l]§r $donationFormat")
    }

    private fun executeEventCommand(player: Player, command: String) {
        val cmd=command.replace("&", "§")
            .replace("{player}", player.name)
        if (command.startsWith("$")) {
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "execute as ${player.name} run ${cmd.removePrefix("$")}"
            )
        } else {
            player.performCommandAsOP(cmd)
        }
    }

    private fun findConfigFile(uuid: java.util.UUID): File? {
        val directories = listOf("channel")
        return directories.map { File("${plugin.dataFolder}/$it/${uuid}.yml") }.find { it.exists() }
    }

    private fun saveConfig(config: YamlConfiguration, file: File) {
        try {
            config.save(file)
        } catch (e: Exception) {
            plugin.logger.warning("파일 저장 실패: ${e.message}")
        }
    }

    private fun createHoverMessage(message: String, hoverText: String): Component {
        return prefix.append(Component.text(message).hoverEvent(
            HoverEvent.showText(Component.text(hoverText).decorate(TextDecoration.BOLD))
        ))
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return when (args.size) {
            1 -> Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
            2 -> mutableListOf("유저채널설정", "후원이벤트실행")
            3 -> when (args[1]) {
                "유저채널설정" -> mutableListOf("채널이름", "메세지대상")
                "후원이벤트실행" -> (1000..10000 step 1000).map { it.toString() }.toMutableList()
                else -> mutableListOf()
            }
            4 -> when (args[2]) {
                "메세지대상" -> mutableListOf("스트리머에게만", "모두에게")
                else -> mutableListOf()
            }
            else -> mutableListOf()
        }.filter { it.startsWith(args.lastOrNull() ?: "", true) }.toMutableList()
    }
}
