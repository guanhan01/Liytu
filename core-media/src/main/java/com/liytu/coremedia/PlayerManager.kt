package com.liytu.coremedia

import android.content.Context
import android.media.audiofx.Equalizer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer

/** 播放模式：单曲循环 / 顺序播放 / 列表循环 / 随机播放 */
enum class PlayMode { SINGLE, SEQUENCE, LIST_LOOP, SHUFFLE }

/**
 * 统一播放管理器（本地音频播放）。
 * 播放模式基于 ExoPlayer repeatMode / shuffle 原生实现；
 * EQ 均衡器基于 android.media.audiofx（设备不支持时禁用）。
 */
class PlayerManager(context: Context) {

    val player: ExoPlayer

    var playMode: PlayMode = PlayMode.SEQUENCE
        private set

    /** 10 段均衡器（波段数取决于硬件，最多取 10） */
    private var _equalizer: Equalizer? = null
    val equalizer: Equalizer? get() = _equalizer

    init {
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        player = ExoPlayer.Builder(context, renderersFactory).build()
    }

    fun play(items: List<MediaItem>, startIndex: Int = 0) {
        player.setMediaItems(items, startIndex, 0L)
        player.prepare()
        player.play()
        initAudioFx()
    }

    fun play(item: MediaItem) = play(listOf(item), 0)

    fun seekToNext() {
        player.seekToNextMediaItem()
    }

    fun seekToPrevious() {
        player.seekToPreviousMediaItem()
    }

    /** 应用播放模式 */
    fun setPlayMode(mode: PlayMode): PlayMode {
        playMode = mode
        when (mode) {
            PlayMode.SINGLE -> {
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.shuffleModeEnabled = false
            }
            PlayMode.SEQUENCE -> {
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.shuffleModeEnabled = false
            }
            PlayMode.LIST_LOOP -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                player.shuffleModeEnabled = false
            }
            PlayMode.SHUFFLE -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                player.shuffleModeEnabled = true
            }
        }
        return mode
    }

    /** 循环切换：单曲 → 顺序 → 列表循环 → 随机 → 单曲 */
    fun cyclePlayMode(): PlayMode = setPlayMode(
        when (playMode) {
            PlayMode.SINGLE -> PlayMode.SEQUENCE
            PlayMode.SEQUENCE -> PlayMode.LIST_LOOP
            PlayMode.LIST_LOOP -> PlayMode.SHUFFLE
            PlayMode.SHUFFLE -> PlayMode.SINGLE
        }
    )

    /** 随机开关：开=随机；关=回到列表循环 */
    fun toggleShuffle(): PlayMode =
        if (playMode == PlayMode.SHUFFLE) setPlayMode(PlayMode.LIST_LOOP)
        else setPlayMode(PlayMode.SHUFFLE)

    /** 初始化均衡器（挂到当前音频会话；失败说明设备不支持） */
    fun initAudioFx() {
        if (_equalizer != null) return
        _equalizer = try {
            Equalizer(0, player.audioSessionId).apply { enabled = true }
        } catch (_: Exception) {
            try { Equalizer(0, 0).apply { enabled = true } } catch (_: Exception) { null }
        }
    }

    /** 当前音频会话 id（供 EQ 使用） */
    val audioSessionId: Int get() = player.audioSessionId

    fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    fun release() {
        _equalizer?.let { runCatching { it.release() } }
        _equalizer = null
        player.release()
    }
}
