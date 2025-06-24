#!/bin/bash

# Spring Cloud 微服务停止脚本

echo "=== Spring Cloud 微服务停止脚本 ==="
echo

# 停止服务生产者
if [ -f logs/provider.pid ]; then
    PROVIDER_PID=$(cat logs/provider.pid)
    if ps -p $PROVIDER_PID > /dev/null; then
        echo "停止服务生产者 (PID: $PROVIDER_PID)..."
        kill $PROVIDER_PID
        echo "✓ 服务生产者已停止"
    else
        echo "服务生产者进程不存在"
    fi
    rm -f logs/provider.pid
else
    echo "未找到服务生产者 PID 文件"
fi

# 停止服务消费者
if [ -f logs/consumer.pid ]; then
    CONSUMER_PID=$(cat logs/consumer.pid)
    if ps -p $CONSUMER_PID > /dev/null; then
        echo "停止服务消费者 (PID: $CONSUMER_PID)..."
        kill $CONSUMER_PID
        echo "✓ 服务消费者已停止"
    else
        echo "服务消费者进程不存在"
    fi
    rm -f logs/consumer.pid
else
    echo "未找到服务消费者 PID 文件"
fi

# 强制停止相关 Java 进程
echo
echo "检查并清理相关 Java 进程..."
PROCESSES=$(ps aux | grep -E '(service-provider|service-consumer)' | grep -v grep | awk '{print $2}')
if [ ! -z "$PROCESSES" ]; then
    echo "发现残留进程，正在清理..."
    echo $PROCESSES | xargs kill -9
    echo "✓ 残留进程已清理"
else
    echo "✓ 无残留进程"
fi

echo
echo "=== 所有服务已停止 ==="