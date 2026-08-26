package com.liytu.feature.music

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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.liytu.coremedia.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class TrackItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val durationMs: Long,
)

@Composable
fun MusicScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasAudioPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }

    var tracks by remember { mutableStateOf<List<TrackItem>>(emptyList()) }
    var imported by remember { mutableStateOf<List<TrackItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var currentIndex by remember { mutableIntStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var showPlayer by remember { mutableStateOf(false) }

    val playerManager = remember { PlayerManager(context.applicationContext) }
    val listener = remember {
        object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentIndex = playerManager.player.currentMediaItemIndex
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    // 就绪后刷新当前曲目
                    currentIndex = playerManager.player.currentMediaItemIndex
                }
            }
        }
    }
    DisposableEffect(playerManager) {
        playerManager.addListener(listener)
        onDispose { playerManager.release() }
    }

    // 扫描
    LaunchedEffect(granted) {
        if (granted) {
            loading = true
            tracks = scanTracks(context)
            loading = false
        } else {
            loading = false
        }
    }
    // 全局播放位置轮询（供全屏播放器读取）
    LaunchedEffect(isPlaying) {
        while (true) {
            GlobalPositionHolder.position = playerManager.player.currentPosition
            delay(400)
        }
    }
    // 刷新按钮
    var refreshTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(refreshTick) {
        if (granted && refreshTick > 0) {
            loading = true
            tracks = scanTracks(context)
            loading = false
        }
    }

    // SAF 导入
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
                val name = queryDisplayName(context, uri) ?: "音乐文件 ${i + 1}"
                TrackItem(
                    id = -(System.nanoTime() + i),
                    uri = uri,
                    title = name.substringBeforeLast('.').ifBlank { name },
                    artist = "导入",
                    durationMs = 0L,
                )
            }
            imported = (imported + newItems).distinctBy { it.uri.toString() }
        }
    }

    val allTracks = remember(tracks, imported) { tracks + imported }
    val currentTrack = allTracks.getOrNull(currentIndex)

    // 播放
    fun playAt(index: Int) {
        if (index < 0 || index >= allTracks.size) return
        playerManager.play(allTracks.map { MediaItem.fromUri(it.uri) }, index)
        currentIndex = index
        showPlayer = true
    }

    if (showPlayer && currentTrack != null) {
        PlayerScreen(
            track = currentTrack,
            isPlaying = isPlaying,
            positionMs = playerManager.player.currentPosition,
            durationMs = if (playerManager.player.duration > 0) playerManager.player.duration else currentTrack.durationMs,
            onBack = { showPlayer = false },
            onTogglePlay = {
                if (playerManager.player.isPlaying) playerManager.player.pause() else {
                    if (playerManager.player.playbackState == Player.STATE_IDLE) {
                        playerManager.play(allTracks.map { MediaItem.fromUri(it.uri) }, currentIndex)
                    } else {
                        playerManager.player.play()
                    }
                }
            },
            onPrevious = { if (allTracks.isNotEmpty()) { playerManager.player.seekToPreviousMediaItem(); currentIndex = playerManager.player.currentMediaItemIndex } },
            onNext = { if (allTracks.isNotEmpty()) { playerManager.player.seekToNextMediaItem(); currentIndex = playerManager.player.currentMediaItemIndex } },
            onSeek = { pos -> playerManager.player.seekTo(pos) },
            modifier = modifier,
        )
    } else {
        MusicListScreen(
            granted = granted,
            loading = loading,
            tracks = allTracks,
            currentIndex = currentIndex,
            isPlaying = isPlaying,
            onRequestPermission = { permissionLauncher.launch(audioPermission()) },
            onRefresh = { refreshTick++ },
            onImport = { importLauncher.launch(arrayOf("audio/*")) },
            onTrackClick = { playAt(it) },
            onOpenPlayer = { if (currentTrack != null) showPlayer = true },
            isMiniVisible = currentTrack != null,
            currentTrack = currentTrack,
            onMiniPlayToggle = {
                if (playerManager.player.isPlaying) playerManager.player.pause() else playerManager.player.play()
            },
            modifier = modifier,
        )
    }
}

