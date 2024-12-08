package kr.apo2073.stream.builders

import kr.apo2073.stream.Stream
import kr.apo2073.stream.cht
import kr.apo2073.stream.chzzk
import kr.apo2073.stream.config.ConnectionConfig.connectionSave
import kr.apo2073.stream.config.ConnectionConfig.getConnectionConfig
import kr.apo2073.stream.utilities.ChzzkEvents
import kr.apo2073.stream.utilities.versions.Managers.prefix
import kr.apo2073.stream.utilities.versions.Managers.sendMessage
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import xyz.r2turntrue.chzzk4j.exception.ChannelNotExistsException
import java.io.File
import java.util.*

fun ChkBuilder(uuid: UUID, id: String) {
    val player= Bukkit.getPlayer(uuid) ?:return
    try {
        val chz= chzzk
        var cht= cht[uuid]
        val ch= chz.getChannel(id)
        cht = chz.chat(id)?.withChatListener(ChzzkEvents())?.build()
        cht?.connectBlocking()
        sendMessage(prefix.append(Component.text("채널 ${ch?.channelName}( ${ch?.followerCount} 팔로워 )에 연결했습니다")), player)

        val file= File("${Stream.instance.dataFolder}/channel", "${uuid}.yml")
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        config.save(file)

    }catch (e:ChannelNotExistsException) {
        sendMessage(prefix.append(Component.text("해당 채널이 존재하지 않습니다")), player)
        getConnectionConfig().set(id, null)
        connectionSave()
    } catch (e:Exception) {
        e.printStackTrace()
        getConnectionConfig().set(id, null)
        connectionSave()
    }
}
