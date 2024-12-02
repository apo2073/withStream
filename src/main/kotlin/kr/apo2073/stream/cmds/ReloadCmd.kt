package kr.apo2073.stream.cmds

import kr.apo2073.stream.Stream
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class ReloadCmd(plugin: JavaPlugin):CommandExecutor {
    init {
        plugin.getCommand("리로드")?.setExecutor(this::onCommand)
    }
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("chk.reload")) {
            sender.sendMessage(
                Component.text("§l[§c*§f]§r 해당 명령어를 실행할 권한이 없습니다")
                    .hoverEvent(
                        HoverEvent.showText(Component.text("[ 권한 :: §cchk.reload §f]").decorate(
                            TextDecoration.BOLD))))
            return true
        }
        Stream.instance!!.reloadConfig()
        sender.sendMessage(Component.text("§l[§a*§f]§r config 파일을 리로드 했습니다"))
        return true
    }
}