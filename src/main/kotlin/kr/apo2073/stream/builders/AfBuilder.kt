package kr.apo2073.stream.builders

import kr.apo2073.stream.af
import kr.apo2073.stream.config.ConfigManager.getConfig
import kr.apo2073.stream.config.ConnectionConfig.connectionSave
import kr.apo2073.stream.config.ConnectionConfig.getConnectionConfig
import kr.apo2073.stream.events.AfreecaListener
import kr.apo2073.stream.util.Managers.prefix
import kr.apo2073.stream.util.Managers.sendMessage
import me.taromati.afreecatv.AfreecatvAPI
import me.taromati.afreecatv.exception.AfreecatvException
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.util.*

fun AfBuilder(uuid:UUID, bjID:String) {
    val player= Bukkit.getPlayer(uuid) ?: return
    try {
        val afAPI=AfreecatvAPI.getLiveInfo(bjID)
        af[uuid]=AfreecatvAPI.AfreecatvBuilder().withData(bjID)
            .build()
            .addListeners(AfreecaListener())

        val config=getConfig(Bukkit.getPlayer(uuid) ?: return)
        sendMessage(prefix.append(Component.text("채널 ${afAPI.bjName}에 연결했습니다.")), player)
    } catch (e: AfreecatvException) {
        sendMessage(prefix.append(Component.text("오류! §c${e.message}")), player)
    } catch (e: Exception) {
        e.printStackTrace()
        getConnectionConfig().set(bjID, null)
        connectionSave()
    }

}
fun afGetName(bjID: String):String {
    return AfreecatvAPI.getLiveInfo(bjID).bjName
}