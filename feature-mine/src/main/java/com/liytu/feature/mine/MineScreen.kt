@file:OptIn(ExperimentalLayoutApi::class)

package com.liytu.feature.mine

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liytu.coreui.components.GlassCard
import com.liytu.coreui.theme.LiytuPresets
import com.liytu.coreui.theme.LiytuThemePreset
import com.liytu.coreui.theme.ThemeStyle

/** 我的：设置主页（主题外观 / 音乐设置 / 关于） */
@Composable
fun MineScreen(current: LiytuThemePreset, onPresetChange: (LiytuThemePreset) -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }

    AnimatedContent(
        targetState = page,
        transitionSpec = {
            (slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(280))) togetherWith
                (slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(200)))
        },
        label = "mine",
    ) { p ->
        when (p) {
            0 -> MineHome(
                current = current,
                onOpenTheme = { page = 1 },
                onOpenMusic = { page = 2 },
            )
            1 -> ThemePage(current = current, onPresetChange = onPresetChange, onBack = { page = 0 })
            2 -> MusicSettingsPage(onBack = { page = 0 })
        }
    }
}

/* ---------------- 设置主页 ---------------- */

@Composable
private fun MineHome(
    current: LiytuThemePreset,
    onOpenTheme: () -> Unit,
    onOpenMusic: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(
            "我的",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(14.dp))

        // 头像卡
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(listOf(current.key, current.accent))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("L", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Liytu 体验官", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(Modifier.height(3.dp))
                    Text("当前主题：${current.name}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f))
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // 设置列表
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingRow(
                    icon = { Icon(Icons.Filled.Palette, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(20.dp)) },
                    title = "主题外观",
                    value = current.name,
                    onClick = onOpenTheme,
                )
                SettingDivider()
                SettingRow(
                    icon = { Icon(Icons.Filled.MusicNote, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(20.dp)) },
                    title = "音乐设置",
                    value = "过滤 · 播放",
                    onClick = onOpenMusic,
                )
                SettingDivider()
                SettingRow(
                    icon = { Icon(Icons.Filled.Info, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(20.dp)) },
                    title = "关于 Liytu",
                    value = "v0.1.0 BETA",
                    onClick = {},
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "更多能力持续构建中 · 下一批：EQ 均衡器 · 视频选集倍速 · PDF/EPUB 书源",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.45f),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun SettingRow(
    icon: @Composable () -> Unit,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Spacer(Modifier.width(12.dp))
        Text(title, fontSize = 15.sp, color = Color.White, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, color = Color.White.copy(alpha = 0.55f))
        Spacer(Modifier.width(6.dp))
        Icon(
            Icons.Filled.ArrowForwardIos,
            null,
            tint = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(13.dp),
        )
    }
}

@Composable
private fun SettingDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 6.dp)
    )
}

/* ---------------- 主题子页 ---------------- */

