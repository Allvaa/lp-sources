package me.allvaa.lpsourcesplugin

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "plugins.allvaalpsources")
@Component
data class PluginConfig(
    val activeSources: ArrayList<String> = arrayListOf("bilibili"),
    val bilibiliConfig: PluginBilibiliConfig = PluginBilibiliConfig()
) {
    data class PluginBilibiliConfig(
        var playlistPageCount: Int = -1
    )
}