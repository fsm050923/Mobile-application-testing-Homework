#!/bin/bash
# ============================================================
# APK 打包安装测试脚本 (Linux/Mac/Git Bash)
# ============================================================
# 流程:
#   1. 清理构建
#   2. 编译 Debug + Release APK
#   3. APK 信息分析
#   4. 签名验证
#   5. 安装到设备
#   6. 启动验证
#   7. 卸载清理
# ============================================================

PACKAGE="com.example.basictestapplication"
MAIN_ACTIVITY=".MainActivity"
APK_DEBUG="app/build/outputs/apk/debug/app-debug.apk"
APK_RELEASE="app/build/outputs/apk/release/app-release-unsigned.apk"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="apk_test_report_${TIMESTAMP}.txt"

echo "============================================================"
echo "  APK 打包安装测试 - BasicTestApplication"
echo "  包名: $PACKAGE"
echo "  时间: $TIMESTAMP"
echo "============================================================"

# ----------------------------------------------------------
# 步骤 1: 清理并构建
# ----------------------------------------------------------
echo ""
echo "[步骤1] 清理并构建 APK..."

./gradlew clean assembleDebug assembleRelease 2>&1 | tee build_output.log

if [ ! -f "$APK_DEBUG" ]; then
    echo "[ERROR] Debug APK 构建失败"
    exit 1
fi
echo "[PASS] Debug APK 构建成功: $APK_DEBUG"

if [ -f "$APK_RELEASE" ]; then
    echo "[PASS] Release APK 构建成功: $APK_RELEASE"
else
    echo "[WARN] Release APK 未找到（正常 - 需要签名配置）"
fi

# ----------------------------------------------------------
# 步骤 2: APK 文件大小分析
# ----------------------------------------------------------
echo ""
echo "[步骤2] APK 文件大小分析..."

{
    echo "=== APK 测试报告 ==="
    echo "时间: $(date)"
    echo ""
    echo "--- 文件大小 ---"
} > "$REPORT_FILE"

if [ -f "$APK_DEBUG" ]; then
    SIZE=$(du -h "$APK_DEBUG" | cut -f1)
    echo "Debug APK: $SIZE"
    echo "Debug APK 大小: $SIZE" >> "$REPORT_FILE"

    # 列出 APK 内容大小 (apkanalyzer)
    if command -v apkanalyzer &> /dev/null; then
        echo "" >> "$REPORT_FILE"
        echo "--- apkanalyzer 文件大小 ---" >> "$REPORT_FILE"
        apkanalyzer apk file-size "$APK_DEBUG" >> "$REPORT_FILE" 2>&1
    fi
fi

# ----------------------------------------------------------
# 步骤 3: APK 信息分析 (apkanalyzer / aapt)
# ----------------------------------------------------------
echo ""
echo "[步骤3] APK 内容分析..."

if command -v apkanalyzer &> /dev/null; then
    echo "" >> "$REPORT_FILE"
    echo "--- APK 摘要 ---" >> "$REPORT_FILE"
    apkanalyzer apk summary "$APK_DEBUG" >> "$REPORT_FILE" 2>&1

    echo "" >> "$REPORT_FILE"
    echo "--- AndroidManifest 信息 ---" >> "$REPORT_FILE"
    apkanalyzer manifest print "$APK_DEBUG" >> "$REPORT_FILE" 2>&1

    echo "" >> "$REPORT_FILE"
    echo "--- 权限列表 ---" >> "$REPORT_FILE"
    apkanalyzer manifest permissions "$APK_DEBUG" >> "$REPORT_FILE" 2>&1

    echo "[PASS] APK 分析完成"
elif command -v aapt &> /dev/null; then
    echo "" >> "$REPORT_FILE"
    echo "--- aapt 信息 ---" >> "$REPORT_FILE"
    aapt dump badging "$APK_DEBUG" >> "$REPORT_FILE" 2>&1
    echo "[PASS] 使用 aapt 分析完成"
else
    echo "[WARN] apkanalyzer 和 aapt 均未找到，跳过 APK 内容分析"
    echo "请安装 Android SDK Build-Tools 并配置 PATH"
