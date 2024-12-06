package kr.apo2073.stream.builders

import kr.apo2073.stream.Stream
import kr.apo2073.stream.af
import kr.apo2073.stream.events.AfreecaListener
import kr.apo2073.stream.util.Cconfig
import kr.apo2073.stream.util.Managers.prefix
import kr.apo2073.stream.util.Managers.sendMessage
import kr.apo2073.stream.util.connectionSave
import me.taromati.afreecatv.AfreecatvAPI
import me.taromati.afreecatv.exception.AfreecatvException
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

        val file= File("${Stream.instance!!}/afreeca_channel", "${uuid}.yml")
        val config=YamlConfiguration.loadConfiguration(file)
        sendMessage(prefix.append(Component.text("채널 ${afAPI.bjName}에 연결했습니다.")), player)
        config.save(file)
    } catch (e: AfreecatvException) {
        sendMessage(prefix.append(Component.text("오류! §c${e.message}")), player)
    } catch (e: Exception) {
        e.printStackTrace()
        Cconfig.set(bjID, null)
        connectionSave()
    }

}
fun afGetName(bjID: String):String {
    return AfreecatvAPI.getLiveInfo(bjID).bjName
}