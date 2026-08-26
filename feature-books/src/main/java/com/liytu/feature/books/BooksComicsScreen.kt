@file:OptIn(ExperimentalFoundationApi::class)

package com.liytu.feature.books

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liytu.coreui.components.GlassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.zip.ZipInputStream
import kotlin.math.abs

/** 导入的书籍 */
data class NovelItem(
    val name: String,
    val uri: Uri,
    var content: String = "",
    val format: String = "txt", // txt / pdf / epub
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
            val name = queryDisplayName(context, uri)?.substringBeforeLast('.')?.ifBlank { "未命名" } ?: "未命名"
            val ext = (queryDisplayName(context, uri) ?: "").substringAfterLast('.', "").lowercase()
            val format = when (ext) {
                "pdf" -> "pdf"
                "epub" -> "epub"
                else -> "txt"
            }
            val novel = NovelItem(name = name, uri = uri, format = format)
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
                    "小说 · 漫画 · PDF · EPUB 阅读中心",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
            Spacer(Modifier.height(10.dp))

            // 小说卡片
            SectionCard(
                title = "小说",
                count = "已导入 ${novels.size} 本",
                buttonText = "导入 TXT / PDF / EPUB",
                onClick = { novelLauncher.launch(arrayOf("*/*")) },
            ) {
                if (novels.isEmpty()) {
                    Text(
                        "支持 UTF-8 / GBK 编码 TXT，PDF / EPUB 文档，导入后即可阅读",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(10.dp))
                } else {
                    Text(
                        "最近导入",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(8.dp))
                }
                // 最近导入的书列表
                novels.takeLast(3).reversed().forEach { n ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { reading = n }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                n.format.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            n.name,
                            fontSize = 14.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
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
                    "选择图片可多选，支持 JPG / PNG / WebP，按文件名排序翻页",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

/* ---------------- 小说阅读页（分页 / PDF / EPUB） ---------------- */

@Composable
private fun NovelReaderPage(novel: NovelItem, context: Context, onBack: () -> Unit) {
    var text by remember(novel.uri) { mutableStateOf(novel.content) }
    var loading by remember(novel.uri) { mutableStateOf(novel.content.isBlank()) }

    LaunchedEffect(novel.uri) {
        if (text.isBlank()) {
            val loaded = withContext(Dispatchers.IO) {
                when (novel.format) {
                    "pdf" -> "" // PDF 走渲染器
                    "epub" -> extractEpubText(context, novel.uri)
                    else -> readTextSmart(context, novel.uri)
                }
            }
            text = loaded
            novel.content = loaded
            loading = false
        }
    }

    BackHandler { onBack() }

    when (novel.format) {
        "pdf" -> PdfReaderPage(novel = novel, context = context, onBack = onBack)
        else -> {
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("正在排版…", color = Color.White.copy(alpha = 0.7f))
                }
            } else {
                PagedReader(
                    title = novel.name,
                    text = text,
                    progressKey = "book_" + novel.uri.toString().hashCode(),
                    onBack = onBack,
                )
            }
        }
    }
}

/** 分页文本阅读器：多字体 / 字号 / 行高 / 翻页动画 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagedReader(title: String, text: String, progressKey: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("liytu_progress", Context.MODE_PRIVATE) }
    val savedPage = remember { prefs.getInt(progressKey, 0) }
    var fontSize by rememberSaveable { mutableFloatStateOf(19f) }
    var lineHeightFactor by rememberSaveable { mutableFloatStateOf(1.7f) }
    var fontName by rememberSaveable { mutableStateOf("sans") }
    var animMode by rememberSaveable { mutableIntStateOf(0) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    val fontFamily = when (fontName) {
        "serif" -> FontFamily.Serif
        "mono" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.SansSerif
    }
    val measurer = rememberTextMeasurer()
    val style = TextStyle(
        fontSize = fontSize.sp,
        lineHeight = (fontSize * lineHeightFactor).sp,
        fontFamily = fontFamily,
        color = Color.White.copy(alpha = 0.92f),
    )
    var pages by remember { mutableStateOf<List<String>>(emptyList()) }
    val pagerState = rememberPagerState(initialPage = savedPage, pageCount = { pages.size })
    LaunchedEffect(pages, savedPage) {
        if (pages.isNotEmpty()) {
            val target = savedPage.coerceIn(0, pages.size - 1)
            if (pagerState.currentPage != target) pagerState.scrollToPage(target)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pages.isNotEmpty()) prefs.edit().putInt(progressKey, pagerState.currentPage).apply()
    }
    DisposableEffect(Unit) {
        onDispose { prefs.edit().putInt(progressKey, pagerState.currentPage).apply() }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxW = constraints.maxWidth
        val maxH = (constraints.maxHeight - 40.dp.roundToPx() * 2).coerceAtLeast(100)
        LaunchedEffect(text, fontSize, lineHeightFactor, fontName, maxW, maxH) {
            // TextMeasurer 需主线程测量；大文本一次测量约 1 秒内
            pages = paginateText(text.take(320_000), measurer, style, maxW, maxH)
        }

        Column(Modifier.fillMaxSize()) {
            // 顶栏
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onBack)
                        .padding(10.dp)
                        .size(22.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "第 ${(pagerState.currentPage + 1).coerceAtLeast(1)} / ${pages.size.coerceAtLeast(1)} 页",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
                // 设置按钮
                Row(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (showSettings) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.14f))
                        .clickable { showSettings = !showSettings }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "阅读设置",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("设置", color = Color.White, fontSize = 12.sp)
                }
            }

            // 正文分页
            if (pages.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("正在排版…", color = Color.White.copy(alpha = 0.6f))
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) { page ->
                    Box(Modifier.fillMaxSize().graphicsLayer {
                        val offset = pagerState.currentPageOffsetFraction
                        when (animMode) {
                            1 -> { alpha = (1f - abs(offset)).coerceIn(0f, 1f) }
                            2 -> { alpha = if (abs(offset) < 0.5f) 1f else 0f }
                            else -> {
                                translationX = offset * size.width * 0.14f
                                alpha = 1f - 0.22f * abs(offset)
                            }
                        }
                    }) {
                        Text(
                            pages[page],
                            style = style,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 22.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            // 设置面板
            AnimatedVisibility(visible = showSettings) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    cornerRadius = 20.dp,
                    tint = Color.White.copy(alpha = 0.16f),
                ) {
                    Column {
                        // 字体
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("sans" to "系统", "serif" to "衬线", "mono" to "等宽", "cursive" to "手写").forEach { (key, label) ->
                                FontChip(
                                    text = label,
                                    selected = fontName == key,
                                    onClick = { fontName = key },
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "字号 ${fontSize.toInt()}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 14f..30f,
                        )
                        Text(
                            "行高 ${String.format("%.1f", lineHeightFactor)}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                        Slider(
                            value = lineHeightFactor,
                            onValueChange = { lineHeightFactor = it },
                            valueRange = 1.3f..2.3f,
                        )
                        // 翻页动画
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("滑动翻页", "覆盖滑动", "淡入淡出", "无动画").forEachIndexed { i, label ->
                                FontChip(
                                    text = label,
                                    selected = animMode == i,
                                    onClick = { animMode = i },
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "提示：仅排版前 32 万字（约 1 秒）",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.4f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FontChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = Color.White,
        )
    }
}

/** 按行分页：全文测量一次，按每页行数切片 */
private fun paginateText(
    text: String,
    measurer: androidx.compose.ui.text.TextMeasurer,
    style: TextStyle,
    maxW: Int,
    maxH: Int,
): List<String> {
    if (text.isBlank()) return listOf("（空文档）")
    val result = measurer.measure(
        AnnotatedString(text),
        style = style,
        constraints = Constraints(maxWidth = maxW.coerceAtLeast(100)),
    )
    val lineCount = result.lineCount
    if (lineCount <= 0) return listOf(text)
    val avgLineH = result.size.height.toFloat() / lineCount
    if (avgLineH <= 0f) return listOf(text)
    val perPage = (maxH.toFloat() / avgLineH).toInt().coerceAtLeast(3)
    val out = mutableListOf<String>()
    var line = 0
    while (line < lineCount) {
        val startOffset = result.getLineStart(line)
        val endLine = (line + perPage).coerceAtMost(lineCount)
        val endOffset = if (endLine < lineCount) result.getLineStart(endLine) else text.length
        out.add(text.substring(startOffset, endOffset).trimStart())
        line = endLine
    }
    return out
}

/* ---------------- PDF 阅读页（系统 PdfRenderer） ---------------- */

@Composable
private fun PdfReaderPage(novel: NovelItem, context: Context, onBack: () -> Unit) {
    val renderer = remember(novel.uri) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(novel.uri, "r")
                ?: return@remember null
            PdfRenderer(pfd)
        } catch (_: Exception) {
            null
        }
    }
    val bookPrefs = remember { context.getSharedPreferences("liytu_progress", Context.MODE_PRIVATE) }
    val pdfKey = "pdf_" + novel.uri.toString().hashCode()
    val savedPdfPage = remember { bookPrefs.getInt(pdfKey, 0) }
    val pagerState = rememberPagerState(initialPage = savedPdfPage, pageCount = { renderer?.pageCount ?: 0 })
    LaunchedEffect(renderer, savedPdfPage) {
        val total = renderer?.pageCount ?: 0
        if (total > 0) {
            val target = savedPdfPage.coerceIn(0, total - 1)
            if (pagerState.currentPage != target) pagerState.scrollToPage(target)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        val total = renderer?.pageCount ?: 0
        if (total > 0) bookPrefs.edit().putInt(pdfKey, pagerState.currentPage).apply()
    }
    DisposableEffect(Unit) {
        onDispose { bookPrefs.edit().putInt(pdfKey, pagerState.currentPage).apply() }
    }

    BackHandler { onBack() }

    if (renderer == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("无法打开该 PDF 文件", color = Color.White.copy(alpha = 0.7f))
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onBack)
                    .padding(10.dp)
                    .size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    novel.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "第 ${(pagerState.currentPage + 1).coerceAtLeast(1)} / ${renderer.pageCount.coerceAtLeast(1)} 页",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            val bitmap by produceState<Bitmap?>(initialValue = null, page, renderer) {
                value = withContext(Dispatchers.Default) { renderPdfPage(renderer, page) }
            }
            val bmp = bitmap
            if (bmp != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "PDF 第 ${page + 1} 页",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("加载中…", color = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}

private fun renderPdfPage(renderer: PdfRenderer, pageIndex: Int): Bitmap {
    val page = renderer.openPage(pageIndex)
    val scale = 1080f / page.width.toFloat()
    val w = (page.width * scale).toInt().coerceAtLeast(200)
    val h = (page.height * scale).toInt().coerceAtLeast(200)
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    try {
        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    } finally {
        page.close()
    }
    return bmp
}

/* ---------------- EPUB 文本提取 ---------------- */

private fun extractEpubText(context: Context, uri: Uri): String {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return "无法读取 EPUB"
        val zip = ZipInputStream(input.buffered())
        val chapters = mutableListOf<Pair<String, String>>()
        var entry = zip.nextEntry
        while (entry != null) {
            val name = entry.name.lowercase()
            if (!entry.isDirectory && (name.endsWith(".html") || name.endsWith(".xhtml"))) {
                if (name.contains("nav") || name.contains("toc") || name.contains("cover")) {
                    entry = zip.nextEntry
                    continue
                }
                val body = zip.readBytes().toString(Charsets.UTF_8)
                val html = body.substringAfter("<body", body).substringAfter(">", body)
                val text = html
                    .replace(Regex("<[^>]+>"), "\n")
                    .replace("&nbsp;", " ")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&quot;", "\"")
                    .replace("&#39;", "'")
                    .replace(Regex("\n{3,}"), "\n\n")
                    .trim()
                if (text.length > 40) {
                    chapters += name.substringAfterLast('/') to text
                }
            }
            entry = zip.nextEntry
        }
        zip.close()
        if (chapters.isEmpty()) return "未能提取到 EPUB 正文"
        chapters.joinToString("\n\n") { it.second }
    } catch (_: Exception) {
        "EPUB 解析失败"
    }
}