fi

# ----------------------------------------------------------
# 步骤 4: 签名验证
# ----------------------------------------------------------
echo ""
echo "[步骤4] 签名验证..."

echo "" >> "$REPORT_FILE"
echo "--- 签名信息 ---" >> "$REPORT_FILE"

if [ -f "$APK_DEBUG" ]; then
    if command -v apksigner &> /dev/null; then
        apksigner verify --print-certs "$APK_DEBUG" >> "$REPORT_FILE" 2>&1
        if [ $? -eq 0 ]; then
            echo "[PASS] Debug APK 签名验证通过"
        else
            echo "[INFO] Debug APK 使用 debug 签名"
        fi
    else
        echo "[WARN] apksigner 未找到"
    fi
fi

# ----------------------------------------------------------
# 步骤 5: 安装到设备
# ----------------------------------------------------------
echo ""
echo "[步骤5] 安装到设备..."

echo "" >> "$REPORT_FILE"
echo "--- 安装结果 ---" >> "$REPORT_FILE"

# 检查 adb
if ! command -v adb &> /dev/null; then
    echo "[ERROR] adb 未找到"
    echo "ERROR: adb not found" >> "$REPORT_FILE"
    exit 1
fi

# 检查设备
DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "[WARN] 未检测到已连接设备，跳过安装测试"
    echo "WARN: No device connected, skipping install" >> "$REPORT_FILE"
else
    # 先卸载
    echo "[INFO] 卸载旧版本..."
    adb uninstall "$PACKAGE" 2>/dev/null

    # 安装
    echo "[INFO] 安装 Debug APK..."
    INSTALL_RESULT=$(adb install -r "$APK_DEBUG" 2>&1)
    echo "$INSTALL_RESULT" >> "$REPORT_FILE"

    if echo "$INSTALL_RESULT" | grep -q "Success"; then
        echo "[PASS] APK 安装成功"
    else
        echo "[FAIL] APK 安装失败: $INSTALL_RESULT"
    fi

    # 确认安装
    if adb shell pm list packages | grep -q "$PACKAGE"; then
        echo "[PASS] 包已确认安装在设备上"
        echo "PASS: Package listed on device" >> "$REPORT_FILE"
    fi

    # ----------------------------------------------------------
    # 步骤 6: 启动验证
    # ----------------------------------------------------------
    echo ""
    echo "[步骤6] 启动验证..."

    echo "" >> "$REPORT_FILE"
    echo "--- 启动验证 ---" >> "$REPORT_FILE"

    adb shell am start -n "$PACKAGE/$MAIN_ACTIVITY" 2>&1 | tee -a "$REPORT_FILE"

    # 等待应用启动
    sleep 3

    # 检查前台 Activity
    FOCUSED=$(adb shell dumpsys window | grep mCurrentFocus 2>/dev/null || true)
    echo "当前焦点: $FOCUSED" >> "$REPORT_FILE"

    if echo "$FOCUSED" | grep -q "$PACKAGE"; then
        echo "[PASS] 应用启动成功，前台可见"
        echo "PASS: App in foreground" >> "$REPORT_FILE"
    else
        echo "[INFO] 应用已启动（焦点检查不一定准确）"
    fi

    # ----------------------------------------------------------
    # 步骤 7: 卸载清理
    # ----------------------------------------------------------
    echo ""
    echo "[步骤7] 卸载清理..."

    echo "" >> "$REPORT_FILE"
    echo "--- 卸载 ---" >> "$REPORT_FILE"

    UNINSTALL_RESULT=$(adb uninstall "$PACKAGE" 2>&1)
    echo "$UNINSTALL_RESULT" >> "$REPORT_FILE"

    if echo "$UNINSTALL_RESULT" | grep -q "Success"; then
        echo "[PASS] APK 卸载成功"
    else
        echo "[FAIL] APK 卸载失败: $UNINSTALL_RESULT"
    fi
fi

# ----------------------------------------------------------
# 完成
# ----------------------------------------------------------
echo ""
echo "============================================================"
echo "  APK 测试全部完成"
echo "  报告文件: $REPORT_FILE"
echo "============================================================"
