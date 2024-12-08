package kr.apo2073.stream.builders

import kr.apo2073.stream.config.ConnectionConfig.connectionSave
import kr.apo2073.stream.config.ConnectionConfig.getConnectionConfig
import kr.apo2073.stream.events.YouTubeListener
import kr.apo2073.stream.utilities.versions.Managers.prefix
import kr.apo2073.stream.utilities.versions.Managers.sendMessage
import kr.apo2073.ytliv.YouTubeBuilder
import kr.apo2073.ytliv.Youtube
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.util.*

lateinit var yt:MutableMap<UUID, Youtube>
lateinit var yyt:MutableMap<UUID,String>
fun YtBuilder(uuid: UUID, videoId:String) {
    val player=Bukkit.getPlayer(uuid) ?: return
    try {
        yyt[uuid]=videoId
        yt[uuid]=YouTubeBuilder()
            .setApiKey("AIzaSyBpMcjduOo5VbaWa-ptNGuGsG323gaop60")
            .setVideoId(videoId)
            .addListener(YouTubeListener())
            .build()
        val channelInfo=yt[uuid]?.channelInfo() ?: run {
            sendMessage(prefix.append(Component.text("해당 채널이 존재하지 않습니다")), Bukkit.getPlayer(uuid) ?: return)
            return
        }
        if (channelInfo.channelName==null) {
            sendMessage(prefix.append(Component.text("해당 채널이 존재하지 않습니다")), Bukkit.getPlayer(uuid) ?: return)
            return
        }
        sendMessage(prefix.append(Component.text("채널 ${channelInfo.channelName}( ${channelInfo.subscriptionCount} 구독자 ) 에 연결했습니다.")), player)
    } catch (e: Exception) {
        e.printStackTrace()
        getConnectionConfig().set(videoId, null)
        connectionSave()
    }
}

