package kr.apo2073.stream.cmds

import kr.apo2073.stream.config.ConfigManager.getConfig
import kr.apo2073.stream.config.ConfigManager.removeConfig
import kr.apo2073.stream.config.ConfigManager.saveConfig
import kr.apo2073.stream.config.ConfigManager.setValue
import kr.apo2073.stream.config.ConnectionConfig.connectionSave
import kr.apo2073.stream.config.ConnectionConfig.getConnectionConfig
import kr.apo2073.stream.config.ConnectionConfig.setConnectionValue
import kr.apo2073.stream.Stream
import kr.apo2073.stream.af
import kr.apo2073.stream.builders.AfBuilder
import kr.apo2073.stream.builders.ChkBuilder
import kr.apo2073.stream.builders.TonBuilder
import kr.apo2073.stream.cht
import kr.apo2073.stream.tn
import kr.apo2073.stream.util.Managers.prefix
import kr.apo2073.stream.util.Managers.sendMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class ChannelCmds(private val plugin: JavaPlugin) : TabExecutor {

    private val strm = Stream.instance
    private val platformMap = mapOf(
        "치지직" to "chzzk",
        "투네이션" to "toonation",
        "아프리카" to "afreeca"
    )

    init {
        platformMap.keys.forEach {
            plugin.getCommand(it)?.apply {
                setExecutor(this@ChannelCmds)
                tabCompleter = this@ChannelCmds
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return true

        if (!sender.hasPermission("stream.channel")) {
            sendMessage(
                prefix.append(
                    Component.text("해당 명령어를 실행할 권한이 없습니다")
                        .hoverEvent(HoverEvent.showText(Component.text("[ 권한 :: §cstream.channel §f]").decorate(TextDecoration.BOLD)))
                ), sender
            )
            return true
        }

        if (args.isNullOrEmpty()) {
            sendMessage(prefix.append(Component.text("/플렛폼 등록 <채널 이름> <채널 ID(또는 key)>")), sender)
            return false
        }

        when (args[0]) {
            "등록" -> handleRegister(sender, label, args)
            "등록해제" -> handleUnregister(sender, label)
            "설정" -> handleSettings(sender, args, label)
            "정보" -> handleInfo(sender)
            else -> sendMessage(prefix.append(Component.text("알 수 없는 명령어입니다.")), sender)
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        val suggestions = when (args.size) {
            1 -> listOf("등록", "등록해제", "설정", "정보")
            2 -> when (args[0]) {
                "설정" -> listOf("채널이름", "메세지대상")
                "등록" -> listOf("채널이름")
                else -> emptyList()
            }
            3 -> when (args[1]) {
                "메세지대상" -> listOf("스트리머에게만", "모두에게")
                else -> listOf("채널ID")
            }
            else -> emptyList()
        }
        return suggestions.filter { it.startsWith(args.lastOrNull() ?: "", true) }.toMutableList()
    }

    private fun getPlatform(label: String): String {
        return platformMap.entries.find { label.contains(it.key) }?.value ?: "none"
    }

    private fun handleRegister(sender: Player, label: String, args: Array<out String>) {
        if (args.size != 3) {
            sendMessage(prefix.append(Component.text("/플렛폼 등록 <채널 이름> <채널 ID(또는 key)>")), sender)
            return
        }

        val (channelName, channelID) = args[1] to args[2]

        if (getConnectionConfig().getString(channelID) != null) {
            sendMessage(prefix.append(Component.text("이 채널은 이미 다른 플레이어가 등록한 채널입니다")), sender)
            return
        }

        if (cht[sender.uniqueId] != null || tn[sender.uniqueId] != null) {
            sendMessage(prefix.append(Component.text("한 채널만 등록할 수 있습니다")), sender)
            return
        }

        val platform = getPlatform(label)
        setValue(sender, "$platform.channelID", channelID)
        setValue(sender, "$platform.channelName", channelName)
        setValue(sender, "$platform.owner", sender.name)
        saveConfig(sender)

        setConnectionValue(channelID, sender.uniqueId.toString())
        connectionSave()

        when (platform) {
            "chzzk" -> ChkBuilder(sender.uniqueId, channelID)
            "toonation" -> TonBuilder(sender.uniqueId, channelID)
            "afreeca" -> AfBuilder(sender.uniqueId, channelID)
        }
    }

    private fun handleUnregister(sender: Player, label: String) {
        val platform = getPlatform(label)
        val config = getConfig(sender)
        val channelName = config.getString("$platform.channelName") ?: run {
            sendMessage(prefix.append(Component.text("등록된 채널이 없습니다")), sender)
            return
        }

        val channelID = config.getString("$platform.channelID") ?: return

        when (platform) {
            "chzzk" -> cht[sender.uniqueId]?.closeBlocking().also { cht.remove(sender.uniqueId) }
            "toonation" -> tn.remove(sender.uniqueId)
            "afreeca" -> af.remove(sender.uniqueId)
        }

        removeConfig(sender)
        setConnectionValue(channelID, null)
        connectionSave()

        sendMessage(prefix.append(Component.text("채널 ${channelName}을(를) 연결 해제했습니다")), sender)
    }

    private fun handleSettings(sender: Player, args: Array<out String>, label: String) {
        if (args.size != 3) {
            sendMessage(prefix.append(Component.text("/플렛폼 설정 <설정> <설정 값>")), sender)
            return
        }

        val (settingKey, settingValue) = args[1] to args[2]

        when (settingKey) {
            "채널이름" -> {
                setValue(sender, "${getPlatform(label)}.channelName", settingValue.replace("&", "§"))
                sendMessage(prefix.append(Component.text("채널 이름이 $settingValue§f(으)로 설정되었습니다.")), sender)
            }
            "메세지대상" -> {
                val target = if (settingValue.contains("모두")) "every" else "streamer"
                setValue(sender, "message", target)
                sendMessage(prefix.append(Component.text("메세지 대상이 $settingValue§f로 설정되었습니다.")), sender)
            }
            else -> sendMessage(prefix.append(Component.text("잘못된 설정 키입니다.")), sender)
        }
        saveConfig(sender)
    }

    private fun handleInfo(sender: Player) {
        sendMessage(prefix.append(Component.text("Coming Soon")), sender)
    }
}
