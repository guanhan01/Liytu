package com.liytu.feature.home

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.liytu.coreui.components.GlassCard

/** 首页：Liytu 门面 —— 品牌 Hero 大卡 + 渐变玻璃功能入口 */
@Composable
fun HomeScreen(
    backdrop: Backdrop? = null,
    onOpenTab: (Int) -> Unit = {},
    usageTexts: Map<Int, String> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        // ── 品牌区 ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Liytu",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.20f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    "BETA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "全能多媒体 · Miuix × Liquid Glass",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.78f),
        )
        Spacer(Modifier.height(18.dp))

        // ── Hero 品牌大卡 ──
        HeroCard(backdrop = backdrop, onOpenTab = onOpenTab)
        Spacer(Modifier.height(16.dp))

        // ── 功能入口 2×2 ──
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FeatureEntryCard(
                title = "音乐",
                subtitle = "本地播放 · 歌词",
                icon = Icons.Filled.PlayArrow,
                colors = listOf(Color(0xFF7C4DFF), Color(0xFF448AFF)),
                usageText = usageTexts[1],
                modifier = Modifier.weight(1f),
                onClick = { onOpenTab(1) },
            )
            FeatureEntryCard(
                title = "影视",
                subtitle = "选集 · 换集 · 倍速",
                icon = Icons.Filled.Star,
                colors = listOf(Color(0xFFFF6FA0), Color(0xFFFF9A3D)),
                usageText = usageTexts[2],
                modifier = Modifier.weight(1f),
                onClick = { onOpenTab(2) },
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FeatureEntryCard(
                title = "书漫",
                subtitle = "小说 · 漫画",
                icon = Icons.Filled.List,
                colors = listOf(Color(0xFF00B4D8), Color(0xFF3DDC97)),
                usageText = usageTexts[3],
                modifier = Modifier.weight(1f),
                onClick = { onOpenTab(3) },
            )
            FeatureEntryCard(
                title = "我的",
                subtitle = "主题 · 设置",
                icon = Icons.Filled.Person,
                colors = listOf(Color(0xFFFF7EB3), Color(0xFF7C6FFF)),
                usageText = usageTexts[4],
                modifier = Modifier.weight(1f),
                onClick = { onOpenTab(4) },
            )
        }
        Spacer(Modifier.height(22.dp))

        // ── 页脚 ──
        Text(
            "Version 0.1.0 · 持续构建中",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.50f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
        )
        Spacer(Modifier.height(8.dp))
    }
}

/** 品牌 Hero 卡：slogan + 装饰光晕 + 快捷开始 */
@Composable
private fun HeroCard(backdrop: Backdrop?, onOpenTab: (Int) -> Unit) {
    GlassCard(
        backdrop = backdrop,
        cornerRadius = 28.dp,
        tint = Color.White.copy(alpha = 0.16f),
    ) {
        Box(Modifier.fillMaxWidth()) {
            // 右上装饰光晕
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-56).dp)
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                        )
                    )
            )
            Column {
                Text(
                    "LIYTU · ALL-IN-ONE MEDIA",
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.62f),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "全能多媒体中心",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "音乐 · 影视 · 小说 · 漫画\n一个应用全搞定",
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Color.White.copy(alpha = 0.84f),
                )
                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.24f))
                        .clickable { onOpenTab(1) }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("开始体验", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** 功能入口卡：双色渐变图标 + 标题 / 副标题 / 箭头 */
@Composable
private fun FeatureEntryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: List<Color>,
    usageText: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        cornerRadius = 24.dp,
        tint = Color.White.copy(alpha = 0.14f),
    ) {
        Column {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.linearGradient(
                            colors,
                            start = Offset.Zero,
                            end = Offset(420f, 340f),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.height(13.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.62f),
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.74f),
            )
            if (!usageText.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "最近使用 $usageText",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }
        }
    }
}
