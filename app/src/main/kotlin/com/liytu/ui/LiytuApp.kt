package com.liytu.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.liytu.coreui.liquid.LiquidBottomTab
import com.liytu.coreui.liquid.LiquidBottomTabs
import com.liytu.coreui.theme.LiytuThemePreset
import com.liytu.coreui.theme.LocalAppBackdrop
import com.liytu.feature.books.BooksComicsScreen
import com.liytu.feature.home.HomeScreen
import com.liytu.feature.mine.MineScreen
import com.liytu.feature.music.MusicScreen
import com.liytu.feature.video.VideoScreen

private val tabIcons = listOf<ImageVector>(
    Icons.Filled.Home,
    Icons.Filled.PlayArrow,
    Icons.Filled.Star,
    Icons.Filled.List,
    Icons.Filled.Person,
)
private val tabLabels = listOf("首页", "音乐", "影视", "书漫", "我的")

/** 根据预设与液态玻璃开关 / 强度计算背景渐变色 */
private fun backgroundColors(preset: LiytuThemePreset): List<Color> {
    val intensity = preset.liquidGlassIntensity.coerceIn(0f, 1f)
    return if (preset.liquidGlassEnabled) {
        listOf(
            preset.key,
            preset.key.copy(alpha = 0.55f + 0.35f * intensity),
            preset.accent.copy(alpha = 0.60f + 0.30f * intensity),
            preset.accent,
        )
    } else {
        // 关闭液态玻璃：更实的渐变（模拟纯色 + 高光）
        listOf(
            preset.key.copy(alpha = 0.94f),
            preset.key.copy(alpha = 0.82f),
            preset.accent.copy(alpha = 0.55f),
            preset.accent.copy(alpha = 0.78f),
        )
    }
}

@Composable
fun LiytuApp(preset: LiytuThemePreset, onPresetChange: (LiytuThemePreset) -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val backdrop = rememberLayerBackdrop()

    Box(Modifier.fillMaxSize()) {
        // 液态玻璃背景层：渐变铺满（含状态栏区域），随液态玻璃开关 / 强度实时变化
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
                .background(Brush.linearGradient(backgroundColors(preset)))
        )
        // 内容层：全局提供 backdrop，供所有 GlassCard 使用
        CompositionLocalProvider(LocalAppBackdrop provides backdrop) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    // 页面转场：slide + fade，方向随 tab 切换
                    AnimatedContent(
                        targetState = selected,
                        transitionSpec = {
                            val dir = if (targetState > initialState) 1 else -1
                            (slideInHorizontally(
                                tween(350, easing = FastOutSlowInEasing)
                            ) { it / 4 * dir } + fadeIn(tween(350))) togetherWith
                                (slideOutHorizontally(
                                    tween(350, easing = FastOutSlowInEasing)
                                ) { -it / 4 * dir } + fadeOut(tween(200)))
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "tab",
                    ) { tab ->
                        when (tab) {
                            0 -> HomeScreen(onOpenTab = { selected = it })
                            1 -> MusicScreen()
                            2 -> VideoScreen()
                            3 -> BooksComicsScreen()
                            4 -> MineScreen(current = preset, onPresetChange = onPresetChange)
                        }
                    }
                }
                // 底部液态玻璃导航
                Row(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    LiquidBottomTabs(
                        selectedTabIndex = { selected },
                        onTabSelected = { selected = it },
                        backdrop = backdrop,
                        tabsCount = 5,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .weight(1f),
                    ) {
                        repeat(5) { index ->
                            LiquidBottomTab(onClick = { selected = index }) {
                                Icon(
                                    imageVector = tabIcons[index],
                                    contentDescription = tabLabels[index],
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White,
                                )
                                Text(
                                    text = tabLabels[index],
                                    fontSize = 11.sp,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
