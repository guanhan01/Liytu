package com.liytu.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.liytu.coreui.components.GlassCard

/** 首页：Liytu 门面 —— 渐变玻璃功能卡 + 欢迎卡片 */
@Composable
fun HomeScreen(
    backdrop: Backdrop? = null,
    onOpenTab: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        // 品牌区
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Liytu",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "BETA",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "全能多媒体 · Miuix × Liquid Glass",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
        )
        Spacer(Modifier.height(22.dp))

        // 欢迎玻璃卡
        GlassCard(backdrop = backdrop, cornerRadius = 26.dp) {
            Column {
                Text(
                    "欢迎使用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "本地音乐 · 视频 · 小说 · 漫画，一个应用全搞定。",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Spacer(Modifier.height(18.dp))

        // 四大功能卡：2x2
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FeatureCard(
                title = "音乐",
                subtitle = "本地播放 · 歌词",
                icon = Icons.Filled.PlayArrow,
                accent = 0f,
                modifier = Modifier.weight(1f),
                onClick = { onOpenTab(1) },
            )
            FeatureCard(
                title = "影视",
                subtitle = "选集 · 换集 · 倍速",
                icon = Icons.Filled.Star,
                accent = 1f,
                modifier = Modifier.weight(1f),
                onClick = { onOpenTab(2) },
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FeatureCard(
                title = "书漫",
                subtitle = "小说 · 漫画",
                icon = Icons.Filled.List,
                accent = 2f,
                modifier = Modifier.weight(1f),
                onClick = { onOpenTab(3) },
            )
            FeatureCard(
                title = "我的",
                subtitle = "主题 · 设置",
                icon = Icons.Filled.Person,
                accent = 3f,
                modifier = Modifier.weight(1f),
                onClick = { onOpenTab(4) },
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "Version 0.1.0 · 持续构建中",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.55f),
        )
    }
}

/** 渐变色随卡片索引变化，保持主题内视觉节奏 */
private fun cardGradient(accent: Float): Brush {
    val r = 0.55f + 0.12f * ((accent % 3f) + 1f)
    return Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.30f),
            Color.White.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.22f),
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f * r, 700f * r),
    )
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        cornerRadius = 24.dp,
        tint = Color.White.copy(alpha = 0.14f),
    ) {
        Column {
            // 图标渐变方块
            Column(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardGradient(accent)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
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
                    tint = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
            )
        }
    }
}
