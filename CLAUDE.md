# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run all unit tests (local JVM, both modules)
./gradlew test

# Run only app module unit tests
./gradlew :app:testDebugUnitTest

# Run a single unit test class
./gradlew :app:testDebugUnitTest --tests "com.example.basictestapplication.LocalCacheSampleTest"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint

# Generate Baseline Profile (requires connected device; baselineprofile module has useConnectedDevices = true)
./gradlew :app:generateReleaseBaselineProfile -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile

# Run macrobenchmarks (requires connected device)
./gradlew :baselineprofile:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark

# Convert AAB to APK (using local bundletool jar)
java -jar tools/bundletool-all-1.18.3.jar build-apks --bundle=app/build/outputs/bundle/release/app-release.aab --output=app/build/outputs/apk/release/app.apks --mode=universal
```

Verify the bundletool jar path before running — the project ships `bundletool-all-1.18.3.jar` in the `tools/` directory.

## Architecture

**Stack**: AGP 8.3.0, Kotlin 1.9.23, Jetpack Compose (BOM 2024.03, compiler extension 1.5.11), Hilt 2.51.1, DataStore, Glance 1.0.0, WorkManager 2.9.0, Timber 5.0.1, compileSdk/targetSdk 34, minSdk 24. Java 17 target. `buildConfig = true` is enabled.

### Modules

- **`:app`** — Main application. Compose UI, widget, worker, DI.
- **`:baselineprofile`** — Baseline Profile generator and startup macrobenchmarks. Depends on `:app` via `targetProjectPath`. Uses `minSdk = 28` (app is 24) and `useConnectedDevices = true`.

### Key Components

**DI (`App.kt` + `di/AppModule.kt`)**:
- `App` is the `@HiltAndroidApp` Application; it also implements `Configuration.Provider` to inject Hilt into WorkManager workers. Plants `Timber.DebugTree()` in debug builds.
- `AppModule` provides `WorkManager` singleton. `DispatcherModule` provides named `CoroutineDispatcher` qualifiers (`@DefaultDispatcher`, `@IoDispatcher`).

**MVVM (`MainViewModel.kt` + `LocalCacheSample.kt`)**:
- `MainViewModel` is a `@HiltViewModel` that depends on `LocalCacheSampleImpl` (in-memory `StateFlow` cache) and `GlanceDataProvider`.
- `LocalCacheSample` is the interface; `LocalCacheSampleImpl` stores `TodoList` items in a `MutableStateFlow`.

**Widget (`glance/`)**:
- `GlanceDataStoreWidget` — Jetpack Glance widget with responsive sizing (small/horizontal/big). Renders DataStore text and navigates to `MainActivity` on click.
- `GlanceDataProvider` — Singleton that reads/writes text via `DataStore<Preferences>` and triggers widget refresh on save.
- `WidgetReceiver` — Standard `GlanceAppWidgetReceiver`.

**Background Work (`worker/WidgetWorker.kt`)**:
- `WidgetWorkerAdapter` — Schedules a periodic `WidgetWorker` every 15 minutes via `WorkManager`.
- `WidgetWorker` (`@HiltWorker`) — Generates random text, persists it via `GlanceDataProvider`, and updates all widget instances.

**UI (`MainActivity.kt`)**:
- Single-Activity Compose app. `MainActivity` is `@AndroidEntryPoint`. Content shows cached data, a "Set cache value" button, and a `LazyColumn` with 50 items + footer. A "Scroll to top" FAB appears when scrolled past the first item. Uses multi-preview annotations.

### Testing

- **Unit tests** (`app/src/test/`): Organized into `unit/` (JUnit 4), `mockk/` (MockK专项), `viewmodel/`. JUnit 4 + MockK 1.13.8 + kotlinx-coroutines-test. ~60 test cases across 6 files.
- **Instrumented tests** (`app/src/androidTest/`): Organized into `compose/` (Kakao/Cup), `uiautomator/` (UiAutomator), `screens/` (Screen Models). Tests verify cache button, lazy list, footer, scroll-to-top FAB, and system-level interactions (Back/Home/rotation).
- **Monkey tests** (`scripts/monkey_test.sh|bat`): 3 scenarios (500/2000/10000 events) with configurable event distribution.
- **APK tests** (`scripts/apk_test.sh|bat`): 9-step pipeline (build → analyze → sign → install → launch → uninstall).
- **JaCoCo**: Integrated in `app/build.gradle.kts`, generates HTML/XML reports via `./gradlew :app:testDebugUnitTest :app:jacocoTestReport`.
- **Tools**: `tools/bundletool-all-1.18.3.jar` for AAB→APK conversion.
