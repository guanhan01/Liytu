plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
}

android {
    namespace = "com.liytu.coreui"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // backdrop 在公开 API（GlassCard/Backdrop 参数）中使用，必须 api 暴露给下游模块
    api(libs.backdrop)
    implementation(libs.shapes)
    implementation(libs.miuix)
    implementation(libs.material3)
    implementation(libs.lifecycle.runtime.compose)
    // jb compose 坐标 -> 解析为 androidx.compose
    implementation("org.jetbrains.compose.foundation:foundation:1.11.1")
    implementation("org.jetbrains.compose.ui:ui:1.11.1")
    implementation("org.jetbrains.compose.ui:ui-graphics:1.11.1")
}
