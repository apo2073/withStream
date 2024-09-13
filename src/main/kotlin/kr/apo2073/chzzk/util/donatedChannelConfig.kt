package kr.apo2073.chzzk.util

import kr.apo2073.chzzk.Chk
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

var files= File("${Chk.instance!!.dataFolder}/chzzk_channel/donated"
    , "donated.yml")
var Dconfig: FileConfiguration = YamlConfiguration.loadConfiguration(files)

fun DconfigSave() {
    Dconfig.save(files)
}
fun DconfigReload() {
    files= File("${Chk.instance!!.dataFolder}/chzzk_channel/donated"
        , "donated.yml")
    Dconfig= YamlConfiguration.loadConfiguration(files)
}


var filess= File("${Chk.instance!!.dataFolder}/chzzk_channel/"
    , "connection.yml")
var Cconfig= YamlConfiguration.loadConfiguration(filess)
fun connectionSave() {
    Cconfig.save(filess)
}

fun CconfigReload() {
    filess= File("${Chk.instance!!.dataFolder}/chzzk_channel/"
        , "connection.yml")
    Cconfig= YamlConfiguration.loadConfiguration(filess)
}

fun removeCconfig() {
    if (filess.exists()) {
        filess.delete()
    }
}