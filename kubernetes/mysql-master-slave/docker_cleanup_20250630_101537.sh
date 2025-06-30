#!/bin/bash

# Docker 资源清理脚本
# 自动生成时间: $(date)

echo "🧹 开始清理 Docker 资源..."

# 1. 清理停止的容器
echo "1️⃣ 清理停止的容器..."
docker container prune -f

# 2. 清理悬空镜像
echo "2️⃣ 清理悬空镜像..."
docker image prune -f

# 3. 清理未使用的卷
echo "3️⃣ 清理未使用的卷..."
docker volume prune -f

# 4. 清理未使用的网络
echo "4️⃣ 清理未使用的网络..."
docker network prune -f

# 5. 清理构建缓存
echo "5️⃣ 清理构建缓存..."
if docker buildx version >/dev/null 2>&1; then
    docker buildx prune -f
fi

echo "✅ 清理完成！"

# 显示清理后的状态
echo ""
echo "📊 清理后的磁盘使用情况:"
docker system df

