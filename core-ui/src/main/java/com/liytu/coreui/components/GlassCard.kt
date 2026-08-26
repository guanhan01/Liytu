package com.liytu.coreui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.liytu.coreui.theme.LocalAppStyle
import com.liytu.coreui.theme.ThemeStyle
import com.liytu.coreui.theme.LocalAppBackdrop

/**
 * 液态玻璃卡片：基于 backdrop 的模糊玻璃质感（Miuix 风格）。
 * Material 风格下自动降级为直角纯色卡片（更接近 Material 规范）。
 * Android 13+ 由 backdrop 渲染真实 liquid glass；低版本自动降级为纯色高光模拟。
 */
@Composable
fun GlassCard(
    backdrop: Backdrop? = LocalAppBackdrop.current,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    blurRadius: Dp = 32.dp,
    tint: Color = Color.White.copy(alpha = 0.22f),
    content: @Composable () -> Unit,
) {
    val style = LocalAppStyle.current
    if (style == ThemeStyle.MATERIAL) {
        // Material 风格：小圆角 + 白色半透明面 + 细边框
        val shape = RoundedCornerShape(12.dp)
        Box(
            modifier = modifier
                .background(Color.White.copy(alpha = 0.08f), shape)
                .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
                .padding(16.dp)
        ) {
            content()
        }
    } else {
        val shape = RoundedCornerShape(cornerRadius)
        Box(
            modifier = modifier
                .then(
                    if (backdrop != null) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { shape },
                            effects = { blur(blurRadius.toPx()) }
                        )
                    } else Modifier
                )
                .background(tint, shape)
                .padding(16.dp)
        ) {
            content()
        }
    }
}
