package com.liytu.coreui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Liytu 全局主题入口：根据预设生成 Material3 配色，并包裹 Miuix 主题 */
@Composable
fun LiytuTheme(
    preset: LiytuThemePreset,
    content: @Composable () -> Unit,
) {
    val dark = when (preset.colorMode) {
        ColorMode.LIGHT -> false
        ColorMode.DARK -> true
        ColorMode.SYSTEM -> isSystemInDarkTheme()
    }
    val key = preset.key
    val accent = preset.accent
    val scheme = if (dark) {
        darkColorScheme(
            primary = key,
            onPrimary = Color.White,
            primaryContainer = key.copy(alpha = 0.25f),
            onPrimaryContainer = Color(0xFFE8E9FF),
            secondary = accent,
            onSecondary = Color.White,
            tertiary = accent,
            onTertiary = Color.White,
            background = Color(0xFF101014),
            onBackground = Color(0xFFECECF4),
            surface = Color(0xFF17171F),
            onSurface = Color(0xFFECECF4),
            surfaceVariant = Color(0xFF232330),
            onSurfaceVariant = Color(0xFFB8B8C8),
        )
    } else {
        lightColorScheme(
            primary = key,
            onPrimary = Color.White,
            primaryContainer = key.copy(alpha = 0.18f),
            onPrimaryContainer = Color(0xFF1A1A40),
            secondary = accent,
            onSecondary = Color.White,
            tertiary = accent,
            onTertiary = Color.White,
            background = Color(0xFFF6F7FB),
            onBackground = Color(0xFF17171F),
            surface = Color.White,
            onSurface = Color(0xFF17171F),
            surfaceVariant = Color(0xFFEDEEF4),
            onSurfaceVariant = Color(0xFF5A5A6A),
        )
    }
    MaterialTheme(
        colorScheme = scheme,
        shapes = liytuShapes(preset.globalCornerRadius),
        content = {
            MiuixTheme {
                content()
            }
        }
    )
}

/** 当前主题预设（供组件读取） */
object LiytuThemeAccess {
    val currentPreset: LiytuThemePreset
        @Composable @ReadOnlyComposable
        get() = androidx.compose.runtime.LocalLiytuPreset.current
}
