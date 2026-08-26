package com.liytu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.liytu.feature.books.BooksComicsScreen
import com.liytu.feature.home.HomeScreen
import com.liytu.feature.mine.MineScreen
import com.liytu.feature.music.MusicScreen
import com.liytu.feature.video.VideoScreen
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.ColorFilter

private val tabIcons = listOf<ImageVector>(
    Icons.Filled.Home,
    Icons.Filled.PlayArrow,
    Icons.Filled.Star,
    Icons.Filled.List,
    Icons.Filled.Person,
)
private val tabLabels = listOf("首页", "音乐", "影视", "书漫", "我的")

@Composable
fun LiytuApp(preset: LiytuThemePreset, onPresetChange: (LiytuThemePreset) -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val backdrop = rememberLayerBackdrop()

    Box(Modifier.fillMaxSize()) {
        // 液态玻璃背景层
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
                .background(
                    Brush.linearGradient(
                        listOf(
                            preset.key,
                            preset.key.copy(alpha = 0.75f),
                            preset.accent.copy(alpha = 0.85f),
                            preset.accent,
                        )
                    )
                )
        )
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxSize()) {
                when (selected) {
                    0 -> HomeScreen(backdrop = backdrop, onOpenTab = { selected = it })
                    1 -> MusicScreen()
                    2 -> VideoScreen()
                    3 -> BooksComicsScreen()
                    4 -> MineScreen(current = preset, onPresetChange = onPresetChange)
                }
            }
            Row(Modifier.navigationBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp)) {
                LiquidBottomTabs(
                    selectedTabIndex = { selected },
                    onTabSelected = { selected = it },
                    backdrop = backdrop,
                    tabsCount = 5,
                    modifier = Modifier.padding(horizontal = 6.dp).weight(1f),
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
