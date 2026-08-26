package com.liytu.coreui.theme

import androidx.compose.ui.graphics.Color

/** 主题风格：Miuix（HyperOS 风）或 Material 3 */
enum class ThemeStyle { MIUIX, MATERIAL }

/** 配色模式 */
enum class ColorMode { LIGHT, DARK, SYSTEM }

/**
 * Liytu 主题预设（DIY 主题系统的核心数据模型）
 * 支持保存多个预设、编辑 keyColor/accentColor/圆角/液态玻璃强度等。
 */
data class LiytuThemePreset(
    val id: String,
    val name: String,
    val style: ThemeStyle = ThemeStyle.MIUIX,
    val colorMode: ColorMode = ColorMode.SYSTEM,
    val keyColor: Long = 0xFF6F86FF,          // ARGB
    val accentColor: Long = 0xFFFF6FA0,       // ARGB
    val globalCornerRadius: Float = 20f,      // dp
    val liquidGlassEnabled: Boolean = true,
    val liquidGlassIntensity: Float = 1f,     // 0..1
    val blurRadius: Float = 32f,              // dp
    val fontFamilyName: String = "system",
    val fontSizeScale: Float = 1f,
    val isDefault: Boolean = false,
) {
    val key: Color get() = Color(keyColor)
    val accent: Color get() = Color(accentColor)
}

/** 内置预设 */
object LiytuPresets {
    val defaults = listOf(
        LiytuThemePreset(
            id = "preset_ocean",
            name = "海洋",
            keyColor = 0xFF6F86FF,
            accentColor = 0xFFFF6FA0,
            liquidGlassIntensity = 1f,
            isDefault = true,
        ),
        LiytuThemePreset(
            id = "preset_sunset",
            name = "日落",
            keyColor = 0xFFFF9A3D,
            accentColor = 0xFFFF4E6B,
        ),
        LiytuThemePreset(
            id = "preset_forest",
            name = "森林",
            keyColor = 0xFF3DDC97,
            accentColor = 0xFF00B4D8,
        ),
        LiytuThemePreset(
            id = "preset_midnight",
            name = "午夜",
            keyColor = 0xFF7C6FFF,
            accentColor = 0xFF00E5FF,
        ),
        LiytuThemePreset(
            id = "preset_rose",
            name = "玫瑰",
            keyColor = 0xFFFF7EB3,
            accentColor = 0xFFB388FF,
        ),
    )
}
