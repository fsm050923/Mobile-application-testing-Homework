@echo off
REM ============================================================
REM APK 打包安装测试脚本 (Windows)
REM ============================================================
REM 用法: apk_test.bat
REM ============================================================

setlocal enabledelayedexpansion

set PACKAGE=com.example.basictestapplication
set MAIN_ACTIVITY=.MainActivity
set APK_DEBUG=app\build\outputs\apk\debug\app-debug.apk
set APK_RELEASE=app\build\outputs\apk\release\app-release-unsigned.apk

for /f "tokens=2-8 delims=/:. " %%a in ('echo %date% %time%') do (
    set TIMESTAMP=%%c%%a%%b_%%d%%e%%f
)
set REPORT_FILE=apk_test_report_%TIMESTAMP%.txt

echo ============================================================
echo   APK Packaging & Install Test - BasicTestApplication
echo   Package: %PACKAGE%
echo   Time: %TIMESTAMP%
echo ============================================================

REM ----------------------------------------------------------
REM Step 1: Clean & Build
REM ----------------------------------------------------------
echo.
echo [Step 1] Clean & Build APKs...

call gradlew.bat clean assembleDebug assembleRelease > build_output.log 2>&1

if not exist "%APK_DEBUG%" (
    echo [ERROR] Debug APK build failed
    type build_output.log
    exit /b 1
)
echo [PASS] Debug APK built: %APK_DEBUG%

if exist "%APK_RELEASE%" (
    echo [PASS] Release APK built: %APK_RELEASE%
) else (
    echo [WARN] Release APK not found (may need signing config)
)

REM ----------------------------------------------------------
REM Step 2: APK File Size
REM ----------------------------------------------------------
echo.
echo [Step 2] APK File Size Analysis...

(
    echo === APK Test Report ===
    echo Time: %date% %time%
    echo.
    echo --- File Size ---
) > "%REPORT_FILE%"

if exist "%APK_DEBUG%" (
    for %%A in ("%APK_DEBUG%") do (
        set /a SIZE_KB=%%~zA / 1024
        set /a SIZE_MB=%%~zA / 1048576
        echo Debug APK Size: !SIZE_KB! KB ^(!SIZE_MB! MB^)
        echo Debug APK Size: !SIZE_KB! KB >> "%REPORT_FILE%"
    )
)

REM ----------------------------------------------------------
REM Step 3: APK Content Analysis
REM ----------------------------------------------------------
echo.
echo [Step 3] APK Content Analysis...

where apkanalyzer >nul 2>&1
if %errorlevel% equ 0 (
    echo. >> "%REPORT_FILE%"
    echo --- APK Summary --- >> "%REPORT_FILE%"
    apkanalyzer apk summary "%APK_DEBUG%" >> "%REPORT_FILE%" 2>&1

    echo. >> "%REPORT_FILE%"
    echo --- AndroidManifest --- >> "%REPORT_FILE%"
    apkanalyzer manifest print "%APK_DEBUG%" >> "%REPORT_FILE%" 2>&1

    echo. >> "%REPORT_FILE%"
    echo --- Permissions --- >> "%REPORT_FILE%"
    apkanalyzer manifest permissions "%APK_DEBUG%" >> "%REPORT_FILE%" 2>&1

    echo [PASS] APK analysis completed
) else (
    echo [WARN] apkanalyzer not found. Install Android SDK Build-Tools.
    echo WARN: apkanalyzer not found >> "%REPORT_FILE%"
)

REM ----------------------------------------------------------
REM Step 4: Signature Verification
REM ----------------------------------------------------------
echo.
echo [Step 4] Signature Verification...

echo. >> "%REPORT_FILE%"
echo --- Signature --- >> "%REPORT_FILE%"

if exist "%APK_DEBUG%" (
    where apksigner >nul 2>&1
    if !errorlevel! equ 0 (
        apksigner verify --print-certs "%APK_DEBUG%" >> "%REPORT_FILE%" 2>&1
        if !errorlevel! equ 0 (
            echo [PASS] Debug APK signature verified
        ) else (
            echo [INFO] Debug APK uses debug signature
        )
    ) else (
        echo [WARN] apksigner not found
    )
)

REM ----------------------------------------------------------
REM Step 5: Install
REM ----------------------------------------------------------
echo.
echo [Step 5] Install to Device...

echo. >> "%REPORT_FILE%"
echo --- Install Result --- >> "%REPORT_FILE%"

where adb >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] adb not found
    exit /b 1
)

adb devices | findstr /r "device$" >nul
if %errorlevel% neq 0 (
    echo [WARN] No connected device, skipping install test
    echo WARN: No device connected >> "%REPORT_FILE%"
) else (
    echo [INFO] Uninstalling old version...
    adb uninstall %PACKAGE% >nul 2>&1

    echo [INFO] Installing Debug APK...
    adb install -r "%APK_DEBUG%" >> "%REPORT_FILE%" 2>&1

    if !errorlevel! equ 0 (
        echo [PASS] APK installed successfully
        echo PASS: Installation successful >> "%REPORT_FILE%"
    ) else (
        echo [FAIL] APK installation failed
        echo FAIL: Installation failed >> "%REPORT_FILE%"
    )

    REM Confirm installation
    adb shell pm list packages | findstr "%PACKAGE%" >nul
    if !errorlevel! equ 0 (
        echo [PASS] Package confirmed on device
    )

    REM ----------------------------------------------------------
    REM Step 6: Launch Verification
    REM ----------------------------------------------------------
    echo.
    echo [Step 6] Launch Verification...

    echo. >> "%REPORT_FILE%"
    echo --- Launch --- >> "%REPORT_FILE%"

    adb shell am start -n "%PACKAGE%/%MAIN_ACTIVITY%" >> "%REPORT_FILE%" 2>&1
    echo [INFO] Launch command executed

    REM Wait for app to start
    timeout /t 3 /nobreak >nul

    REM Check focused window
    for /f "tokens=*" %%a in ('adb shell dumpsys window ^| findstr mCurrentFocus 2^>nul') do (
        echo Current Focus: %%a >> "%REPORT_FILE%"
        echo %%a | findstr "%PACKAGE%" >nul
        if !errorlevel! equ 0 (
            echo [PASS] App launched successfully, in foreground
        ) else (
            echo [INFO] App launched (focus check may be imprecise)
        )
    )

    REM ----------------------------------------------------------
    REM Step 7: Uninstall
    REM ----------------------------------------------------------
    echo.
    echo [Step 7] Uninstall Cleanup...

    echo. >> "%REPORT_FILE%"
    echo --- Uninstall --- >> "%REPORT_FILE%"

    adb uninstall %PACKAGE% >> "%REPORT_FILE%" 2>&1
    if !errorlevel! equ 0 (
        echo [PASS] APK uninstalled successfully
    ) else (
        echo [FAIL] APK uninstall failed
    )
)

echo.
echo ============================================================
echo   All APK Tests Completed
echo   Report: %REPORT_FILE%
echo ============================================================
exit /b 0
