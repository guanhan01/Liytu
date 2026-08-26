package com.liytu.coreui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** 全局圆角体系：所有卡片/按钮/对话框引用，支持实时调节 */
fun liytuShapes(radius: Float = 20f): Shapes = Shapes(
    extraSmall = RoundedCornerShape((radius * 0.3f).dp),
    small = RoundedCornerShape((radius * 0.5f).dp),
    medium = RoundedCornerShape(radius.dp),
    large = RoundedCornerShape((radius * 1.4f).dp),
    extraLarge = RoundedCornerShape((radius * 1.8f).dp),
)

/** 当前预设的 CompositionLocal（组件读取圆角、液态玻璃开关等） */
val LocalLiytuPreset = staticCompositionLocalOf { LiytuPresets.defaults.first() }