/* ---------------- 列表页 ---------------- */

@Composable
private fun MusicListScreen(
    granted: Boolean,
    loading: Boolean,
    tracks: List<TrackItem>,
    currentIndex: Int,
    isPlaying: Boolean,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    onImport: () -> Unit,
    onTrackClick: (Int) -> Unit,
    onOpenPlayer: () -> Unit,
    isMiniVisible: Boolean,
    currentTrack: TrackItem?,
    onMiniPlayToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "音乐",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    if (granted) "本地曲库 · ${tracks.size} 首" else "需要媒体权限才能扫描本地音乐",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
            // 刷新
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "刷新", tint = Color.White.copy(alpha = 0.85f))
            }
            // 导入
            Row(
                Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable(onClick = onImport)
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
                            .clickable(onClick = onRequestPermission)
                            .padding(horizontal = 22.dp, vertical = 12.dp)
                    ) {
                        Text("授予音频权限", color = Color.White, fontWeight = FontWeight.Medium)
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
            tracks.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♪", fontSize = 52.sp, color = Color.White.copy(alpha = 0.35f))
                    Spacer(Modifier.height(8.dp))
                    Text("未找到本地音乐", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "把 mp3 / flac 放入 Music 目录，或点下方按钮导入",
                        fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .clickable(onClick = onImport)
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("导入音乐文件", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
            else -> LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(tracks) { index, track ->
                    TrackRow(
                        track = track,
                        playing = index == currentIndex && isPlaying,
                        onClick = { onTrackClick(index) },
                    )
                    Spacer(Modifier.height(2.dp))
                }
                item { Spacer(Modifier.height(10.dp)) }
            }
        }

        // 迷你播放条
        if (isMiniVisible && currentTrack != null) {
            MiniPlayerBar(
                track = currentTrack,
                isPlaying = isPlaying,
                onOpen = onOpenPlayer,
                onTogglePlay = onMiniPlayToggle,
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TrackRow(track: TrackItem, playing: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 封面（首字母渐变块）
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(trackBrush(track.title, playing)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                track.title.firstOrNull()?.uppercase() ?: "♪",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (playing) FontWeight.SemiBold else FontWeight.Normal,
                color = if (playing) Color.White else Color.White.copy(alpha = 0.92f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        if (playing) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "播放中",
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        } else {
            Text(
                formatTime(track.durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun MiniPlayerBar(
    track: TrackItem,
    isPlaying: Boolean,
    onOpen: () -> Unit,
    onTogglePlay: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onOpen)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(trackBrush(track.title, true)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                track.title.firstOrNull()?.uppercase() ?: "♪",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                track.artist,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onTogglePlay) {
            if (isPlaying) {
                PauseIcon(color = Color.White, size = 22.dp)
            } else {
                Icon(Icons.Filled.PlayArrow, contentDescription = "播放", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

/* ---------------- 全屏播放器 ---------------- */

@Composable
private fun PlayerScreen(
    track: TrackItem,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerPosition = remember { mutableLongStateOf(positionMs) }
    var dragging by remember { mutableStateOf(false) }
    var sliderPos by remember { mutableFloatStateOf(positionMs.toFloat()) }
    val totalMs = remember(durationMs) { if (durationMs > 0) durationMs else 0L }

    // 位置轮询
    LaunchedEffect(Unit) {
        while (true) {
            if (!dragging) {
                val pos = com.liytu.feature.music.GlobalPositionHolder.position
                playerPosition.longValue = pos
                sliderPos = pos.toFloat()
            }
            delay(400)
        }
    }

    BackHandler { onBack() }

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 顶栏
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "正在播放",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp))
        }
        Spacer(Modifier.weight(1f))

        // 旋转封面
        val infinite = rememberInfiniteTransition(label = "cover")
        val angle by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
            label = "angle",
        )
        Box(
            Modifier
                .size(230.dp)
                .graphicsLayer { rotationZ = angle }
                .clip(CircleShape)
                .background(trackBrush(track.title, true)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                track.title.firstOrNull()?.uppercase() ?: "♪",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 84.sp,
            )
        }
        Spacer(Modifier.height(34.dp))

        // 曲目信息
        Text(
            track.title,
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            track.artist,
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(26.dp))

        // 进度条
        val sliderMax = if (totalMs > 0) totalMs.toFloat() else 1f
        Slider(
            value = sliderPos.coerceIn(0f, sliderMax),
            onValueChange = {
                sliderPos = it
                dragging = true
            },
            onValueChangeFinished = {
                onSeek(sliderPos.toLong())
                dragging = false
            },
            valueRange = 0f..sliderMax,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.28f),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth()) {
            Text(
                formatTime(if (dragging) sliderPos.toLong() else playerPosition.longValue),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                formatTime(totalMs),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(14.dp))

        // 控制排
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(onClick = onPrevious) {
                SkipPreviousIcon(color = Color.White, size = 30.dp)
            }
            Spacer(Modifier.width(30.dp))
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f))
                    .clickable(onClick = onTogglePlay),
                contentAlignment = Alignment.Center,
            ) {
                if (isPlaying) {
                    PauseIcon(color = Color(0xFF2A2A44), size = 28.dp)
                } else {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "播放",
                        tint = Color(0xFF2A2A44),
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            Spacer(Modifier.width(30.dp))
            IconButton(onClick = onNext) {
                SkipNextIcon(color = Color.White, size = 30.dp)
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

/* ---------------- 自绘图标 ---------------- */

@Composable
fun PauseIcon(color: Color, size: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val barW = w * 0.28f
        val gap = w * 0.18f
        drawRoundRect(
            color = color,
            topLeft = Offset(w / 2f - gap - barW, h * 0.22f),
            size = androidx.compose.ui.geometry.Size(barW, h * 0.56f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW * 0.3f),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w / 2f + gap, h * 0.22f),
            size = androidx.compose.ui.geometry.Size(barW, h * 0.56f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW * 0.3f),
        )
    }
}

@Composable
fun SkipNextIcon(color: Color, size: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val triW = w * 0.45f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.12f, h * 0.18f)
            lineTo(w * 0.12f + triW, h * 0.5f)
            lineTo(w * 0.12f, h * 0.82f)
            close()
        }
        drawPath(path, color)
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.72f, h * 0.18f),
            size = androidx.compose.ui.geometry.Size(w * 0.14f, h * 0.64f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.05f),
        )
    }
}

@Composable
fun SkipPreviousIcon(color: Color, size: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val triW = w * 0.45f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.88f, h * 0.18f)
            lineTo(w * 0.88f - triW, h * 0.5f)
            lineTo(w * 0.88f, h * 0.82f)
            close()
        }
        drawPath(path, color)
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.14f, h * 0.18f),
            size = androidx.compose.ui.geometry.Size(w * 0.14f, h * 0.64f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.05f),
        )
    }
}

