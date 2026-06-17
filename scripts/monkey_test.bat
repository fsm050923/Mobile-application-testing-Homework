@echo off
REM ============================================================
REM Monkey 压力测试脚本 (Windows)
REM ============================================================
REM 用法: monkey_test.bat [light|medium|heavy|all]
REM 默认: all (运行全部三个场景)
REM ============================================================

setlocal enabledelayedexpansion

set PACKAGE=com.example.basictestapplication
set SEED_LIGHT=12345
set SEED_MEDIUM=23456
set SEED_HEAVY=34567
set EVENTS_LIGHT=500
set EVENTS_MEDIUM=2000
set EVENTS_HEAVY=10000
set THROTTLE_LIGHT=300
set THROTTLE_MEDIUM=150
set THROTTLE_HEAVY=50
set LOG_DIR=monkey_logs

REM 时间戳
for /f "tokens=2-8 delims=/:. " %%a in ('echo %date% %time%') do (
    set TIMESTAMP=%%c%%a%%b_%%d%%e%%f
)

set PCT_TOUCH=45
set PCT_MOTION=15
set PCT_TRACKBALL=5
set PCT_NAV=10
set PCT_MAJORNAV=10
set PCT_SYSKEYS=5
set PCT_APPSWITCH=10

echo ============================================================
echo   Monkey Pressure Test - BasicTestApplication
echo   Package: %PACKAGE%
echo   Time: %TIMESTAMP%
echo ============================================================

REM Check adb
where adb >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] adb command not found. Ensure Android SDK is installed and PATH is configured.
    exit /b 1
)

REM Check device
adb devices | findstr /r "device$" >nul
if %errorlevel% neq 0 (
    echo [ERROR] No connected Android device/emulator detected.
    exit /b 1
)
echo [INFO] Device connected.

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

if "%~1"=="" set MODE=all
if not "%~1"=="" set MODE=%~1

if "%MODE%"=="light" call :run_monkey light %EVENTS_LIGHT% %THROTTLE_LIGHT% %SEED_LIGHT%
if "%MODE%"=="medium" call :run_monkey medium %EVENTS_MEDIUM% %THROTTLE_MEDIUM% %SEED_MEDIUM%
if "%MODE%"=="heavy" call :run_monkey heavy %EVENTS_HEAVY% %THROTTLE_HEAVY% %SEED_HEAVY%
if "%MODE%"=="all" (
    call :run_monkey light %EVENTS_LIGHT% %THROTTLE_LIGHT% %SEED_LIGHT%
    call :run_monkey medium %EVENTS_MEDIUM% %THROTTLE_MEDIUM% %SEED_MEDIUM%
    call :run_monkey heavy %EVENTS_HEAVY% %THROTTLE_HEAVY% %SEED_HEAVY%
)

echo ============================================================
echo   All Monkey Tests Completed
echo   Logs: %LOG_DIR%\
echo ============================================================
exit /b 0

:run_monkey
set LEVEL=%~1
set EVENTS=%~2
set THROTTLE=%~3
set SEED=%~4
set LOGFILE=%LOG_DIR%\monkey_%LEVEL%_%TIMESTAMP%.log

echo.
echo ----------------------------------------------------------
echo   Scene: %LEVEL%
echo   Events: %EVENTS%
echo   Throttle: %THROTTLE%ms
echo   Seed: %SEED%
echo   Log: %LOGFILE%
echo ----------------------------------------------------------

adb shell monkey ^
    -p %PACKAGE% ^
    -v ^
    --throttle %THROTTLE% ^
    --pct-touch %PCT_TOUCH% ^
    --pct-motion %PCT_MOTION% ^
    --pct-trackball %PCT_TRACKBALL% ^
    --pct-nav %PCT_NAV% ^
    --pct-majornav %PCT_MAJORNAV% ^
    --pct-syskeys %PCT_SYSKEYS% ^
    --pct-appswitch %PCT_APPSWITCH% ^
    --ignore-crashes ^
    --ignore-timeouts ^
    --monitor-native-crashes ^
    -s %SEED% ^
    %EVENTS% > "%LOGFILE%" 2>&1

findstr /c:"Monkey finished" "%LOGFILE%" >nul
if !errorlevel! equ 0 (
    echo [PASS] %LEVEL% test completed
) else (
    echo [WARN] %LEVEL% test may not have completed normally
)

exit /b 0
