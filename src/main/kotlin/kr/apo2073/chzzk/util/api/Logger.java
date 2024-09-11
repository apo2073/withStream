package kr.apo2073.chzzk.util.api;

import kr.apo2073.chzzk.Chk;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.Objects;

public class Logger {
    private static final String prefix = ChatColor.AQUA + "[TRMT] ";

    public static void info(String msg) {
        Bukkit.getConsoleSender().sendMessage(prefix + msg);
    }

    public static void say(String msg) {
        String command = "say " + msg;
        Bukkit.getScheduler()
                .callSyncMethod(Objects.requireNonNull(Chk.Companion.getInstance()),
                        () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }
}
