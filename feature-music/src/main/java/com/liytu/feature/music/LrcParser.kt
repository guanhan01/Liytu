package com.liytu.feature.music

import android.content.Context
import android.provider.MediaStore
import java.io.File
import java.nio.charset.Charset

/** 一行 LRC 歌词 */
data class LrcLine(val timeMs: Long, val text: String)

private val timeTagRegex = Regex("""\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")

/**
 * 解析 LRC 文本为时间排序的歌词行。
 * 支持多时间标签（如 [00:12.00][00:45.00] 副歌）、[offset:±ms] 全局偏移、空行过滤。
 */
fun parseLrc(content: String): List<LrcLine> {
    var offsetMs = 0L
    val out = mutableListOf<LrcLine>()
    content.lineSequence().forEach { raw ->
        val line = raw.trim()
        if (line.isEmpty()) return@forEach
        val matches = timeTagRegex.findAll(line).toList()
        if (matches.isEmpty()) {
            val offsetMatch = Regex("""\[offset:\s*([+-]?\d+)]""", RegexOption.IGNORE_CASE).find(line)
            offsetMatch?.let { offsetMs = it.groupValues[1].toLongOrNull() ?: 0L }
            return@forEach
        }
        val text = line.substring(matches.last().range.last + 1).trim()
        matches.forEach { m ->
            val min = m.groupValues[1].toLongOrNull() ?: 0L
            val sec = m.groupValues[2].toLongOrNull() ?: 0L
            val fracStr = m.groupValues[3]
            val fracMs = when (fracStr.length) {
                1 -> (fracStr.toLongOrNull() ?: 0L) * 100
                2 -> (fracStr.toLongOrNull() ?: 0L) * 10
                3 -> fracStr.toLongOrNull() ?: 0L
                else -> 0L
            }
            val time = (min * 60000 + sec * 1000 + fracMs - offsetMs).coerceAtLeast(0L)
            if (text.isNotEmpty()) out += LrcLine(time, text)
        }
    }
    return out.sortedBy { it.timeMs }
}

/**
 * 读取歌曲同目录同名 .lrc 文件（基于 MediaStore DATA 路径）。
 * SAF 导入的歌曲无文件路径，返回空列表（可接受）。
 */
fun loadLrcFromTrack(context: Context, track: TrackItem): List<LrcLine> {
    val path = queryAudioPath(context, track.id) ?: return emptyList()
    val file = File(path)
    val lrcFile = file.parentFile?.let { File(it, file.nameWithoutExtension + ".lrc") } ?: return emptyList()
    if (!lrcFile.exists() || !lrcFile.canRead()) return emptyList()
    return try {
        parseLrc(decodeText(lrcFile.readBytes()))
    } catch (_: Exception) {
        emptyList()
    }
}

/** 按 MediaStore _id 查询音频文件路径 */
fun queryAudioPath(context: Context, audioId: Long): String? {
    return try {
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.DATA),
            "_id=?",
            arrayOf(audioId.toString()),
            null
        )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    } catch (_: Exception) {
        null
    }
}

/** UTF-8 优先解码，失败回退 GBK（兼容老 LRC） */
private fun decodeText(bytes: ByteArray): String {
    return try {
        val utf8 = String(bytes, Charsets.UTF_8)
        if (utf8.contains('\uFFFD')) throw IllegalArgumentException()
        utf8
    } catch (_: Exception) {
        try {
            String(bytes, Charset.forName("GBK"))
        } catch (_: Exception) {
            ""
        }
    }
}
