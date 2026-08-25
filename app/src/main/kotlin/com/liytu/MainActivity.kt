package com.liytu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LiytuApp() }
    }
}

@Composable
fun LiytuApp() {
    val backdrop = rememberLayerBackdrop()
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF6F86FF),
                            Color(0xFF9A6FFF),
                            Color(0xFFFF6FA0),
                            Color(0xFFFFA06F)
                        )
                    )
                )
        )
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun GlassCard(backdrop: Backdrop, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(28.dp) },
                effects = {
                    blur(32.dp.toPx())
                }
            )
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicText(
            "Liytu",
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        BasicText(
            "Liquid Glass · Compose",
            style = TextStyle(color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
        )
    }
}
