package com.liytu.feature.video

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import android.app.Activity
import android.media.AudioManager
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.PlaybackParameters
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class VideoItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val durationMs: Long,
    val folder: String? = null,
)

@Composable
fun VideoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasVideoPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }

    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var imported by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshTick by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf<VideoItem?>(null) }

    LaunchedEffect(granted, refreshTick) {
        if (granted) {
            loading = true
            videos = scanVideos(context)
            loading = false
        } else {
            loading = false
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newItems = uris.mapIndexed { i, uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
                VideoItem(
                    id = -(System.nanoTime() + i),
                    uri = uri,
                    title = queryDisplayName(context, uri)?.substringBeforeLast('.')?.ifBlank { "视频 ${i + 1}" }
                        ?: "视频 ${i + 1}",
                    durationMs = 0L,
                )
            }
            imported = (imported + newItems).distinctBy { it.uri.toString() }
        }
    }

    val allVideos = remember(videos, imported) { videos + imported }

    val current = playing
    if (current != null) {
        val group = remember(allVideos, current.uri) {
            allVideos.filter { (it.folder ?: "") == (current.folder ?: "") }
                .sortedBy { it.title }
        }
        VideoPlayerPage(
            item = current,
            group = group,
            onBack = { playing = null },
            onSwitchTo = { playing = it },
        )
    } else {
        Column(
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "影视",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        if (granted) "本地视频 · ${allVideos.size} 个" else "需要媒体权限才能扫描本地视频",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "刷新",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { refreshTick++ }
                        .padding(8.dp)
                        .size(20.dp),
                )
                Spacer(Modifier.width(6.dp))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable { importLauncher.launch(arrayOf("video/*")) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("导入", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(6.dp))

            when {
                !granted -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("尚未授予媒体权限", color = Color.White.copy(alpha = 0.85f))
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .clickable { permissionLauncher.launch(videoPermission()) }
                                .padding(horizontal = 22.dp, vertical = 12.dp)
                        ) {
                            Text("授予视频权限", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "也可以直接「导入」文件播放",
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.55f),
                        )
                    }
                }
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
                allVideos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(52.dp),
                        )
                        Spacer(Modifier.height(10.dp))
                        Text("未找到本地视频", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "把 mp4 / mkv 放入 Movies 目录，或点下方按钮导入",
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(18.dp))
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .clickable { importLauncher.launch(arrayOf("video/*")) }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("导入视频文件", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(allVideos) { index, video ->
                        VideoCard(video = video, index = index, onClick = { playing = video })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun VideoCard(video: VideoItem, index: Int, onClick: () -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(20.dp))
                .background(videoBrush(index)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(40.dp),
            )
            // 时长角标
            if (video.durationMs > 0) {
                Text(
                    formatDuration(video.durationMs),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            video.title,
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VideoPlayerPage(
    item: VideoItem,
    group: List<VideoItem>,
    onBack: () -> Unit,
    onSwitchTo: (VideoItem) -> Unit,
) {
    val context = LocalContext.current
    val player = remember(item.uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(item.uri))
            prepare()
            playWhenReady = true
        }
    }

    // ---- 手势 & 控制状态 ----
    var controlsVisible by remember { mutableStateOf(true) }
    var hud by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var draggingProgress by remember { mutableStateOf<Float?>(null) }
    var dragMode by remember { mutableIntStateOf(0) }
    var startBright by remember { mutableFloatStateOf(-1f) }
    var startVol by remember { mutableIntStateOf(-1) }
    var seekPreview by remember { mutableLongStateOf(0L) }
    var speed by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(speed) {
        player.playbackParameters = PlaybackParameters(speed)
    }
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVol = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }

    LaunchedEffect(player) {
        while (true) {
            val dur = player.duration.takeIf { it > 0 } ?: 0L
            if (dur > 0 && draggingProgress == null) {
                progress = (player.currentPosition.toFloat() / dur).coerceIn(0f, 1f)
            }
            delay(400)
        }
    }

    BackHandler { onBack() }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        // 手势层：单击控制显隐 / 双击播放暂停 / 横滑进度 / 左半竖滑亮度 右半竖滑音量
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(player) {
                    detectTapGestures(
                        onTap = { controlsVisible = !controlsVisible },
                        onDoubleTap = {
                            if (player.isPlaying) player.pause() else player.play()
                        },
                    )
                }
                .pointerInput(player) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragMode = 0
                            if (offset.x < size.width / 2f) {
                                val act = context as? Activity
                                startBright = act?.window?.attributes?.screenBrightness?.takeIf { it >= 0f } ?: 0.5f
                                startVol = -1
                            } else {
                                startVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                                startBright = -1f
                            }
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            if (dragMode == 0) {
                                dragMode = if (kotlin.math.abs(amount.x) > kotlin.math.abs(amount.y)) 1 else 2
                            }
                            when (dragMode) {
                                1 -> {
                                    val dur = player.duration.takeIf { it > 0 } ?: 0L
                                    if (dur > 0) {
                                        val deltaMs = (-amount.x / size.width * dur).toLong()
                                        seekPreview = (player.currentPosition + deltaMs).coerceIn(0L, dur)
                                        hud = videoDeltaText(seekPreview - player.currentPosition)
                                    }
                                }
                                else -> {
                                    if (startBright >= 0f) {
                                        val act = context as? Activity
                                        if (act != null) {
                                            val b = (startBright - amount.y / size.height * 1.2f).coerceIn(0.05f, 1f)
                                            act.window.attributes = act.window.attributes.apply { screenBrightness = b }
                                            hud = "亮度 ${(b * 100).toInt()}%"
                                        }
                                    } else if (startVol >= 0) {
                                        val dv = (-amount.y / (size.height / 2f) * maxVol).toInt()
                                        val v = (startVol + dv).coerceIn(0, maxVol)
                                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, v, 0)
                                        hud = "音量 ${(v * 100 / maxVol)}%"
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            if (dragMode == 1 && seekPreview > 0) player.seekTo(seekPreview)
                            dragMode = 0
                            startBright = -1f
                            startVol = -1
                            hud = null
                        },
                        onDragCancel = {
                            dragMode = 0
                            startBright = -1f
                            startVol = -1
                            hud = null
                        },
                    )
                },
        )
        // HUD 提示
        hud?.let { msg ->
            Box(
                Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(msg, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        // ---- 顶部控制（随 controlsVisible 显隐） ----
        androidx.compose.animation.AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.TopStart),
        ) {
        Column {
        Row(
            Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onBack)
                    .padding(10.dp)
                    .size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                item.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            // 倍速
            Box(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable {
                        val speeds = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)
                        speed = speeds[(speeds.indexOf(speed) + 1) % speeds.size]
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    if (speed == 1f) "1.0x" else "${speed}x",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        // 选集行
        val idx = group.indexOfFirst { it.uri == item.uri }
        val prev = if (idx > 0) group.getOrNull(idx - 1) else null
        val next = group.getOrNull(idx + 1)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .padding(top = 52.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (prev != null) Color.Black.copy(alpha = 0.35f) else Color.Transparent)
                    .clickable(enabled = prev != null) { prev?.let(onSwitchTo) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "上一集",
                    tint = if (prev != null) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "上一集",
                    color = if (prev != null) Color.White else Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "${idx + 1} / ${group.size}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
            )
            Spacer(Modifier.weight(1f))
            Row(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (next != null) Color.Black.copy(alpha = 0.35f) else Color.Transparent)
                    .clickable(enabled = next != null) { next?.let(onSwitchTo) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "下一集",
                    color = if (next != null) Color.White else Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "下一集",
                    tint = if (next != null) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        }
        }
        // ---- 底部控制条（随 controlsVisible 显隐） ----
        androidx.compose.animation.AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Slider(
                    value = draggingProgress ?: progress,
                    onValueChange = { draggingProgress = it },
                    onValueChangeFinished = {
                        val dur = player.duration.takeIf { it > 0 } ?: 0L
                        if (dur > 0) {
                            player.seekTo(((draggingProgress ?: 0f) * dur).toLong())
                        }
                        draggingProgress = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (player.isPlaying) player.pause() else player.play() }) {
                        if (player.isPlaying) {
                            Row(
                                Modifier.size(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.width(5.dp).height(18.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                                Box(Modifier.width(5.dp).height(18.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                            }
                        } else {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "播放",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    val dur = player.duration.takeIf { it > 0 } ?: 0L
                    val shown = if (draggingProgress != null) (draggingProgress!! * dur).toLong() else player.currentPosition
                    Text(
                        "${formatDuration(shown)} / ${formatDuration(dur)}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }
}

private fun videoDeltaText(ms: Long): String {
    val s = ms / 1000
    return if (s >= 0) "+${s / 60}:%02d".format(s % 60) else "-${(-s) / 60}:%02d".format((-s) % 60)
}

private fun videoBrush(index: Int): Brush {
    val palettes = listOf(
        listOf(0xFF5C6BC0, 0xFF8E24AA),
        listOf(0xFF00897B, 0xFF3949AB),
        listOf(0xFFF4511E, 0xFF6A1B9A),
        listOf(0xFF546E7A, 0xFF00695C),
        listOf(0xFF7B1FA2, 0xFF303F9F),
    )
    val p = palettes[index % palettes.size]
    return Brush.linearGradient(
        listOf(Color(p[0]).copy(alpha = 0.85f), Color(p[1]).copy(alpha = 0.85f))
    )
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}

private fun videoPermission(): String =
    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_VIDEO
    else Manifest.permission.READ_EXTERNAL_STORAGE

private fun hasVideoPermission(context: Context): Boolean {
    val permission = videoPermission()
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(
            uri, null, null, null, null
        )?.use { c ->
            val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        }
    } catch (_: Exception) {
        null
    }
}

private suspend fun scanVideos(context: Context): List<VideoItem> = withContext(Dispatchers.IO) {
    val result = mutableListOf<VideoItem>()
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.DURATION,
    )
    context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        "${MediaStore.Video.Media.DATE_ADDED} DESC"
    )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            val data = cursor.getString(dataCol)
            result += VideoItem(
                id = id,
                uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id),
                title = cursor.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "本地视频 ${id}",
                durationMs = cursor.getLong(durationCol),
                folder = data?.let { java.io.File(it).parentFile?.name },
            )
        }
    }
    result
}
