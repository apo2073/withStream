package kr.apo2073.stream.cmds

import kr.apo2073.stream.Stream
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
import java.io.File
import java.io.IOException

class DonationEvent(plugin: JavaPlugin): TabExecutor {
    init {
        plugin.getCommand("후원")?.apply {
            setExecutor(this@DonationEvent)
            tabCompleter=this@DonationEvent
        }
    }
    val strm=Stream.instance

    override fun onCommand(sender: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        if (!sender.hasPermission("stream.donation")) {
            sendMessage(
                prefix.append(Component.text("해당 명령어를 실행할 권한이 없습니다")
                    .hoverEvent(HoverEvent.showText(Component.text("[ 권한 :: §cstream.donation §f]").decorate(TextDecoration.BOLD)))), sender as Player)
            return true
        }
        if ((p3?.size ?: 0) < 2) {
            sendMessage(
                prefix.append(Component.text("/후원 (금액) (명령어)")), sender as Player)
            return true
        }
        val amount= p3?.get(0)?.toIntOrNull() ?: run {
            sendMessage(
                prefix.append(Component.text("올바른 금액을 입력하세요")),sender as Player)
            return true
        }
        val command=p3.drop(1).joinToString(" ")

        strm.config.set("donation-event.$amount", command)
        try {
            strm.config.save(File(strm.dataFolder, "config.yml"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        sendMessage(prefix.append(Component.text("후원 이벤트 §6${amount}§f을 명령어 §a${command}§f(으)로 설정했습니다")), sender as Player)

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
                tab.add("[실행될명령어]")
            }
        }
        return tab
    }
}