package com.liytu.coreui.components

import androidx.compose.foundation.background
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

/**
 * 液态玻璃卡片：基于 backdrop 的模糊玻璃质感。
 * Android 13+ 由 backdrop 渲染真实 liquid glass；低版本自动降级为纯色高光模拟。
 */
@Composable
fun GlassCard(
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    blurRadius: Dp = 32.dp,
    tint: Color = Color.White.copy(alpha = 0.22f),
    content: @Composable () -> Unit,
) {
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
