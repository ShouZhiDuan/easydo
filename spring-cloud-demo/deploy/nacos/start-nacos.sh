#!/bin/bash

# Nacos Docker 启动脚本

echo "=== Nacos Docker 启动脚本 ==="
echo

# 检查 Docker 环境
if ! command -v docker &> /dev/null; then
    echo "错误: 未找到 Docker 环境，请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "错误: 未找到 Docker Compose 环境，请先安装 Docker Compose"
    exit 1
fi

# 检查 Docker 服务状态
if ! docker info &> /dev/null; then
    echo "错误: Docker 服务未启动，请先启动 Docker"
    exit 1
fi

echo "✓ Docker 环境检查通过"
echo

# 创建必要的目录
echo "创建数据和日志目录..."
mkdir -p data logs
echo "✓ 目录创建完成"
echo

# 启动 Nacos 服务
echo "启动 Nacos 服务..."
docker-compose up -d

if [ $? -eq 0 ]; then
    echo "✓ Nacos 服务启动成功"
    echo
    echo "=== 服务信息 ==="
    echo "Nacos 控制台: http://localhost:8848/nacos"
    echo "用户名: nacos"
    echo "密码: nacos"
    echo
    echo "等待服务完全启动..."
    sleep 30
    
    echo "检查服务状态..."
    if curl -s http://localhost:8848/nacos/v1/console/health/readiness | grep -q "UP"; then
        echo "✓ Nacos 服务运行正常"
    else
        echo "⚠ Nacos 服务可能还在启动中，请稍后访问控制台"
    fi
    
    echo
    echo "查看日志: docker-compose logs -f nacos"
    echo "停止服务: ./stop-nacos.sh"
else
    echo "✗ Nacos 服务启动失败"
    echo "请检查 Docker 环境和配置文件"
    exit 1
fi