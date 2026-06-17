#!/bin/bash
# ============================================================
# Monkey 压力测试脚本 (Linux/Mac/Git Bash)
# ============================================================
# 用法: bash scripts/monkey_test.sh [light|medium|heavy|all]
# 默认: all (运行全部三个场景)
# ============================================================

PACKAGE="com.example.basictestapplication"
SEED_LIGHT=12345
SEED_MEDIUM=23456
SEED_HEAVY=34567
EVENTS_LIGHT=500
EVENTS_MEDIUM=2000
EVENTS_HEAVY=10000
THROTTLE_LIGHT=300
THROTTLE_MEDIUM=150
THROTTLE_HEAVY=50
LOG_DIR="monkey_logs"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Monkey 事件类型分布 (百分比)
PCT_TOUCH=45        # 触摸事件(点击、长按)
PCT_MOTION=15       # 滑动事件
PCT_TRACKBALL=5     # 轨迹球
PCT_NAV=10          # 导航事件(上下左右)
PCT_MAJORNAV=10     # 主要导航(返回键)
PCT_SYSKEYS=5       # 系统按键(Home、音量)
PCT_APPSWITCH=10    # Activity 切换

echo "============================================================"
echo "  Monkey 压力测试 - BasicTestApplication"
echo "  包名: $PACKAGE"
echo "  时间: $TIMESTAMP"
echo "============================================================"

# 检查 adb 是否可用
if ! command -v adb &> /dev/null; then
    echo "[ERROR] adb 命令未找到，请确保 Android SDK 已安装并配置 PATH"
    exit 1
fi

# 检查设备是否连接
DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "[ERROR] 未检测到已连接的 Android 设备/模拟器"
    echo "请连接设备后重试"
    exit 1
fi
echo "[INFO] 检测到 $DEVICE_COUNT 个设备"

# 创建日志目录
mkdir -p "$LOG_DIR"

# ----------------------------------------------------------
# Monkey 测试函数
# ----------------------------------------------------------
run_monkey() {
    local LEVEL=$1
    local EVENTS=$2
    local THROTTLE=$3
    local SEED=$4
    local LOGFILE="$LOG_DIR/monkey_${LEVEL}_${TIMESTAMP}.log"

    echo ""
    echo "----------------------------------------------------------"
    echo "  场景: $LEVEL"
    echo "  事件数: $EVENTS"
    echo "  Throttle: ${THROTTLE}ms"
    echo "  种子: $SEED"
    echo "  日志: $LOGFILE"
    echo "----------------------------------------------------------"

    adb shell monkey \
        -p "$PACKAGE" \
        -v \
        --throttle "$THROTTLE" \
        --pct-touch "$PCT_TOUCH" \
        --pct-motion "$PCT_MOTION" \
        --pct-trackball "$PCT_TRACKBALL" \
        --pct-nav "$PCT_NAV" \
        --pct-majornav "$PCT_MAJORNAV" \
        --pct-syskeys "$PCT_SYSKEYS" \
        --pct-appswitch "$PCT_APPSWITCH" \
        --ignore-crashes \
        --ignore-timeouts \
        --monitor-native-crashes \
        -s "$SEED" \
        "$EVENTS" \
        2>&1 | tee "$LOGFILE"

    # 检查结果
    if grep -q "Monkey finished" "$LOGFILE"; then
        echo "[PASS] $LEVEL 测试完成"
    else
        echo "[WARN] $LEVEL 测试可能未正常完成，请检查日志"
    fi

    # 统计崩溃和 ANR
    CRASHES=$(grep -c "CRASH" "$LOGFILE" || true)
    ANRS=$(grep -c "ANR" "$LOGFILE" || true)
    echo "  - 崩溃: $CRASHES 次"
    echo "  - ANR: $ANRS 次"
}

# ----------------------------------------------------------
# 执行测试
# ----------------------------------------------------------
case "${1:-all}" in
    light)
        run_monkey "light" "$EVENTS_LIGHT" "$THROTTLE_LIGHT" "$SEED_LIGHT"
        ;;
    medium)
        run_monkey "medium" "$EVENTS_MEDIUM" "$THROTTLE_MEDIUM" "$SEED_MEDIUM"
        ;;
    heavy)
        run_monkey "heavy" "$EVENTS_HEAVY" "$THROTTLE_HEAVY" "$SEED_HEAVY"
        ;;
    all)
        run_monkey "light" "$EVENTS_LIGHT" "$THROTTLE_LIGHT" "$SEED_LIGHT"
        run_monkey "medium" "$EVENTS_MEDIUM" "$THROTTLE_MEDIUM" "$SEED_MEDIUM"
        run_monkey "heavy" "$EVENTS_HEAVY" "$THROTTLE_HEAVY" "$SEED_HEAVY"
        ;;
    *)
        echo "用法: bash monkey_test.sh [light|medium|heavy|all]"
        echo "  light  - 轻量级冒烟测试 (500 事件)"
        echo "  medium - 中量级功能测试 (2000 事件)"
        echo "  heavy  - 重量级压力测试 (10000 事件)"
        echo "  all    - 运行全部 (默认)"
        exit 1
        ;;
esac

echo ""
echo "============================================================"
echo "  Monkey 测试全部完成"
echo "  日志目录: $LOG_DIR/"
echo "============================================================"
