package com.liytu.coremedia

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer

/**
 * 统一播放管理器（第一阶段：本地音频播放）。
 * 阶段 5 会升级为 MediaSessionService + PlaybackQueue。
 */
class PlayerManager(context: Context) {

    val player: ExoPlayer

    init {
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        player = ExoPlayer.Builder(context, renderersFactory).build()
    }

    fun play(items: List<MediaItem>, startIndex: Int = 0) {
        player.setMediaItems(items, startIndex, 0L)
        player.prepare()
        player.play()
    }

    fun play(item: MediaItem) = play(listOf(item), 0)

    fun seekToNext() {
        player.seekToNextMediaItem()
    }

    fun seekToPrevious() {
        player.seekToPreviousMediaItem()
    }

    /** 播放结束自动连播 */
    val onMediaItemTransition: ((Int, MediaItem) -> Unit)?
        get() = null

    fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    fun release() {
        player.release()
    }
}
