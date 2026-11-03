# 拍题助手 · PhotoSolve

面向 Android 的 AI 搜题应用，支持拍照、相册选图和悬浮窗识屏。无需账号或自建服务器，使用用户自行配置的 OpenAI 兼容视觉模型接口。

## 功能

- 拍照、相册、悬浮识屏三种图片入口。
- 自由矩形裁剪，支持拖动四边、四角和移动选区。
- 搜题页编辑本题提示词，新题恢复默认；可在设置中选择只显示答案。
- 本地历史记录和收藏。
- 首页 / 我的底部导航，系统返回手势，浅色与深色主题。
- 可拖动的半透明磨砂风格悬浮球。

## 使用

1. 在“我的 → API 设置”填写 Base URL、API Key 和模型名称。
2. 选择支持图片输入的模型；接口格式兼容不代表模型具备视觉能力。
3. 在首页开始搜题，选择图片、裁剪并确认提示词后发送。
4. 需要识屏时开启悬浮窗，授予悬浮窗权限，切换到题目页面后点击圆球并确认系统截图授权。

题目图片和提示词会发送至用户配置的 API 服务。历史和收藏保存在本机，API Key 使用 Android Keystore 加密保存。API 费用由所选服务提供方收取。

## 开发环境

- Android 10（API 29）及以上；compileSdk / targetSdk 为 35。
- Kotlin 2.0.21、Jetpack Compose、Room、DataStore。
- Android Gradle Plugin 8.7.3，构建 JDK 17。
- 安装 Android SDK Platform 35 及相应构建工具。

当前仓库尚未包含 Gradle Wrapper（`gradlew` / `gradlew.bat`）。请使用 Android Studio 打开项目根目录，在 Gradle 设置中配置本地 Gradle 和 JDK 17；当前开发环境已使用 Gradle 9.6.0 构建 Debug / Release，存在 Kapt 与 Gradle 弃用警告。

Android SDK 路径通过本机 `local.properties` 配置，该文件不提交到仓库。完成 Gradle 同步后，执行 `:app:assembleDebug`，安装包位于 `app/build/outputs/apk/debug/app-debug.apk`。Release 产物需要自行配置签名后发布。

## 目录

```text
app/src/main/java/com/photosolve/app/
├── capture/     悬浮窗与屏幕截图
├── data/        网络、图片、设置和本地记录
├── domain/      搜题及裁剪逻辑
├── security/    API Key 加密
└── ui/          页面、组件与主题
docs/           验收说明
tests/          裁剪及导航逻辑检查
```

## 验证与限制

已在 Android 16 模拟器验证主要导航、悬浮开关、截图授权和裁剪流程；不同厂商的后台弹出界面和省电策略仍需真机测试。系统禁止截图的页面可能输出黑屏。

参见 [交互验收说明](docs/interaction-verification.md)。文档中引用的 `work/` 脚本属于本地开发辅助文件，不随仓库发布。`tests/CropNavigationChecks.kt` 为独立 Kotlin 检查程序，目前未接入标准 Gradle 测试任务。

模型生成的答案可能有误，请结合题目自行核对。当前仅实现 Android 版本。

## 许可证

许可证尚待项目作者选择。公开仓库本身不等同于授予开源许可证。
