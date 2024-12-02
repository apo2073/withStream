package kr.apo2073.stream.cmds

import kr.apo2073.stream.Stream
import kr.apo2073.stream.cht
import kr.apo2073.stream.chzzk
import kr.apo2073.stream.tn
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

class ChannelCmds(val plugin: JavaPlugin) : TabExecutor {
    init {
        val cmdList= mutableListOf("치지직", "투네이션", "아프리카")
        for (cmd in cmdList) {
            plugin.getCommand(cmd)?.apply {
                setExecutor(this@ChannelCmds)
                tabCompleter=this@ChannelCmds
            }
        }
    }

    private val chk=Stream.instance!!

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§l[§c*§f]§r 플레이어 전용 명령어 입니다.")
            return true
        }
        if (!sender.hasPermission("stream.channel")) {
            sender.sendMessage(
                Component.text("§l[§c*§f]§r 해당 명령어를 실행할 권한이 없습니다")
                    .hoverEvent(HoverEvent.showText(Component.text("[ 권한 :: §cchk.channel §f]").decorate(TextDecoration.BOLD))))
            return true
        }
        if (args.isNullOrEmpty()) {
            sender.sendMessage("§l[§c*§f]§r /플렛폼 등록 <채널 이름> <채널 ID(또는 key)>")
            return false
        }

        val file=if (label.contains("치지직")) {
            File("${plugin.dataFolder}/chzzk_channel", "${sender.uniqueId}.yml")
        } else if(label.contains("아프리")) {
            File("${plugin.dataFolder}/afreeca_channel", "${sender.uniqueId}.yml")
        } else {
            File("${plugin.dataFolder}/tn_channel", "${sender.uniqueId}.yml")
        }

        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        val uuid= sender.uniqueId

        DconfigReload()
        CconfigReload()
        if (args[0]=="등록"){
            val chn: String
            val chID: String
            if (args.size == 3) {
                chn = args[1]
                chID = args[2]
            } else {
                sender.sendMessage("§l[§c*§f]§r /플렛폼 등록 <채널 이름> <채널 ID(또는 key)>")
                return false
            }

            if (Cconfig.getString(chID)!=null) {
                sender.sendMessage("§l[§c*§f]§r 이 채널은 이미 다른 플레이어가 등록한 채널입니다.")
                return true
            }

            if ((cht[sender.uniqueId]!=null
                        && chzzk[sender.uniqueId]!=null )
                || tn[sender.uniqueId]!=null) {
                sender.sendMessage(
                    Component.text("§l[§c*§f]§r 한 채널만 등록할 수 있습니다."))
                return true
            }

            CconfigReload()
            if(Cconfig.get(chID)!=null
                && cht[uuid]!=null
                && chzzk[uuid]!=null
                ) {
                chk.reloadConfig()
                sender.sendMessage(
                    Component.text("§l[§c*§f]§r 이미 다른 플레이어가 등록한 채널입니다!"))
                return true
            }

            config.set("channelID", chID)
            config.set("channelName", chn)
            config.set("owner", sender.name)

            config.set("message", "streamer")

            config.set(chID, sender.uniqueId.toString())
            config.set(sender.uniqueId.toString(), chID)
            config.save(file)

            Cconfig.set(chID, sender.uniqueId.toString())
            connectionSave()

            if (label.contains("치지직")) chk.ChkBuilder(sender.uniqueId, chID)
            if (label.contains("투네")) TonBuilder(sender.uniqueId, chID)
            if (label.contains("아프")) AfBuilder(sender.uniqueId, chID)
        } else if (args[0]=="등록해제") {
            val channelN=config.get("channelName")
            val channelId=config.getString("channelID")
            if (channelN==null) {
                sender.sendMessage("§l[§c*§f]§r 등록된 채널이 없습니다")
                return true
            }
            sender.sendMessage("§l[§a*§f]§r 채널 ${channelN}§f(이)가 등록이 해제되었습니다")
            if (label.contains("치지직")){
                cht[sender.uniqueId]?.closeBlocking()
                cht[sender.uniqueId]?.closeAsync()
                Cconfig.set(channelId.toString(), null)
                chzzk.remove(sender.uniqueId)
                connectionSave()
                cht.remove(sender.uniqueId)
            }
            if (label.contains("투네")) {
                tn[sender.uniqueId]
                Cconfig.set(tn[sender.uniqueId].toString(), null)
                connectionSave()
                tn.remove(sender.uniqueId)
            }
            file.delete()
        } else if (args[0]=="설정") {
            var setting = ""
            if (args.size == 3) {
                setting = args[2]
            } else {
                sender.sendMessage("§l[§c*§f]§r/플렛폼 설정 <설정> <설정 값>")
                sender.sendMessage("§l[§d*§f]§r/플렛폼 설정 채널이름 채널1")
                sender.sendMessage("§l[§d*§f]§r/플렛폼 설정 메세지대상 모두에게")
                return false
            }

            if (args[1]=="채널이름") {
                config.set("channelName", setting.replace("&", "§"))
                sender.sendMessage("§l[§a*§f]§r 채널 이름이 ${setting}§f(으)로 설정되었습니다.")
            }
            if (args[1]=="채팅포멧") {
                config.set("Chat-format", setting)
                sender.sendMessage("§l[§a*§f]§r 채팅 포멧이 ${setting}(으)로 설정되었습니다.")
            }
            if (args[1]=="메세지대상") {
                if (args[2].contains("모두")) {
                    config.set("message", "every")
                    sender.sendMessage("§l[§a*§f]§r 채팅/후원 메세지 대상이 '모두에게'로 설정되었습니다")
                } else {
                    config.set("message", "streamer")
                    sender.sendMessage("§l[§a*§f]§r 채팅/후원 메세지 대상이 '스트리머에게만'으로 설정되었습니다")
                }
            }

            try {
                config.save(file)
            } catch (e: Exception) {
                plugin.logger.warning(e.message)
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val tab = mutableListOf<String>()

        when (args.size) {
            1 -> {
                tab.add("등록")
                tab.add("등록해제")
                tab.add("설정")
            }
            2 -> {
                if (args[0] == "등록") tab.add("채널이름")
                if (args[0] == "설정") {
                    tab.add("채널이름")
                    tab.add("메세지대상")
                    //tab.add("채팅포멧")
                }
            }
            3 -> {
                if (args[0] == "등록") tab.add("채널ID")
                if (args[1]== "메세지대상") {
                    tab.add("스트리머에게만")
                    tab.add("모두에게")
                }
            }
        }

        return tab.filter { it.startsWith(args.lastOrNull() ?: "", true) }.toMutableList()
    }
}
