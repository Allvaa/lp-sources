package me.allvaa.lpsourcesplugin

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration
import me.allvaa.lpsources.bilibili.BilibiliAudioSourceManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class Plugin(private val config: PluginConfig) : AudioPlayerManagerConfiguration {
    init {
        log.info("Starting allvaa-lpsources plugin.")
    }

    override fun configure(manager: AudioPlayerManager): AudioPlayerManager {
        if (config.activeSources.contains("bilibili")) {
            manager.registerSourceManager(
                BilibiliAudioSourceManager()
                    .setPlaylistPageCount(config.bilibiliConfig.playlistPageCount)
            )
            log.info("Registered Bilibili source manager.")
        }

        return manager
    }

    companion object {
        private val log = LoggerFactory.getLogger(Plugin::class.java)
    }
}