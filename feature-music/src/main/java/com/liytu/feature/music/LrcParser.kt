package com.liytu.feature.music

import android.content.Context
import android.net.Uri
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
 * 歌词加载（优先级）：
 * 1. 音频内嵌 ID3 USLT 歌词标签 —— 通过 content:// 流读取（分区存储下唯一可靠方式）
 * 2. 同目录同名 .lrc 文件（MediaStore DATA 路径；仅旧系统 / 文件权限放行时可用）
 */
fun loadLrcFromTrack(context: Context, track: TrackItem): List<LrcLine> {
    val embedded = extractEmbeddedLrc(context, track.uri)?.takeIf { it.isNotBlank() }?.let { parseLrc(it) }
    if (!embedded.isNullOrEmpty()) return embedded

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

/** 从 content:// 流中提取 ID3v2 USLT 歌词文本 */
fun extractEmbeddedLrc(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val header = ByteArray(10)
            if (input.read(header) < 10) return@use null
            if (header[0] != 'I'.code.toByte() || header[1] != 'D'.code.toByte() || header[2] != '3'.code.toByte()) {
                return@use null
            }
            val version = header[3].toInt() and 0xFF
            val syncSafe = { b: ByteArray, off: Int ->
                ((b[off].toInt() and 0x7f) shl 21) or
                    ((b[off + 1].toInt() and 0x7f) shl 14) or
                    ((b[off + 2].toInt() and 0x7f) shl 7) or
                    (b[off + 3].toInt() and 0x7f)
            }
            val tagSize = syncSafe(header, 6).coerceAtMost(512 * 1024)
            val body = ByteArray(tagSize)
            var total = 0
            while (total < tagSize) {
                val n = input.read(body, total, tagSize - total)
                if (n <= 0) break
                total += n
            }
            parseId3Uslt(body.copyOf(total), version)
        }
    } catch (_: Exception) {
        null
    }
}

/** 遍历 ID3v2 帧，返回 USLT 帧文本 */
private fun parseId3Uslt(data: ByteArray, version: Int): String? {
    var i = 0
    // 跳过扩展头（v2.3 4 字节大端长度；v2.4 syncsafe 长度）
    val headerFlags = 0
    if (headerFlags and 0x40 != 0) {
        // 简化：本库标签由 mutagen 生成，无扩展头；这里做防御
        if (data.size < 4) return null
        val extSize = if (version >= 4) {
            ((data[0].toInt() and 0x7f) shl 21) or ((data[1].toInt() and 0x7f) shl 14) or
                ((data[2].toInt() and 0x7f) shl 7) or (data[3].toInt() and 0x7f)
        } else {
            ((data[0].toInt() and 0xff) shl 24) or ((data[1].toInt() and 0xff) shl 16) or
                ((data[2].toInt() and 0xff) shl 8) or (data[3].toInt() and 0xff)
        }
        i = 4 + if (version >= 4) extSize else extSize
    }
    while (i + 10 <= data.size) {
        val id = String(data, i, 4, Charsets.US_ASCII)
        if (id == "\u0000\u0000\u0000\u0000") break
        val rawSize =
            ((data[i + 4].toInt() and 0xff) shl 24) or ((data[i + 5].toInt() and 0xff) shl 16) or
                ((data[i + 6].toInt() and 0xff) shl 8) or (data[i + 7].toInt() and 0xff)
        val size = if (version >= 4) {
            // v2.4 syncsafe
            ((data[i + 4].toInt() and 0x7f) shl 21) or ((data[i + 5].toInt() and 0x7f) shl 14) or
                ((data[i + 6].toInt() and 0x7f) shl 7) or (data[i + 7].toInt() and 0x7f)
        } else rawSize
        if (id == "USLT") {
            val bodyOff = i + 10
            val bodySize = size.coerceAtMost(data.size - bodyOff)
            if (bodySize < 4) return null
            val enc = data[bodyOff].toInt() and 0xff
            var p = bodyOff + 4
            val frameEnd = bodyOff + bodySize
            // 跳过描述符（encoding 决定终止字节）
            if (enc == 0 || enc == 3) {
                while (p < frameEnd && data[p] != 0.toByte()) p++
                p++
            } else {
                while (p + 1 < frameEnd && !(data[p] == 0.toByte() && data[p + 1] == 0.toByte())) p += 2
                p += 2
            }
            if (p >= frameEnd) return null
            val textBytes = data.copyOfRange(p, frameEnd)
            return when (enc) {
                0 -> String(textBytes, Charsets.ISO_8859_1)
                1 -> String(textBytes, Charsets.UTF_16)
                2 -> String(textBytes, Charsets.UTF_16BE)
                3 -> String(textBytes, Charsets.UTF_8)
                else -> null
            }
        }
        i += 10 + size
    }
    return null
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

/**
 * 智能歌词加载（优先级）：
 * 1. TrackItem.lyricUri（SAF 文件夹导入时匹配到的同名 .lrc）
 * 2. 音频内嵌 ID3 USLT 歌词标签
 * 3. 同目录同名 .lrc 文件（MediaStore DATA 路径）
 */
fun loadLrcSmart(context: Context, track: TrackItem): List<LrcLine> {
    track.lyricUri?.let { lrcUri ->
        return try {
            val bytes = context.contentResolver.openInputStream(lrcUri)?.use { it.readBytes() }
                ?: return emptyList()
            parseLrc(decodeText(bytes))
        } catch (_: Exception) {
            emptyList()
        }
    }
    return loadLrcFromTrack(context, track)
}
