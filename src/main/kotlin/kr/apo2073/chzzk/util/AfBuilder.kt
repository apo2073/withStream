package kr.apo2073.chzzk.util

import kr.apo2073.chzzk.Chk
import kr.apo2073.chzzk.af
import kr.apo2073.chzzk.events.AfreecaListener
import me.taromati.afreecatv.AfreecatvAPI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

fun AfBuilder(uuid:UUID, bjID:String) {
    val player= Bukkit.getPlayer(uuid) ?: return
    try {
        val afAPI=AfreecatvAPI.getLiveInfo(bjID)
        af[uuid]=AfreecatvAPI.AfreecatvBuilder().withData(bjID)
            .build()
            .addListeners(AfreecaListener())

        val file= File("${Chk.instance!!}/afreeca_channel", "${uuid}.yml")
        val config=YamlConfiguration.loadConfiguration(file)
        player.sendMessage(Component.text("§l[§a*§f]§r 채널 ${afAPI.bjName}에 연결했습니다."))
        config.save(file)
    } catch (e: Exception) {
        player.sendMessage(Component.text("§l[§c*§f]§r ${e.message.toString()}"))
        Cconfig.set(bjID, null)
        connectionSave()
    }

}
fun afGetName(bjID: String):String {
    return AfreecatvAPI.getLiveInfo(bjID).bjName
}