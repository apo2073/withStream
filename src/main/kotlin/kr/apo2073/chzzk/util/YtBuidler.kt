package kr.apo2073.chzzk.util

import com.google.api.services.youtube.YouTube
import java.util.*

lateinit var yt:MutableMap<UUID, YouTube>
fun YtBuilder(uuid: UUID, channelID:String) {
    val scopes= mutableListOf("https://www.googleapis.com/auth/youtube.readonly")

}

