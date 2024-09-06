package kr.apo2073.chzzk.cmds

import kr.apo2073.chzzk.Chk
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.java.JavaPlugin

class DonationEventCmd(plugin: JavaPlugin): TabExecutor {
    init {
        plugin.getCommand("후원")?.apply {
            setExecutor(this@DonationEventCmd)
            tabCompleter=this@DonationEventCmd
        }
    }
    val chk=Chk.instance!!

    override fun onCommand(sender: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (!sender.hasPermission("chk.donation")) {
            sender.sendMessage(
                Component.text("§l[§c*§f]§r 해당 명령어를 실행할 권한이 없습니다")
                    .hoverEvent(HoverEvent.showText(Component.text("[ 권한 :: §cchk.donation §f]").decorate(TextDecoration.BOLD))))
            return true
        }
        if ((p3?.size ?: 0) < 2) {
            sender.sendMessage(
                Component.text("§l[§c*§f]§r /후원 (금액) (명령어)"))
            return true
        }
        val amount= p3?.get(0)?.toIntOrNull() ?: run {
            sender.sendMessage(
                Component.text("§l[§c*§f]§r 올바른 금액을 입력하세요"))
            return true
        }
        val command=p3.drop(0).joinToString(" ")

        chk.config.set("donation-event.$amount", command)

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
            1-> {
                for (i in 1000..10000 step 1000) {
                    tab.add(i.toString())
                }
            }
            2-> {
                tab.add("Command")
            }
        }
        return tab
    }
}