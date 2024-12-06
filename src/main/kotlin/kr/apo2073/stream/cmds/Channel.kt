package kr.apo2073.stream.cmds

import kr.apo2073.stream.Stream
import kr.apo2073.stream.builders.AfBuilder
import kr.apo2073.stream.builders.ChkBuilder
import kr.apo2073.stream.builders.TonBuilder
import kr.apo2073.stream.cht
import kr.apo2073.stream.chzzk
import kr.apo2073.stream.tn
import kr.apo2073.stream.util.Cconfig
import kr.apo2073.stream.util.Managers.prefix
import kr.apo2073.stream.util.Managers.sendMessage
import kr.apo2073.stream.util.connectionSave
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
import java.util.*

class ChannelCmds(private val plugin: JavaPlugin) : TabExecutor {

    private val strm = Stream.instance!!

    init {
        val commands = listOf("치지직", "투네이션", "아프리카")
        commands.forEach {
            plugin.getCommand(it)?.apply {
                setExecutor(this@ChannelCmds)
                tabCompleter = this@ChannelCmds
            }
        }
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): Boolean {
        if (sender !is Player) {
            return true
        }

        if (!sender.hasPermission("stream.channel")) {
            sendMessage(
                prefix.append(Component.text("해당 명령어를 실행할 권한이 없습니다")
                    .hoverEvent(HoverEvent.showText(Component.text("[ 권한 :: §cstream.channel §f]").decorate(TextDecoration.BOLD)))),
                sender
            )
            return true
        }

        if (args.isNullOrEmpty()) {
            sendMessage(prefix.append(Component.text("/플렛폼 등록 <채널 이름> <채널 ID(또는 key)>")), sender)
            return false
        }

        val configFile = getChannelFile(label, sender.uniqueId)
        val config = YamlConfiguration.loadConfiguration(configFile)

        when (args[0]) {
            "등록" -> handleRegister(sender, label, args, config, configFile)
            "등록해제" -> handleUnregister(sender, label, config, configFile)
            "설정" -> handleSettings(sender, args, config, configFile)
            else -> sendMessage(prefix.append(Component.text("알 수 없는 명령어입니다.")), sender)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val suggestions = when (args.size) {
            1 -> listOf("등록", "등록해제", "설정")
            2 -> {
                if (args[0] == "설정") listOf("채널이름", "메세지대상")
                else if (args[0]=="등록") listOf("채널이름")
                else emptyList()
            }
            3 -> {
                if (args[1] == "메세지대상") listOf("스트리머에게만", "모두에게")
                else if (args[0]=="등록") listOf("채널ID")
                else emptyList()
            }
            else -> emptyList()
        }
        return suggestions.filter { it.startsWith(args.lastOrNull() ?: "", true) }.toMutableList()
    }

    private fun getChannelFile(label: String, uuid: UUID): File {
        val folder = when {
            label.contains("치지직") -> "chzzk_channel"
            label.contains("아프리") -> "afreeca_channel"
            label.contains("투네") -> "tn_channel"
            else -> "tn_channel"
        }
        return File("${plugin.dataFolder}/$folder", "$uuid.yml")
    }

    private fun handleRegister(
        sender: Player,
        label: String,
        args: Array<out String>,
        config: FileConfiguration,
        configFile: File
    ) {
        if (args.size != 3) {
            sendMessage(prefix.append(Component.text("/플렛폼 등록 <채널 이름> <채널 ID(또는 key)>")), sender)
            return
        }

        val channelName = args[1]
        val channelID = args[2]

        if (Cconfig.getString(channelID) != null) {
            sendMessage(prefix.append(Component.text("이 채널은 이미 다른 플레이어가 등록한 채널입니다")), sender)
            return
        }

        if ((cht[sender.uniqueId] != null && chzzk[sender.uniqueId] != null) || tn[sender.uniqueId] != null) {
            sendMessage(prefix.append(Component.text("한 채널만 등록할 수 있습니다")), sender)
            return
        }

        config.set("channelID", channelID)
        config.set("channelName", channelName)
        config.set("owner", sender.name)
        config.save(configFile)

        Cconfig.set(channelID, sender.uniqueId.toString())
        connectionSave()

        when {
            label.contains("치지직") -> ChkBuilder(sender.uniqueId, channelID)
            label.contains("투네") -> TonBuilder(sender.uniqueId, channelID)
            label.contains("아프") -> AfBuilder(sender.uniqueId, channelID)
        }
    }

    private fun handleUnregister(
        sender: Player,
        label: String,
        config: FileConfiguration,
        configFile: File
    ) {
        val channelName = config.getString("channelName") ?: run {
            sendMessage(prefix.append(Component.text("등록된 채널이 없습니다")), sender)
            return
        }

        val channelID = config.getString("channelID") ?: run {
            sendMessage(prefix.append(Component.text("등록된 채널이 없습니다")), sender)
            return
        }

        when {
            label.contains("치지직") -> {
                cht[sender.uniqueId]?.closeBlocking()
                cht.remove(sender.uniqueId)
            }
            label.contains("투네") -> tn.remove(sender.uniqueId)
            label.contains("아프") -> chzzk.remove(sender.uniqueId)
        }

        configFile.delete()
        Cconfig.set(channelID, null)
        connectionSave()

        sendMessage(prefix.append(Component.text("채널 ${channelName}을(를) 연결 해제했습니다")), sender)
    }

    private fun handleSettings(
        sender: Player,
        args: Array<out String>,
        config: FileConfiguration,
        configFile: File
    ) {
        if (args.size != 3) {
            sendMessage(prefix.append(Component.text("/플렛폼 설정 <설정> <설정 값>")), sender)
            return
        }

        val settingKey = args[1]
        val settingValue = args[2]

        when (settingKey) {
            "채널이름" -> {
                config.set("channelName", settingValue.replace("&", "§"))
                sendMessage(prefix.append(Component.text("채널 이름이 $settingValue§f(으)로 설정되었습니다.")), sender)
            }
            "메세지대상" -> {
                val target = if (settingValue.contains("모두")) "every" else "streamer"
                config.set("message", target)
                sendMessage(prefix.append(Component.text("메세지 대상이 $settingValue§f로 설정되었습니다.")), sender)
            }
            else -> sendMessage(prefix.append(Component.text("잘못된 설정 키입니다.")), sender)
        }

        config.save(configFile)
    }
}