/* ---------------- 工具 ---------------- */

/** 全局播放位置（由 MusicScreen 轮询写入，播放器页读取）——单 Activity 内共享 */
object GlobalPositionHolder {
    @Volatile
    var position: Long = 0L
}

private fun trackBrush(title: String, strong: Boolean): Brush {
    val hue = ((title.hashCode() % 360 + 360) % 360) / 360f
    val c1 = androidx.compose.ui.graphics.Color.hsv(hue * 360f, 0.55f, if (strong) 0.95f else 0.85f)
    val c2 = androidx.compose.ui.graphics.Color.hsv((hue * 360f + 50f) % 360f, 0.70f, 0.99f)
    return Brush.linearGradient(listOf(c1, c2))
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "--:--"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
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

private fun audioPermission(): String =
    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

private fun hasAudioPermission(context: Context): Boolean {
    val permission = audioPermission()
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private suspend fun scanTracks(context: Context): List<TrackItem> = withContext(Dispatchers.IO) {
    val result = mutableListOf<TrackItem>()
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
    )
    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
    context.contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        null,
        "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
    )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            result += TrackItem(
                id = id,
                uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id),
                title = cursor.getString(titleCol) ?: "未知曲目",
                artist = cursor.getString(artistCol)?.takeIf { it.isNotBlank() && it != "<unknown>" } ?: "未知艺术家",
                durationMs = cursor.getLong(durationCol),
            )
        }
    }
    result
}
