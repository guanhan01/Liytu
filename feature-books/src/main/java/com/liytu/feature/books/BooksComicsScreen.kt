package com.liytu.feature.books

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 导入的书籍（TXT） */
data class NovelItem(
    val name: String,
    val uri: Uri,
    var content: String = "",
)

@Composable
fun BooksComicsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var novels by remember { mutableStateOf<List<NovelItem>>(emptyList()) }
    var comicImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var comicName by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf<NovelItem?>(null) }
    var browsing by remember { mutableStateOf(false) }
    var loadingNovel by remember { mutableStateOf(false) }

    val novelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            loadingNovel = true
            val name = queryDisplayName(context, uri)?.substringBeforeLast('.')?.ifBlank { "未命名小说" } ?: "未命名小说"
            var novel = NovelItem(name = name, uri = uri)
            novels = novels.filterNot { it.uri == uri } + novel
            reading = novels.first { it.uri == uri }
            loadingNovel = false
        }
    }

    val comicLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
            }
            comicImages = uris
            comicName = queryDisplayName(context, uris.first())?.substringBeforeLast('.') ?: "漫画"
            browsing = true
        }
    }

    val currentNovel = reading
    if (currentNovel != null) {
        NovelReaderPage(
            novel = currentNovel,
            context = context,
            onBack = { reading = null },
        )
    } else if (browsing) {
        ComicsBrowsePage(
            images = comicImages,
            name = comicName,
            context = context,
            onBack = { browsing = false },
        )
    } else {
        Column(
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Column(Modifier.padding(top = 18.dp, bottom = 8.dp)) {
                Text(
                    "书漫",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    "小说 · 漫画 阅读中心",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
            Spacer(Modifier.height(10.dp))

            // 小说卡片
            SectionCard(
                title = "小说",
                count = "已导入 ${novels.size} 本",
                buttonText = "导入 TXT",
                onClick = { novelLauncher.launch(arrayOf("text/plain")) },
            ) {
                if (novels.isEmpty()) {
                    Text(
                        "支持 UTF-8 / GBK 编码 TXT，导入后即可阅读",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(8.dp))
                } else {
                    Spacer(Modifier.height(2.dp))
                    novels.forEach { novel ->
                        Text(
                            novel.name,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { reading = novel }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
            Spacer(Modifier.height(14.dp))

            // 漫画卡片
            SectionCard(
                title = "漫画",
                count = if (comicImages.isEmpty()) "未导入" else "已选 ${comicImages.size} 张",
                buttonText = "导入图片",
                onClick = { comicLauncher.launch(arrayOf("image/*")) },
            ) {
                Text(
                    "选择图片（可多选），以翻页方式浏览；支持 JPG / PNG / WebP",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(8.dp))
                if (comicImages.isNotEmpty()) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.20f))
                            .clickable { browsing = true }
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("继续浏览", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "后续支持：URL / JSON 书源 · EPUB / PDF · 章节列表 · 排版调节",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    count: String,
    buttonText: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Text(
                    count,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
            Row(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.20f))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 13.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(buttonText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

/* ---------------- 小说阅读页 ---------------- */

@Composable
private fun NovelReaderPage(novel: NovelItem, context: Context, onBack: () -> Unit) {
    var fontSize by remember { mutableFloatStateOf(18f) }
    var text by remember(novel.uri) { mutableStateOf(novel.content) }

    LaunchedEffect(novel.uri) {
        if (text.isBlank()) {
            val loaded = withContext(Dispatchers.IO) { readTextSmart(context, novel.uri) }
            text = loaded
            novel.content = loaded
        }
    }

    BackHandler { onBack() }

    Column(Modifier.fillMaxSize()) {
        // 顶部工具条
        Row(
            Modifier
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
            Spacer(Modifier.width(8.dp))
            Text(
                novel.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            // 字号按钮
            Text(
                "A-",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { fontSize = (fontSize - 2f).coerceAtLeast(12f) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
            Text(
                "${fontSize.toInt()}",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Text(
                "A+",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { fontSize = (fontSize + 2f).coerceAtMost(34f) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        // 正文
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
        ) {
            Text(
                novel.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "—— 导入的本地 TXT ——",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text.ifBlank { "正在加载…" },
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.65f).sp,
                color = Color.White.copy(alpha = 0.92f),
            )
            Spacer(Modifier.height(30.dp))
            Text(
                "— 完 —",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

/* ---------------- 漫画浏览页 ---------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ComicsBrowsePage(
    images: List<Uri>,
    name: String,
    context: Context,
    onBack: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    BackHandler { onBack() }

    Box(Modifier.fillMaxSize().background(Color(0xFF0B0B12))) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            var bitmap by remember(images[page]) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
            LaunchedEffect(images[page]) {
                bitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(images[page])?.use {
                        BitmapFactory.decodeStream(it)
                    }?.asImageBitmap()
                }
            }
            val bmp = bitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = "$name 第 ${page + 1} 页",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("加载中…", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
        // 顶部返回
        Row(
            Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)))
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
            Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        // 底部页码
        Text(
            "${pagerState.currentPage + 1} / ${images.size}",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}

/* ---------------- 工具 ---------------- */

private fun readTextSmart(context: Context, uri: Uri): String {
    return try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return ""
        // BOM 检测
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8).take(300_000)
        }
        val utf8 = String(bytes, Charsets.UTF_8)
        if (!utf8.contains('\uFFFD')) {
            utf8.take(300_000)
        } else {
            String(bytes, Charsets.GBK).take(300_000)
        }
    } catch (_: Exception) {
        ""
    }
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
