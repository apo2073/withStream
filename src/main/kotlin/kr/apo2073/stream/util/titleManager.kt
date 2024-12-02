package kr.apo2073.stream.util

import kr.apo2073.stream.Stream
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration

object titleManager {
    fun showTitle(mainTitle: String, subTitle:String, player: Player) {
        val title = Title.title(
            Component.text(mainTitle),
            Component.text(subTitle),
            Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(5), Duration.ofSeconds(0))
        )
        if (Stream.instance!!.config.getBoolean("view-title")) player.showTitle(title)
    }
}