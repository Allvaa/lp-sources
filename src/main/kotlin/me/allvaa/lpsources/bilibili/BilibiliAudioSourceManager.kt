package me.allvaa.lpsources.bilibili

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import org.apache.http.client.methods.HttpGet
import java.io.DataInput
import java.io.DataOutput

class BilibiliAudioSourceManager : AudioSourceManager {
    val httpInterface: HttpInterface = HttpClientTools.createDefaultThreadLocalManager().`interface`

    override fun getSourceName(): String {
        return "bilibili"
    }

    override fun loadItem(manager: AudioPlayerManager, reference: AudioReference): AudioItem? {
        val matcher = URL_PATTERN.matcher(reference.identifier)
        if (matcher.find()) {
            val type = matcher.group("type")
            val bvid = matcher.group("bvid")
            val page = matcher.group("page")?.toInt() ?: 0

            when (type) {
                "video" -> {
                    val response = httpInterface.execute(HttpGet("https://api.bilibili.com/x/web-interface/view?bvid=$bvid"))
                    val responseJson = JsonBrowser.parse(response.entity.content)
                    val statusCode = responseJson.get("code").`as`(Int::class.java)
                    if (statusCode != 0) {
                        return AudioReference.NO_TRACK
                    }

                    val trackData = responseJson.get("data")
                    return BilibiliAudioTrack(
                        AudioTrackInfo(
                            trackData.get("title").`as`(String::class.java),
                            trackData.get("owner").get("name").`as`(String::class.java),
                            trackData.get("duration").`as`(Long::class.java) * 1000,
                            bvid,
                            false,
                            getVideoURL(bvid)
                        ),
                        trackData.get("bvid").`as`(String::class.java),
                        trackData.get("cid").`as`(Long::class.java),
                        this
                    )
                }
            }
        }
        return null
    }

    override fun isTrackEncodable(track: AudioTrack): Boolean {
        return true
    }

    override fun encodeTrack(track: AudioTrack, output: DataOutput) {
        track as BilibiliAudioTrack
        DataFormatTools.writeNullableText(output, track.bvid)
        DataFormatTools.writeNullableText(output, track.cid.toString())
    }

    override fun decodeTrack(trackInfo: AudioTrackInfo, input: DataInput): AudioTrack {
        return BilibiliAudioTrack(trackInfo, DataFormatTools.readNullableText(input), DataFormatTools.readNullableText(input).toLong(), this)
    }

    override fun shutdown() {
        //
    }

    companion object {
        private val URL_PATTERN = Regex("^https?:\\/\\/(?:(?:www|m)\\.)?bilibili\\.com\\/(?<type>video)\\/(?<bvid>[A-Za-z0-9]+)\\/?(?:(?:\\?p=(?<page>[\\d]+)(?:&.+)?)?|(?:\\?.*)?)\$").toPattern()
        fun getVideoURL(id: String): String {
            return "https://www.bilibili.com/video/$id"
        }
    }
}