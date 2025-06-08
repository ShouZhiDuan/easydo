#!/bin/bash

# HTTP-FTP 服务停止脚本

echo "=== HTTP-FTP 通信系统停止脚本 ==="
echo

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# 读取 PID 文件
SERVICE_A_PID_FILE="$SCRIPT_DIR/.service-a.pid"
SERVICE_B_PID_FILE="$SCRIPT_DIR/.service-b.pid"

# 停止 Service A
if [ -f "$SERVICE_A_PID_FILE" ]; then
    SERVICE_A_PID=$(cat "$SERVICE_A_PID_FILE")
    if ps -p $SERVICE_A_PID > /dev/null 2>&1; then
        echo "停止 Service A (PID: $SERVICE_A_PID)..."
        kill $SERVICE_A_PID
        sleep 3
        
        # 强制停止如果还在运行
        if ps -p $SERVICE_A_PID > /dev/null 2>&1; then
            echo "强制停止 Service A..."
            kill -9 $SERVICE_A_PID
        fi
        echo "✓ Service A 已停止"
    else
        echo "Service A 进程不存在 (PID: $SERVICE_A_PID)"
    fi
    rm -f "$SERVICE_A_PID_FILE"
else
    echo "未找到 Service A PID 文件，尝试通过端口查找..."
    SERVICE_A_PID=$(lsof -ti:8080)
    if [ ! -z "$SERVICE_A_PID" ]; then
        echo "找到占用端口 8080 的进程 (PID: $SERVICE_A_PID)，正在停止..."
        kill $SERVICE_A_PID
        sleep 2
        if ps -p $SERVICE_A_PID > /dev/null 2>&1; then
            kill -9 $SERVICE_A_PID
        fi
        echo "✓ Service A 已停止"
    else
        echo "未找到 Service A 进程"
    fi
fi

# 停止 Service B
if [ -f "$SERVICE_B_PID_FILE" ]; then
    SERVICE_B_PID=$(cat "$SERVICE_B_PID_FILE")
    if ps -p $SERVICE_B_PID > /dev/null 2>&1; then
        echo "停止 Service B (PID: $SERVICE_B_PID)..."
        kill $SERVICE_B_PID
        sleep 3
        
        # 强制停止如果还在运行
        if ps -p $SERVICE_B_PID > /dev/null 2>&1; then
            echo "强制停止 Service B..."
            kill -9 $SERVICE_B_PID
        fi
        echo "✓ Service B 已停止"
    else
        echo "Service B 进程不存在 (PID: $SERVICE_B_PID)"
    fi
    rm -f "$SERVICE_B_PID_FILE"
else
    echo "未找到 Service B PID 文件，尝试通过端口查找..."
    SERVICE_B_PID=$(lsof -ti:8081)
    if [ ! -z "$SERVICE_B_PID" ]; then
        echo "找到占用端口 8081 的进程 (PID: $SERVICE_B_PID)，正在停止..."
        kill $SERVICE_B_PID
        sleep 2
        if ps -p $SERVICE_B_PID > /dev/null 2>&1; then
            kill -9 $SERVICE_B_PID
        fi
        echo "✓ Service B 已停止"
    else
        echo "未找到 Service B 进程"
    fi
fi

echo
echo "=== 停止完成 ==="
echo

# 检查端口是否已释放
echo "检查端口状态:"
if lsof -ti:8080 > /dev/null 2>&1; then
    echo "⚠ 端口 8080 仍被占用"
else
    echo "✓ 端口 8080 已释放"
fi

if lsof -ti:8081 > /dev/null 2>&1; then
    echo "⚠ 端口 8081 仍被占用"
else
    echo "✓ 端口 8081 已释放"
fi

echo
echo "如需重新启动服务，请运行: ./start-services.sh"