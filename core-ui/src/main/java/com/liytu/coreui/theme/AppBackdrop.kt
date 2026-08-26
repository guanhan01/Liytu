package com.liytu.coreui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.kyant.backdrop.Backdrop

/**
 * 全局 App 层液态玻璃 Backdrop。由 LiytuApp 在根部提供，
 * 所有 GlassCard / 玻璃组件默认从这里取 backdrop，无需逐层传参。
 */
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop?> { null }
