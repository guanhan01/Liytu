package com.liytu.feature.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

/** 我的：DIY 主题（预设切换 + 全局圆角实时调节） */
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
        Spacer(Modifier.height(22.dp))

        // 主题预设
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
        Spacer(Modifier.height(14.dp))

        // 界面 DIY
        Text("界面 DIY", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.13f))
                .padding(16.dp)
        ) {
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
                    color = Color.White.copy(alpha = 0.7f),
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
            Spacer(Modifier.height(2.dp))
            Text(
                "所有卡片 / 按钮 / 对话框的圆角实时生效（150ms 过渡）",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.55f),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "后续：颜色取色器 · Miuix / Material 切换 · 字体缩放 · 预设保存（开发中）",
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
                color = Color.White.copy(alpha = 0.6f),
            )
        }
        if (selected) {
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
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

private fun Color.toHexString(): String {
    val a = (this.alpha * 255).toInt()
    val r = (this.red * 255).toInt()
    val g = (this.green * 255).toInt()
    val b = (this.blue * 255).toInt()
    return "#%02X%02X%02X".format(r, g, b)
}
