package me.allvaa.lpsources.bilibili

import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor
import org.apache.http.client.methods.HttpGet
import java.net.URI

class BilibiliAudioTrack(audioTrackInfo: AudioTrackInfo, val bvid: String, val cid: Long, private val sourceManager: BilibiliAudioSourceManager) : DelegatedAudioTrack(audioTrackInfo) {
    override fun process(executor: LocalAudioTrackExecutor) {
        val stream = PersistentHttpStream(sourceManager.httpInterface, URI(getPlaybackURL()), null)
        processDelegate(MpegAudioTrack(trackInfo, stream), executor)
    }

    private fun getPlaybackURL(): String {
        val response = sourceManager.httpInterface.execute(HttpGet("https://api.bilibili.com/x/player/playurl?bvid=$bvid&cid=$cid&fnval=16"))
        val responseJson = JsonBrowser.parse(response.entity.content)
        return responseJson
            .get("data")
            .get("dash")
            .get("audio")
            .values()
            // find the highest quality possible
            .sortedByDescending {
                it.get("id").`as`(Int::class.java)
            }[0]
            .get("baseUrl").`as`(String::class.java)
    }

    override fun makeShallowClone(): AudioTrack {
        return BilibiliAudioTrack(trackInfo, bvid, cid, sourceManager)
    }

    override fun getSourceManager(): AudioSourceManager {
        return sourceManager
    }
}