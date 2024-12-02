package kr.apo2073.stream.util

import com.google.api.services.youtube.YouTube
import java.util.*

var youtubeAPI="AIzaSyBpMcjduOo5VbaWa-ptNGuGsG323gaop60"
var googleAPIURI="https://www.googleapis.com/youtube/v3/search?part=snippet&channelId={YOUR_CHANNEL_ID}&eventType=live&type=video&key={YOUR_API_KEY}"

lateinit var yt:MutableMap<UUID, YouTube>
lateinit var yyt:MutableMap<UUID,String>
suspend fun YtBuilder(uuid: UUID, channelID:String) {

}