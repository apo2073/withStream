package kr.apo2073.chzzk.cmds

import kr.apo2073.chzzk.*
import kr.apo2073.chzzk.util.*
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

class DonateCMD(plugin: JavaPlugin):TabExecutor {
    init {
        plugin.getCommand("후원")?.apply {
            tabCompleter=this@DonateCMD
            setExecutor(this@DonateCMD)
        }
    }
    val chk=Chk.instance!!
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("chk.donation")) {
            sender.sendMessage(
                Component.text("§l[§c*§f]§r 해당 명령어를 실행할 권한이 없습니다")
                    .hoverEvent(HoverEvent.showText(Component.text("[ 권한 :: §cchk.donation §f]").decorate(TextDecoration.BOLD))))
            return true
        }
        if (sender !is Player) return false
        when(args?.size ?: return false) {
            1-> {
                when(args[0]) {
                    "설정", "등록"-> {
                        return false
                    }
                    "reload"-> {
                        Chk.instance!!.reloadConfig()
                        sender.sendMessage(Component.text("§l[§a*§f]§r config 파일을 리로드 했습니다"))
                    }
                }
            }
            2-> {
                return false
            }
            3-> {
                when(args[0]) {
                    "등록"-> {
                        val platform=args[1]
                        val chID=args[2]

                        val file=if (platform.contains("치지직")) {
                            File("${chk.dataFolder}/chzzk_channel", "${sender.uniqueId}.yml")
                        } else if(platform.contains("아프리")) {
                            File("${chk.dataFolder}/afreeca_channel", "${sender.uniqueId}.yml")
                        } else {
                            File("${chk.dataFolder}/yt_channel", "${sender.uniqueId}.yml")
                        }

                        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
                        val uuid= sender.uniqueId

                        DconfigReload()
                        CconfigReload()

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
                        config.set("channelName", chzzk[uuid]?.getChannel(chID)?.channelName)
                        config.set("owner", sender.name)

                        config.set("message", "streamer")

                        config.set(chID, sender.uniqueId.toString())
                        config.set(sender.uniqueId.toString(), chID)
                        config.save(file)

                        Cconfig.set(chID, sender.uniqueId.toString())
                        connectionSave()

                        if (platform.contains("치지직")) chk.ChkBuilder(sender.uniqueId, chID)
                        if (platform.contains("아프")) AfBuilder(sender.uniqueId, chID)

                    }
                    "등록해제"-> {
                        var platform=""
                        val file=if (chzzk[sender.uniqueId]!=null) {
                            File("${chk.dataFolder}/chzzk_channel", "${sender.uniqueId}.yml")
                        } else if(af[sender.uniqueId]!=null) {
                            File("${chk.dataFolder}/afreeca_channel", "${sender.uniqueId}.yml")
                        } else {
                            File("${chk.dataFolder}/yt_channel", "${sender.uniqueId}.yml")
                        }
                        if (file.path.contains("chzzk")) platform="치지직"
                        if (file.path.contains("afree")) platform="아프리카"
                        if (file.path.contains("yt")) platform="유튜"


                        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
                        val uuid= sender.uniqueId

                        val channelN=config.get("channelName")
                        val channelId=config.getString("channelID")
                        if (channelN==null) {
                            sender.sendMessage("§l[§c*§f]§r 등록된 채널이 없습니다")
                            return true
                        }
                        sender.sendMessage("§l[§a*§f]§r 채널 ${channelN}§f(이)가 등록이 해제되었습니다")
                        if (platform.contains("치지직")) {
                            cht[sender.uniqueId]?.closeBlocking()
                            cht[sender.uniqueId]?.closeAsync()
                            Cconfig.set(channelId.toString(), null)
                            chzzk.remove(sender.uniqueId)
                            connectionSave()
                            cht.remove(sender.uniqueId)
                        }
                        if (platform.contains("투네")) {
                            tn[sender.uniqueId]
                            Cconfig.set(tn[sender.uniqueId].toString(), null)
                            connectionSave()
                            tn.remove(sender.uniqueId)
                        }
                        file.delete()
                    }
                    "설정"-> {
                        val amount=args[1].toIntOrNull() ?: run {
                            sender.sendMessage(
                                Component.text("§l[§c*§f]§r 올바른 금액을 입력하세요"))
                            return true
                        }
                        val command=args.drop(1).joinToString(" ")
                        chk.config.set("donation-event.$amount", command)
                        chk.saveConfig()
                        sender.sendMessage("§l[§a*§f]§r 후원 이벤트 §6${amount}§f을 명령어 §a${command}§f(으)로 설정했습니다")
                    }
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): MutableList<String> {
        val tab= mutableListOf<String>()
        when(p3?.size ?: return tab) {
            1 -> {
                tab.add("등록")
                tab.add("등록해제")
                tab.add("reload")
                tab.add("설정")
            }
            2-> {
                if (p3[0].equals("등록")) {
                    tab.add("치지직")
                    tab.add("아프리카")
                    tab.add("유튜브")
                } else if (p3[0].equals("설정")) {
                    tab.add("<금액>")
                }
            }
            3-> {
                if (p3[0].equals("등록")) {
                    tab.add("스트리머ID")
                } else if (p3[0].equals("설정")) {
                    tab.add("명령어")
                }
            }

        }
        return tab
    }
}