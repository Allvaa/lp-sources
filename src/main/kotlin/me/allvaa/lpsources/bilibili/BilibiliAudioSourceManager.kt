package me.allvaa.lpsources.bilibili

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface
import com.sedmelluq.discord.lavaplayer.track.*
import org.apache.http.client.methods.HttpGet
import java.io.DataInput
import java.io.DataOutput

class BilibiliAudioSourceManager : AudioSourceManager {
    val httpInterface: HttpInterface

    init {
        val httpInterfacaManager = HttpClientTools.createDefaultThreadLocalManager()
        httpInterfacaManager.setHttpContextFilter(BilibiliHttpContextFilter())
        httpInterface = httpInterfacaManager.`interface`
    }

    override fun getSourceName(): String {
        return "bilibili"
    }

    override fun loadItem(manager: AudioPlayerManager, reference: AudioReference): AudioItem? {
        val matcher = URL_PATTERN.matcher(reference.identifier)
        if (matcher.find()) {
            when (matcher.group("type")) {
                "video" -> {
                    val bvid = matcher.group("id")
                    val page = (matcher.group("page")?.toInt() ?: 1) - 1

                    val response = httpInterface.execute(HttpGet("${BASE_URL}x/web-interface/view?bvid=$bvid"))
                    val responseJson = JsonBrowser.parse(response.entity.content)

                    val statusCode = responseJson.get("code").`as`(Int::class.java)
                    if (statusCode != 0) {
                        return AudioReference.NO_TRACK
                    }

                    val trackData = responseJson.get("data")
                    return if (trackData.get("pages").values().size > 1) {
                        loadVideoAnthology(trackData, page)
                    } else {
                        loadVideo(trackData)
                    }
                }
                "audio" -> {
                    val type = when (matcher.group("audioType")) {
                        "am" -> "menu"
                        "au" -> "song"
                        else -> null
                    }
                    val sid = matcher.group("audioId")

                    val response = httpInterface.execute(HttpGet("${BASE_URL}audio/music-service-c/web/$type/info?sid=$sid"))
                    val responseJson = JsonBrowser.parse(response.entity.content)

                    val statusCode = responseJson.get("code").`as`(Int::class.java)
                    if (statusCode != 0) {
                        return AudioReference.NO_TRACK
                    }

                    return when (type) {
                        "song" -> loadAudio(responseJson.get("data"))
                        else -> AudioReference.NO_TRACK
                    }
                }
            }
        }
        return null
    }

    private fun loadVideo(trackData: JsonBrowser): AudioItem {
        val bvid = trackData.get("bvid").`as`(String::class.java)

        return BilibiliAudioTrack(
            AudioTrackInfo(
                trackData.get("title").`as`(String::class.java),
                trackData.get("owner").get("name").`as`(String::class.java),
                trackData.get("duration").asLong(0) * 1000,
                bvid,
                false,
                getVideoURL(bvid)
            ),
            BilibiliAudioTrack.TrackType.VIDEO,
            bvid,
            trackData.get("cid").asLong(0),
            this
        )
    }

    private fun loadVideoAnthology(trackData: JsonBrowser, page: Int): AudioItem {
        val playlistName = trackData.get("title").`as`(String::class.java)
        val author = trackData.get("owner").get("name").`as`(String::class.java)
        val bvid = trackData.get("bvid").`as`(String::class.java)

        val tracks = ArrayList<AudioTrack>()

        for (item in trackData.get("pages").values()) {
            tracks.add(BilibiliAudioTrack(
                AudioTrackInfo(
                    item.get("part").`as`(String::class.java),
                    author,
                    item.get("duration").asLong(0) * 1000,
                    bvid,
                    false,
                    getVideoURL(bvid, item.get("page").`as`(Int::class.java))
                ),
                BilibiliAudioTrack.TrackType.VIDEO,
                bvid,
                item.get("cid").asLong(0),
                this
            ))
        }

        return BasicAudioPlaylist(playlistName, tracks, tracks[page], false)
    }

    private fun loadAudio(trackData: JsonBrowser): AudioItem {
        val sid = trackData.get("statistic").get("sid").asLong(0).toString()

        return BilibiliAudioTrack(
            AudioTrackInfo(
                trackData.get("title").`as`(String::class.java),
                trackData.get("uname").`as`(String::class.java),
                trackData.get("duration").asLong(0) * 1000,
                "au$sid",
                false,
                getAudioURL("au$sid")
            ),
            BilibiliAudioTrack.TrackType.AUDIO,
            sid,
            null,
            this
        )
    }

    override fun isTrackEncodable(track: AudioTrack): Boolean {
        return true
    }

    override fun encodeTrack(track: AudioTrack, output: DataOutput) {
        track as BilibiliAudioTrack
        DataFormatTools.writeNullableText(output, track.type.toString())
        DataFormatTools.writeNullableText(output, track.id)
        DataFormatTools.writeNullableText(output, track.cid.toString())
    }

    override fun decodeTrack(trackInfo: AudioTrackInfo, input: DataInput): AudioTrack {
        return BilibiliAudioTrack(trackInfo, DataFormatTools.readNullableText(input).toInt() as BilibiliAudioTrack.TrackType, DataFormatTools.readNullableText(input), DataFormatTools.readNullableText(input).toLong(), this)
    }

    override fun shutdown() {
        //
    }

    companion object {
        const val BASE_URL = "https://api.bilibili.com/"
        private val URL_PATTERN = Regex("^https?:\\/\\/(?:(?:www|m)\\.)?bilibili\\.com\\/(?<type>video|audio)\\/(?<id>(?:(?<audioType>am|au)?(?<audioId>[0-9]{6,}))|[A-Za-z0-9]+)\\/?(?:(?:\\?p=(?<page>[\\d]+)(?:&.+)?)?|(?:\\?.*)?)\$").toPattern()

        private fun getVideoURL(id: String, page: Int? = null): String {
            return "https://www.bilibili.com/video/$id${if (page != null) "?p=$page" else ""}"
        }

        private fun getAudioURL(id: String): String {
            return "https://www.bilibili.com/audio/$id"
        }
    }
}