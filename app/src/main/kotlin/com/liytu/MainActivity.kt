package com.liytu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.liytu.coreui.theme.LiytuPresets
import com.liytu.coreui.theme.LiytuTheme
import com.liytu.ui.LiytuApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var preset by remember { mutableStateOf(LiytuPresets.defaults.first()) }
            LiytuTheme(preset = preset) {
                LiytuApp(preset = preset, onPresetChange = { preset = it })
            }
        }
    }
}
