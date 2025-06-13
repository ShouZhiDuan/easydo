#!/bin/bash

# HTTP-FTP 服务启动脚本

echo "=== HTTP-FTP 通信系统启动脚本 ==="
echo

# 检查 Maven 是否安装
if ! command -v mvn &> /dev/null; then
    echo "错误: Maven 未安装或不在 PATH 中"
    echo "请先安装 Maven: https://maven.apache.org/install.html"
    exit 1
fi

# 检查 Java 是否安装
if ! command -v java &> /dev/null; then
    echo "错误: Java 未安装或不在 PATH 中"
    echo "请先安装 Java 8 或更高版本"
    exit 1
fi

echo "检查环境..."
java -version
mvn -version
echo

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# 创建日志目录
mkdir -p "$SCRIPT_DIR/logs"

echo "开始启动服务..."
echo

# 启动 Service B (服务端)
echo "1. 启动 Service B (HTTP 服务端) - 端口 8081"
cd "$SCRIPT_DIR/service-b"
echo "正在编译 Service B..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "错误: Service B 编译失败"
    exit 1
fi

echo "启动 Service B..."
nohup mvn spring-boot:run > "$SCRIPT_DIR/logs/service-b-startup.log" 2>&1 &
SERVICE_B_PID=$!
echo "Service B 已启动，PID: $SERVICE_B_PID"
echo "日志文件: $SCRIPT_DIR/logs/service-b-startup.log"
echo

# 等待 Service B 启动
echo "等待 Service B 启动完成..."
sleep 10

# 检查 Service B 是否启动成功
if curl -s http://localhost:8081/api/health > /dev/null; then
    echo "✓ Service B 启动成功"
else
    echo "⚠ Service B 可能未完全启动，请检查日志"
fi
echo

# 启动 Service A (客户端)
echo "2. 启动 Service A (HTTP 客户端) - 端口 8080"
cd "$SCRIPT_DIR/service-a"
echo "正在编译 Service A..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "错误: Service A 编译失败"
    exit 1
fi

echo "启动 Service A..."
nohup mvn spring-boot:run > "$SCRIPT_DIR/logs/service-a-startup.log" 2>&1 &
SERVICE_A_PID=$!
echo "Service A 已启动，PID: $SERVICE_A_PID"
echo "日志文件: $SCRIPT_DIR/logs/service-a-startup.log"
echo

# 等待 Service A 启动
echo "等待 Service A 启动完成..."
sleep 10

# 检查 Service A 是否启动成功
if curl -s http://localhost:8080/api/health > /dev/null; then
    echo "✓ Service A 启动成功"
else
    echo "⚠ Service A 可能未完全启动，请检查日志"
fi
echo

echo "=== 启动完成 ==="
echo "Service A (客户端): http://localhost:8080"
echo "Service B (服务端): http://localhost:8081"
echo
echo "进程信息:"
echo "Service A PID: $SERVICE_A_PID"
echo "Service B PID: $SERVICE_B_PID"
echo
echo "日志文件:"
echo "Service A: $SCRIPT_DIR/logs/service-a-startup.log"
echo "Service B: $SCRIPT_DIR/logs/service-b-startup.log"
echo "应用日志: $SCRIPT_DIR/logs/service-a.log, $SCRIPT_DIR/logs/service-b.log"
echo
echo "测试命令:"
echo "健康检查: curl http://localhost:8080/api/health"
echo "GET 请求: curl 'http://localhost:8080/api/request?test=value'"
echo "POST 请求: curl -X POST http://localhost:8080/api/request -H 'Content-Type: application/json' -d '{\"test\":\"data\"}'"
echo
echo "停止服务: kill $SERVICE_A_PID $SERVICE_B_PID"
echo "或使用: ./stop-services.sh"
echo

# 保存 PID 到文件
echo "$SERVICE_A_PID" > "$SCRIPT_DIR/.service-a.pid"
echo "$SERVICE_B_PID" > "$SCRIPT_DIR/.service-b.pid"

echo "服务已在后台运行，按 Ctrl+C 退出此脚本（不会停止服务）"