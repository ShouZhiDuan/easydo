#!/bin/bash

# Docker 快速检查脚本
# 简化版本，用于快速查看Docker资源使用情况

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'

echo -e "${WHITE}🐳 Docker 快速检查${NC}"
echo "===================="

# 检查Docker状态
if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}❌ Docker 未运行${NC}"
    exit 1
fi

# 基本信息
echo -e "${CYAN}📊 基本信息:${NC}"
echo "Docker版本: $(docker version --format '{{.Server.Version}}' 2>/dev/null)"
echo "容器: $(docker ps -q | wc -l) 运行中 / $(docker ps -aq | wc -l) 总计"
echo "镜像: $(docker images -q | wc -l) 个"
echo ""

# 磁盘使用
echo -e "${CYAN}💾 磁盘使用:${NC}"
docker system df
echo ""

# 运行中的容器（如果有的话）
running_containers=$(docker ps -q | wc -l)
if [ "$running_containers" -gt 0 ]; then
    echo -e "${CYAN}🏃 运行中的容器:${NC}"
    docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}" | head -6
    echo ""
fi

# 最大的镜像
echo -e "${CYAN}📦 最大的镜像 (前5个):${NC}"
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | head -6
echo ""

# 磁盘使用警告
total_size_line=$(docker system df | grep Images | awk '{print $3}')
total_size_num=$(echo "$total_size_line" | grep -o '[0-9.]*' | head -1)
total_size_unit=$(echo "$total_size_line" | grep -o '[KMGT]*B' | head -1)

if [ ! -z "$total_size_num" ]; then
    # 转换为GB进行比较
    if [[ "$total_size_unit" == "GB" && $(echo "$total_size_num > 5" | bc -l 2>/dev/null || echo 0) -eq 1 ]] || \
       [[ "$total_size_unit" == "TB" ]]; then
        echo -e "${YELLOW}⚠️  Docker 磁盘使用较大 (${total_size_line})，建议清理${NC}"
        echo "运行以下命令清理未使用资源:"
        echo "  docker system prune        # 清理基本资源"
        echo "  docker system prune -a     # 清理所有未使用资源"
        echo "  ./docker_monitor.sh --clean-unused  # 使用监控脚本清理"
    fi
fi

echo -e "${GREEN}✅ 检查完成${NC}" 