#!/bin/bash

# Nacos Docker 停止脚本

echo "=== Nacos Docker 停止脚本 ==="
echo

# 检查 Docker Compose 文件是否存在
if [ ! -f "docker-compose.yml" ]; then
    echo "错误: 未找到 docker-compose.yml 文件"
    exit 1
fi

# 停止 Nacos 服务
echo "停止 Nacos 服务..."
docker-compose down

if [ $? -eq 0 ]; then
    echo "✓ Nacos 服务已停止"
else
    echo "✗ 停止服务时出现错误"
fi

echo
echo "查看容器状态: docker-compose ps"
echo "清理数据 (谨慎操作): rm -rf data logs"
echo "重新启动: ./start-nacos.sh"