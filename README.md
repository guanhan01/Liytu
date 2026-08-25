# Liytu

一个用 **Jetpack Compose + [Backdrop](https://github.com/kyant0/backdrop)** 做的 **Liquid Glass（液态玻璃）** 示例 App。

主页展示一块带背景模糊、渐变底色的玻璃卡片。核心 API：`rememberLayerBackdrop` / `layerBackdrop` / `drawBackdrop` + `effects.blur`。

## 技术栈

- AGP 9.3.2（内置 Kotlin）+ Kotlin 2.4.10
- JetBrains Compose 1.11.x（编译解析到 androidx.compose）
- Backdrop 2.0.0、Shapes 1.2.0
- compileSdk 37 / minSdk 23 / targetSdk 37

## 如何构建 APK

> 说明：本工程用 `compileSdk 37` 且 Backdrop 依赖 `android:attr/lStar`(API34+)。arm64 本机没有支持 API37 的 arm64 aapt2，会卡在资源链接，因此推荐用 **x86-64 环境（GitHub Actions）** 构建。

### 方式一：GitHub Actions（推荐）

1. 把本目录 `git push` 到一个 GitHub 仓库（`main`/`master` 分支自动触发，或手动 `workflow_dispatch`）。
2. 打开仓库 **Actions** → 等待 `Build Liytu APK` 完成。
3. 进入该次运行 **Artifacts**，下载 `Liytu-debug-apk`，解压得到 `app-debug.apk`。
4. 传到 Android 手机安装即可。

### 方式二：本机 x86-64（可选）

在 x86-64 + JDK17 + Android SDK 37 的机器上：

```bash
./gradlew :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`

## 结构

- `app/src/main/kotlin/com/liytu/MainActivity.kt` — Liquid Glass 界面
- `.github/workflows/build.yml` — GitHub Actions 构建脚本
