package com.liytu.feature.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liytu.coreui.theme.LiytuPresets
import com.liytu.coreui.theme.LiytuThemePreset

/** 可选的 keyColor / accentColor 色板 */
private val keyPalette = listOf(
    0xFF6F86FF, 0xFF7C6FFF, 0xFF00B4D8, 0xFF3DDC97,
    0xFFFF9A3D, 0xFFFF7EB3, 0xFFFF4E6B, 0xFFE6E8F0,
)
private val accentPalette = listOf(
    0xFFFF6FA0, 0xFFB388FF, 0xFF00E5FF, 0xFF3DDC97,
    0xFFFF9A3D, 0xFFFF4E6B, 0xFF7C6FFF, 0xFF6F86FF,
)

/** 我的：DIY 主题（预设切换 + 圆角 / 液态玻璃 / 取色实时调节） */
@Composable
fun MineScreen(
    current: LiytuThemePreset,
    onPresetChange: (LiytuThemePreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("我的", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(
            "主题预设 · 界面 DIY",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.75f),
        )
        Spacer(Modifier.height(20.dp))

        // ── 当前主题预览卡 ──
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            current.key.copy(alpha = 0.42f),
                            current.accent.copy(alpha = 0.28f),
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "当前主题",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.72f),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    current.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(current.key, current.accent)))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        current.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "圆角 ${current.globalCornerRadius.toInt()}dp · 玻璃 ${(current.liquidGlassIntensity * 100).toInt()}% · 模糊 ${current.blurRadius.toInt()}dp",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        // ── 主题预设 ──
        Text("主题预设", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(12.dp))
        LiytuPresets.defaults.forEach { preset ->
            PresetRow(
                preset = preset,
                selected = preset.id == current.id,
                onClick = { onPresetChange(preset) },
            )
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(12.dp))

        // ── 界面 DIY ──
        Text("界面 DIY", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.13f))
                .padding(16.dp)
        ) {
            // 全局圆角
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "全局圆角",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${current.globalCornerRadius.toInt()} dp",
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 13.sp,
                )
            }
            Slider(
                value = current.globalCornerRadius,
                onValueChange = { onPresetChange(current.copy(globalCornerRadius = it)) },
                valueRange = 8f..32f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                ),
            )
            Spacer(Modifier.height(4.dp))

            // 液态玻璃开关
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "液态玻璃",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = current.liquidGlassEnabled,
                    onCheckedChange = { onPresetChange(current.copy(liquidGlassEnabled = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.45f),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.75f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.20f),
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
            }
            Spacer(Modifier.height(8.dp))

            if (current.liquidGlassEnabled) {
                // 玻璃强度
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "玻璃强度",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${(current.liquidGlassIntensity * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 13.sp,
                    )
                }
                Slider(
                    value = current.liquidGlassIntensity,
                    onValueChange = { onPresetChange(current.copy(liquidGlassIntensity = it)) },
                    valueRange = 0.1f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                    ),
                )
                Spacer(Modifier.height(4.dp))

                // 背景模糊
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "背景模糊",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${current.blurRadius.toInt()} dp",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 13.sp,
                    )
                }
                Slider(
                    value = current.blurRadius,
                    onValueChange = { onPresetChange(current.copy(blurRadius = it)) },
                    valueRange = 16f..48f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                    ),
                )
                Spacer(Modifier.height(6.dp))
            } else {
                Text(
                    "已关闭液态玻璃：背景将切换为实色渐变（每张卡片同步降级为纯色高光）",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.55f),
                )
                Spacer(Modifier.height(6.dp))
            }

            // 主色选择
            Spacer(Modifier.height(4.dp))
            Text("主色", color = Color.White.copy(alpha = 0.80f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                keyPalette.forEach { argb ->
                    ColorDot(
                        color = Color(argb),
                        selected = current.keyColor == argb,
                        onClick = { onPresetChange(current.copy(keyColor = argb)) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            // 强调色选择
            Text("强调色", color = Color.White.copy(alpha = 0.80f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                accentPalette.forEach { argb ->
                    ColorDot(
                        color = Color(argb),
                        selected = current.accentColor == argb,
                        onClick = { onPresetChange(current.copy(accentColor = argb)) },
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "切换预设 300ms 色彩过渡 · 圆角 150ms 实时生效\n下一版：预设保存 · Miuix / Material 切换 · 字体缩放",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.55f),
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PresetRow(preset: LiytuThemePreset, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (selected) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.10f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(
                    brush = Brush.linearGradient(listOf(preset.key, preset.accent)),
                    shape = CircleShape,
                )
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(preset.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color.White)
            Text(
                if (selected) "使用中" else "${preset.key.toHexString()} · ${preset.accent.toHexString()}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.60f),
            )
        }
        if (selected) {
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.90f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "使用中",
                    tint = Color(0xFF2A2A44),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

/** 可选色块：选中时白色描边 + 勾选角标 */
@Composable
private fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.25f),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

private fun Color.toHexString(): String {
    val r = (this.red * 255).toInt()
    val g = (this.green * 255).toInt()
    val b = (this.blue * 255).toInt()
    return "#%02X%02X%02X".format(r, g, b)
}
