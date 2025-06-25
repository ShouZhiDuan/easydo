#!/bin/bash

# 构建所有服务Docker镜像的主脚本
# 同时构建order-service:latest和user-service:latest镜像

set -e  # 遇到错误立即退出

# 脚本信息
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_NAME="istio-demo"

echo "🚀 开始构建所有服务的Docker镜像..."
echo "📁 项目目录: $SCRIPT_DIR"
echo ""

# 服务列表
SERVICES=("order-service" "user-service")
SUCCESS_COUNT=0
FAILED_SERVICES=()

# 构建开始时间
START_TIME=$(date +%s)

# 函数：构建单个服务
build_service() {
    local service=$1
    echo "📦 ==============================================="
    echo "📦 开始构建 $service"
    echo "📦 ==============================================="
    
    if [ -d "$service" ] && [ -f "$service/build-image.sh" ]; then
        cd "$service"
        if ./build-image.sh; then
            echo "✅ $service 构建成功!"
            ((SUCCESS_COUNT++))
        else
            echo "❌ $service 构建失败!"
            FAILED_SERVICES+=("$service")
        fi
        cd "$SCRIPT_DIR"
    else
        echo "❌ 错误: $service 目录或构建脚本不存在"
        FAILED_SERVICES+=("$service")
    fi
    echo ""
}

# 构建所有服务
for service in "${SERVICES[@]}"; do
    build_service "$service"
done

# 计算构建时间
END_TIME=$(date +%s)
BUILD_TIME=$((END_TIME - START_TIME))

# 显示构建结果摘要
echo "🎉 ==============================================="
echo "🎉 构建完成摘要"
echo "🎉 ==============================================="
echo "⏱️  总耗时: ${BUILD_TIME}秒"
echo "✅ 成功构建: $SUCCESS_COUNT/${#SERVICES[@]} 个服务"

if [ $SUCCESS_COUNT -eq ${#SERVICES[@]} ]; then
    echo "🎊 所有服务构建成功!"
    echo ""
    echo "📊 构建的镜像列表:"
    for service in "${SERVICES[@]}"; do
        if docker images | grep -q "$service.*latest"; then
            echo "   📱 $service:latest"
        fi
    done
    echo ""
    echo "🐳 查看所有镜像:"
    echo "   docker images | grep -E '(order-service|user-service)'"
    echo ""
    echo "🚀 快速启动服务:"
    echo "   # 启动order-service"
    echo "   docker run -d -p 8080:8080 --name order-service order-service:latest"
    echo ""
    echo "   # 启动user-service"
    echo "   docker run -d -p 8081:8080 --name user-service user-service:latest"
    echo ""
    echo "   # 检查运行状态"
    echo "   docker ps"
    
    exit 0
else
    echo "❌ 部分服务构建失败:"
    for failed in "${FAILED_SERVICES[@]}"; do
        echo "   ❌ $failed"
    done
    echo ""
    echo "💡 请检查失败的服务构建日志并重试"
    exit 1
fi 