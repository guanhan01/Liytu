package com.liytu.coreui.theme

import androidx.compose.ui.graphics.Color

/** 主题风格：Miuix（HyperOS 风）或 Material 3 */
enum class ThemeStyle { MIUIX, MATERIAL }

/** 配色模式 */
enum class ColorMode { LIGHT, DARK, SYSTEM }

/**
 * Liytu 主题预设（DIY 主题系统的核心数据模型）
 * isSolid=true 表示纯色主题（背景为单一颜色，无渐变）
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
    val isSolid: Boolean = false,             // 纯色主题
    val isDefault: Boolean = false,
) {
    val key: Color get() = Color(keyColor)
    val accent: Color get() = Color(accentColor)
}

/** 内置渐变预设 */
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

    /** 纯色主题（keyColor == accentColor == 单一背景色） */
    val solids = listOf(
        LiytuThemePreset(id = "solid_black", name = "夜黑", keyColor = 0xFF14151A, accentColor = 0xFF14151A, isSolid = true, liquidGlassEnabled = false),
        LiytuThemePreset(id = "solid_white", name = "云白", keyColor = 0xFFF3F4F8, accentColor = 0xFFF3F4F8, isSolid = true, liquidGlassEnabled = false),
        LiytuThemePreset(id = "solid_graphite", name = "石墨", keyColor = 0xFF3A3F4B, accentColor = 0xFF3A3F4B, isSolid = true, liquidGlassEnabled = false),
        LiytuThemePreset(id = "solid_crimson", name = "绯红", keyColor = 0xFFE5484D, accentColor = 0xFFE5484D, isSolid = true, liquidGlassEnabled = false),
        LiytuThemePreset(id = "solid_orange", name = "橙阳", keyColor = 0xFFFF7A45, accentColor = 0xFFFF7A45, isSolid = true, liquidGlassEnabled = false),
        LiytuThemePreset(id = "solid_yellow", name = "明黄", keyColor = 0xFFF5B70D, accentColor = 0xFFF5B70D, isSolid = true, liquidGlassEnabled = false),
        LiytuThemePreset(id = "solid_emerald", name = "翡翠", keyColor = 0xFF30A46C, accentColor = 0xFF30A46C, isSolid = true, liquidGlassEnabled = false),
        LiytuThemePreset(id = "solid_azure", name = "青蓝", keyColor = 0xFF0091FF, accentColor = 0xFF0091FF, isSolid = true, liquidGlassEnabled = false),
        LiytuThemePreset(id = "solid_violet", name = "罗兰", keyColor = 0xFF8E4EC6, accentColor = 0xFF8E4EC6, isSolid = true, liquidGlassEnabled = false),
    )
}
