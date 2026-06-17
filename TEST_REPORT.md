# BasicTestApplication 测试报告

移动应用测试 期末大作业

---

## 基本信息

- 项目：BasicTestApplication
- 技术栈：Kotlin + Jetpack Compose + Hilt + DataStore + Glance + WorkManager
- 测试方法：JUnit 单元测试、MockK 辅助测试、JaCoCo 覆盖率、GUI 自动化、Monkey 压力、APK 打包安装

## 项目结构

```
app/src/
├── main/.../basictestapplication/
│   ├── App.kt                    # Application + WorkManager配置
│   ├── MainActivity.kt           # Compose UI
│   ├── MainViewModel.kt          # ViewModel
│   ├── LocalCacheSample.kt       # 缓存接口+实现+数据类
│   ├── di/AppModule.kt           # Hilt DI模块
│   ├── glance/                   # Glance桌面小组件
│   └── worker/WidgetWorker.kt    # WorkManager后台任务
├── test/.../basictestapplication/
│   ├── unit/          # JUnit单元测试 (3文件, ~42用例)
│   ├── mockk/         # MockK专项测试 (2文件, ~19用例)
│   └── viewmodel/     # ViewModel测试 (1文件, 7用例)
└── androidTest/.../basictestapplication/
    ├── compose/       # Kakao/Cup测试 (8用例)
    ├── uiautomator/   # UiAutomator测试 (6用例)
    └── screens/       # Screen Model
```

## 测试方法

### 1. JUnit 单元测试

- 4个测试文件，约42个用例，全部通过
- 覆盖 TodoList 数据类(17)、LocalCacheSampleImpl(15)、MainViewModel(7)、WidgetWorkerAdapter(3)
- 执行：`./gradlew :app:testDebugUnitTest`

### 2. MockK 辅助测试

- 2个测试文件，约19个用例
- 练习了 mock/coEvery/throws/slot/verify/verifyOrder/spyk/relaxed 等常用MockK模式
- 遇到的问题：confirmVerified 和 verifySequence 容易因为内部调用链失败

### 3. JaCoCo 覆盖率

- 在 app/build.gradle.kts 中配置，排除 DI/theme/glance/Hilt生成代码
- 执行：`./gradlew :app:testDebugUnitTest :app:jacocoTestReport`
- 报告：app/build/reports/jacoco/html/index.html

### 4. GUI 自动化测试

- Kakao/Cup Compose测试：8个用例，覆盖按钮、列表、Footer、FAB
- UiAutomator测试：6个用例，覆盖Back/Home/快速点击
- 执行：`./gradlew :app:connectedDebugAndroidTest`

### 5. Monkey 压力测试

- 3个场景：500/2000/10000事件，不同间隔和种子
- 脚本：scripts/monkey_test.sh / .bat
- 执行：`bash scripts/monkey_test.sh all`

### 6. APK 打包安装测试

- 9步流程：构建->分析->签名->安装->启动->卸载
- 脚本：scripts/apk_test.sh / .bat
- 执行：`bash scripts/apk_test.sh`

## 测试覆盖矩阵

| 被测类 | JUnit | MockK | GUI | Monkey | APK |
|---|---|---|---|---|---|
| TodoList | 17 | - | - | 间接 | - |
| LocalCacheSampleImpl | 15 | 被Mock | - | 间接 | - |
| MainViewModel | 7 | 12 | 通过UI | 间接 | - |
| WidgetWorkerAdapter | 3 | Mock | - | - | - |
| GlanceDataProvider | - | 7 | - | 间接 | - |
| MainActivity/UI | - | - | 14 | 直接 | - |
| APK 整体 | - | - | - | - | 9步 |

## 遇到的问题和解决

1. **confirmVerified/verifySequence 失败**：因为 ViewModel 内部会访问 mock 对象的其他属性，这些调用也被 MockK 记录了。解决：改用 verifyOrder 或精确 verify 关键调用。

2. **DataStore.edit 不好 mock**：MutablePreferences 是内部类，MockK 处理不了泛型。解决：改为验证 DataStore 交互模式。

3. **JaCoCo 版本警告**：0.8.8 不兼容 Java 21 class file，但不影响测试。

4. **协程测试需要额外依赖**：kotlinx-coroutines-test 提供了 runTest，用于测试 suspend 函数和 Flow。