/* ---------------- 漫画浏览页 ---------------- */

@Composable
private fun ComicsBrowsePage(
    images: List<Uri>,
    name: String,
    context: Context,
    onBack: () -> Unit,
) {
    // 内容与现版本一致：按文件名排序的 HorizontalPager 翻页
    val sorted = remember(images) { images.sortedBy { queryDisplayName(context, it) ?: "" } }
    val comicPrefs = remember { context.getSharedPreferences("liytu_progress", Context.MODE_PRIVATE) }
    val comicKey = "comics_" + name.hashCode()
    val savedComicPage = remember { comicPrefs.getInt(comicKey, 0) }
    val pagerState = rememberPagerState(initialPage = savedComicPage, pageCount = { sorted.size })
    LaunchedEffect(sorted, savedComicPage) {
        if (sorted.isNotEmpty()) {
            val target = savedComicPage.coerceIn(0, sorted.size - 1)
            if (pagerState.currentPage != target) pagerState.scrollToPage(target)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        if (sorted.isNotEmpty()) comicPrefs.edit().putInt(comicKey, pagerState.currentPage).apply()
    }
    DisposableEffect(Unit) {
        onDispose { comicPrefs.edit().putInt(comicKey, pagerState.currentPage).apply() }
    }

    BackHandler { onBack() }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onBack)
                    .padding(10.dp)
                    .size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(
                    "${pagerState.currentPage + 1} / ${sorted.size}",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            val uri = sorted[page]
            val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
                value = withContext(Dispatchers.IO) {
                    try {
                        val b = android.graphics.BitmapFactory.decodeStream(
                            context.contentResolver.openInputStream(uri)
                        )
                        b
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            val bmp = bitmap
            if (bmp != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "漫画页",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("加载中…", color = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}

/* ---------------- 基础组件 ---------------- */

@Composable
private fun SectionCard(
    title: String,
    count: String,
    buttonText: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(count, color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                }
                Row(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.20f))
                        .clickable(onClick = onClick)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(buttonText, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

/* ---------------- 工具 ---------------- */

private fun readTextSmart(context: Context, uri: Uri): String {
    return try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return "无法读取文件"
        decodeText(bytes)
    } catch (_: Exception) {
        "读取失败"
    }
}

private fun decodeText(bytes: ByteArray): String {
    // UTF-8 -> GBK
    val utf8 = try {
        val s = String(bytes, Charsets.UTF_8)
        // 简单校验：utf8 解码后无过多替换符则视为有效
        if (s.count { it == '\uFFFD' } > bytes.size / 20) null else s
    } catch (_: Exception) {
        null
    }
    utf8?.let { if (it.isNotBlank()) return it }
    return try {
        String(bytes, Charset.forName("GBK"))
    } catch (_: Exception) {
        String(bytes, Charsets.UTF_8)
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    return try {
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    } catch (_: Exception) {
        uri.lastPathSegment
    }
}
