package kr.apo2073.stream.builders

import com.google.api.services.youtube.YouTube
import java.util.*

var youtubeAPI="AIzaSyBpMcjduOo5VbaWa-ptNGuGsG323gaop60"
var googleAPIURI="https://www.googleapis.com/youtube/v3/search?part=snippet&channelId={YOUR_CHANNEL_ID}&eventType=live&type=video&key=$youtubeAPI"

lateinit var yt:MutableMap<UUID, YouTube>
lateinit var yyt:MutableMap<UUID,String>
fun YtBuilder(uuid: UUID, channelID:String) {

}