package kr.apo2073.chzzk.util

import com.google.api.services.youtube.YouTube
import kr.apo2073.chzzk.Chk
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

var youtubeAPI="AIzaSyBpMcjduOo5VbaWa-ptNGuGsG323gaop60"
var googleAPIURI="https://www.googleapis.com/youtube/v3/search?part=snippet&channelId={YOUR_CHANNEL_ID}&eventType=live&type=video&key={YOUR_API_KEY}"

lateinit var yt:MutableMap<UUID, YouTube>
lateinit var yyt:MutableMap<UUID,String>
suspend fun YtBuilder(uuid: UUID, channelID:String) {

}