@Composable
private fun ThemePage(
    current: LiytuThemePreset,
    onPresetChange: (LiytuThemePreset) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        // 顶部
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onBack)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "返回",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
            Text("主题外观", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "渐变 / 纯色预设 · UI 风格 · 液态玻璃 · 自定义取色",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.55f),
        )
        Spacer(Modifier.height(16.dp))

        // ── 渐变预设 ──
        Text("渐变预设", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LiytuPresets.defaults.forEach { preset ->
                PresetChip(
                    preset = preset,
                    selected = current.id == preset.id,
                    onClick = {
                        onPresetChange(preset.copy(style = current.style, colorMode = current.colorMode))
                    },
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── 纯色主题 ──
        Text("纯色主题", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LiytuPresets.solids.forEach { preset ->
                PresetChip(
                    preset = preset,
                    selected = current.id == preset.id,
                    onClick = {
                        onPresetChange(preset.copy(style = current.style, colorMode = current.colorMode))
                    },
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── UI 风格 ──
        Text("界面风格", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(10.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StyleButton(
                    text = "Miuix 液态玻璃",
                    selected = current.style == ThemeStyle.MIUIX,
                    modifier = Modifier.weight(1f),
                    onClick = { onPresetChange(current.copy(style = ThemeStyle.MIUIX)) },
                )
                StyleButton(
                    text = "Material 极简",
                    selected = current.style == ThemeStyle.MATERIAL,
                    modifier = Modifier.weight(1f),
                    onClick = { onPresetChange(current.copy(style = ThemeStyle.MATERIAL)) },
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── 液态玻璃（仅 Miuix 风格） ──
        if (current.style == ThemeStyle.MIUIX) {
            Text("液态玻璃", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                var glassOn by remember(current.liquidGlassEnabled) {
                    mutableStateOf(current.liquidGlassEnabled)
                }
                var intensity by remember(current.liquidGlassIntensity) {
                    mutableFloatStateOf(current.liquidGlassIntensity)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("玻璃效果", fontSize = 15.sp, color = Color.White, modifier = Modifier.weight(1f))
                    Switch(
                        checked = glassOn,
                        onCheckedChange = {
                            glassOn = it
                            onPresetChange(current.copy(liquidGlassEnabled = it))
                        },
                    )
                }
                if (glassOn) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "玻璃强度  ${(intensity * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Slider(
                        value = intensity,
                        onValueChange = {
                            intensity = it
                            onPresetChange(current.copy(liquidGlassIntensity = it))
                        },
                        valueRange = 0f..1f,
                    )
                } else {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "已关闭：背景切换为实色渐变（卡片同步降级为纯色高光）",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        // ── 自定义取色 ──
        Text("自定义取色", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(10.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            /** 主色候选（ARGB） */
            val keyColors = longArrayOf(
                0xFF6F86FF, 0xFFFF9A3D, 0xFF3DDC97, 0xFF7C6FFF,
                0xFFFF7EB3, 0xFF00B4D8, 0xFFE5484D, 0xFF30A46C,
            )
            /** 强调色候选（ARGB） */
            val accentColors = longArrayOf(
                0xFFFF6FA0, 0xFFFF4E6B, 0xFF00B4D8, 0xFF00E5FF,
                0xFFB388FF, 0xFFFF9A3D, 0xFFF5B70D, 0xFF8E4EC6,
            )
            Text("主色", fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                keyColors.forEach { c ->
                    ColorDot(
                        color = Color(c),
                        selected = current.keyColor == c,
                        onClick = { onPresetChange(current.copy(keyColor = c, isSolid = false)) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("强调色", fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                accentColors.forEach { c ->
                    ColorDot(
                        color = Color(c),
                        selected = current.accentColor == c,
                        onClick = { onPresetChange(current.copy(accentColor = c, isSolid = false)) },
                    )
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "切换预设 300ms 色彩过渡 · 风格切换全卡片同步",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.45f),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PresetChip(preset: LiytuThemePreset, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) Color.White else Color.White.copy(alpha = 0.18f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (preset.isSolid) Color(preset.keyColor)
                    else Brush.linearGradient(listOf(preset.key, preset.accent))
                )
                .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(preset.name, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
private fun StyleButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color.White.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = Color.White,
        )
    }
}

@Composable
private fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color)
            .border(if (selected) 2.dp else 0.dp, Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

/* ---------------- 音乐设置子页 ---------------- */

@Composable
private fun MusicSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("liytu_settings", Context.MODE_PRIVATE) }
    var filterShort by remember { mutableStateOf(prefs.getBoolean("filter_short", false)) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onBack)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "返回",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
            Text("音乐设置", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Spacer(Modifier.height(16.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("过滤短音频", fontSize = 15.sp, color = Color.White)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "隐藏时长不足 60 秒的音频（重新扫描后生效）",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.55f),
                    )
                }
                Switch(
                    checked = filterShort,
                    onCheckedChange = {
                        filterShort = it
                        prefs.edit().putBoolean("filter_short", it).apply()
                    },
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("播放模式", fontSize = 15.sp, color = Color.White)
                Spacer(Modifier.height(3.dp))
                Text(
                    "单曲循环 / 顺序播放 / 列表循环 / 随机播放：在播放页底部切换",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.55f),
                )
                Spacer(Modifier.height(10.dp))
                Text("歌词", fontSize = 15.sp, color = Color.White)
                Spacer(Modifier.height(3.dp))
                Text(
                    "卡拉OK逐字着色：播放页点击「词」进入全屏歌词查看",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("均衡器 EQ", fontSize = 15.sp, color = Color.White)
                Spacer(Modifier.height(3.dp))
                Text(
                    "10 段 EQ：播放页点击「EQ」打开调节面板（设备不支持时自动隐藏）",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}
