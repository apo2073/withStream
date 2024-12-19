package kr.apo2073.stream

import com.outstandingboy.donationalert.platform.Toonation
import kr.apo2073.stream.utilities.Setting
import kr.apo2073.stream.utilities.versions.Managers.printLogo
import me.taromati.afreecatv.AfreecatvAPI
import org.bukkit.plugin.java.JavaPlugin
import xyz.r2turntrue.chzzk4j.Chzzk
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import java.util.*

lateinit var chzzk: Chzzk
lateinit var cht: MutableMap<UUID, ChzzkChat>
lateinit var tn: MutableMap<UUID, Toonation>
lateinit var af:MutableMap<UUID, AfreecatvAPI>
class Stream : JavaPlugin() {
    companion object { lateinit var instance: Stream }
    fun getVersion():String= description.version

    override fun onEnable() {
        instance=this
        printLogo()
        Setting(this).onEnable()
    }

    override fun onDisable() {
        Setting(this).onDisable()
    }
}