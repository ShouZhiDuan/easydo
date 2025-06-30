#!/bin/bash

# Docker 监控脚本
# 功能：监控Docker的镜像、容器、卷、网络、构建缓存等资源使用情况
# 作者：AI Assistant
# 版本：1.0

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

# 配置参数
SCRIPT_NAME=$(basename "$0")
LOG_FILE="/tmp/docker_monitor.log"
ALERT_THRESHOLD_GB=50  # 磁盘使用超过50GB时告警
CONTAINER_LIMIT=10     # 显示容器数量限制
IMAGE_LIMIT=15         # 显示镜像数量限制

# 帮助信息
show_help() {
    cat << EOF
${WHITE}Docker 监控脚本${NC}

用法: $SCRIPT_NAME [选项]

选项:
    -h, --help          显示帮助信息
    -a, --all           显示所有详细信息
    -s, --summary       只显示摘要信息
    -i, --images        显示镜像详情
    -c, --containers    显示容器详情
    -v, --volumes       显示卷详情
    -n, --networks      显示网络详情
    -b, --build-cache   显示构建缓存详情
    -r, --reclaimable   显示详细可回收资源分析
    -l, --log           记录到日志文件
    -w, --watch         持续监控模式（每5秒刷新）
    --clean-unused      清理未使用的资源
    --export-json       导出JSON格式数据

示例:
    $SCRIPT_NAME -a                # 显示所有信息
    $SCRIPT_NAME -s                # 只显示摘要
    $SCRIPT_NAME -r                # 显示可回收资源详情
    $SCRIPT_NAME -w                # 持续监控模式
    $SCRIPT_NAME --clean-unused    # 清理未使用资源

EOF
}

# 日志记录函数
log_message() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[$timestamp] [$level] $message" >> "$LOG_FILE"
}

# 格式化字节大小
format_bytes() {
    local bytes=$1
    if [[ $bytes =~ ^[0-9]+$ ]]; then
        if [ $bytes -lt 1024 ]; then
            echo "${bytes}B"
        elif [ $bytes -lt 1048576 ]; then
            echo "$(( bytes / 1024 ))KB"
        elif [ $bytes -lt 1073741824 ]; then
            echo "$(( bytes / 1048576 ))MB"
        else
            echo "$(( bytes / 1073741824 ))GB"
        fi
    else
        echo "$bytes"
    fi
}

# 获取Docker系统信息
get_docker_system_info() {
    echo -e "${WHITE}📊 Docker 系统概览${NC}"
    echo "=================================="
    
    # 检查Docker是否运行
    if ! docker info >/dev/null 2>&1; then
        echo -e "${RED}❌ Docker 未运行或无法访问${NC}"
        return 1
    fi
    
    # 获取系统信息
    local docker_version=$(docker version --format '{{.Server.Version}}' 2>/dev/null)
    local docker_root=$(docker info --format '{{.DockerRootDir}}' 2>/dev/null)
    local total_containers=$(docker ps -aq | wc -l)
    local running_containers=$(docker ps -q | wc -l)
    local total_images=$(docker images -q | wc -l)
    local total_volumes=$(docker volume ls -q | wc -l)
    local total_networks=$(docker network ls -q | wc -l)
    
    echo -e "${CYAN}Docker 版本:${NC} $docker_version"
    echo -e "${CYAN}存储路径:${NC} $docker_root"
    echo -e "${CYAN}容器总数:${NC} $total_containers (运行中: ${GREEN}$running_containers${NC})"
    echo -e "${CYAN}镜像总数:${NC} $total_images"
    echo -e "${CYAN}卷总数:${NC} $total_volumes"
    echo -e "${CYAN}网络总数:${NC} $total_networks"
    echo ""
}

# 获取磁盘使用情况
get_disk_usage() {
    echo -e "${WHITE}💾 磁盘使用情况${NC}"
    echo "=================================="
    
    # 使用更简单的方式获取docker system df信息
    echo -e "${CYAN}Docker 磁盘使用统计:${NC}"
    docker system df
    echo ""
    
    # 获取详细的磁盘使用信息
    echo -e "${CYAN}详细磁盘使用信息:${NC}"
    
    # 镜像占用
    local images_count=$(docker images -q | wc -l)
    local images_size=$(docker system df | grep Images | awk '{print $4}')
    echo -e "${BLUE}镜像:${NC} $images_count 个，总大小约: ${GREEN}${images_size}${NC}"
    
    # 容器占用
    local containers_count=$(docker ps -aq | wc -l)
    local running_containers=$(docker ps -q | wc -l)
    local containers_size=$(docker system df | grep Containers | awk '{print $4}')
    echo -e "${BLUE}容器:${NC} $containers_count 个 (运行中: $running_containers)，总大小约: ${GREEN}${containers_size}${NC}"
    
    # 卷占用
    local volumes_count=$(docker volume ls -q 2>/dev/null | wc -l)
    local volumes_size=$(docker system df | grep "Local Volumes" | awk '{print $4}')
    [[ "$volumes_size" == "0" ]] && volumes_size="0B"
    echo -e "${BLUE}卷:${NC} $volumes_count 个，总大小约: ${GREEN}${volumes_size}${NC}"
    
    # 构建缓存
    local cache_size=$(docker system df | grep "Build Cache" | awk '{print $4}')
    [[ "$cache_size" == "0" ]] && cache_size="0B"
    echo -e "${BLUE}构建缓存:${NC} 总大小约: ${GREEN}${cache_size}${NC}"
    
    # 总计使用量
    local total_size=$(docker system df | awk 'NR>1 {gsub(/[KMGT]B/, ""); total+=$3} END {
        if(total < 1024) print total "MB"
        else if(total < 1048576) print total/1024 "GB"
        else print total/1048576 "TB"
    }')
    echo ""
    echo -e "${YELLOW}📊 总计磁盘使用: ${WHITE}${total_size}${NC}"
    
    # 可回收空间统计
    local reclaimable_images=$(docker system df | grep Images | awk '{print $5}' | sed 's/[()]//g')
    local reclaimable_containers=$(docker system df | grep Containers | awk '{print $5}' | sed 's/[()]//g')
    local reclaimable_volumes=$(docker system df | grep "Local Volumes" | awk '{print $5}' | sed 's/[()]//g')
    
    echo -e "${CYAN}🗑️  可回收空间:${NC}"
    echo -e "   镜像: ${YELLOW}${reclaimable_images}${NC}"
    echo -e "   容器: ${YELLOW}${reclaimable_containers}${NC}"
    echo -e "   卷: ${YELLOW}${reclaimable_volumes}${NC}"
    
    echo ""
}

# 获取详细的可回收资源信息
get_reclaimable_details() {
    echo -e "${WHITE}🗑️  详细可回收资源分析${NC}"
    echo "=================================="
    
    # 1. 可回收镜像详情
    echo -e "${CYAN}📦 可回收镜像:${NC}"
    echo "--------------------------------"
    
    # 悬空镜像
    local dangling_images=$(docker images -f "dangling=true" -q)
    local dangling_count=$(echo "$dangling_images" | grep -v '^$' | wc -l)
    
    if [ "$dangling_count" -gt 0 ]; then
        echo -e "${YELLOW}🏷️  悬空镜像 ($dangling_count 个):${NC}"
        docker images -f "dangling=true" --format "table {{.ID}}\t{{.CreatedSince}}\t{{.Size}}" | head -10
        echo -e "${BLUE}清理命令:${NC} ${GREEN}docker image prune -f${NC}"
        echo ""
    fi
    
    # 未使用的镜像
    echo -e "${YELLOW}📋 未被容器使用的镜像:${NC}"
    local unused_images=$(docker images --format "{{.ID}}" | while read image; do
        if [ -z "$(docker ps -a --filter ancestor=$image -q)" ]; then
            echo $image
        fi
    done)
    
    local unused_count=$(echo "$unused_images" | grep -v '^$' | wc -l)
    if [ "$unused_count" -gt 0 ]; then
        echo "发现 $unused_count 个未使用的镜像:"
        echo "$unused_images" | head -5 | while read img; do
            if [ ! -z "$img" ]; then
                docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedSince}}" | grep $img | head -1
            fi
        done
        echo -e "${BLUE}清理命令:${NC} ${GREEN}docker image prune -a -f${NC}"
    else
        echo "✅ 所有镜像都在使用中"
    fi
    echo ""
    
    # 2. 可回收容器详情
    echo -e "${CYAN}📦 可回收容器:${NC}"
    echo "--------------------------------"
    
    # 停止的容器
    local exited_containers=$(docker ps -a --filter "status=exited" -q)
    local exited_count=$(echo "$exited_containers" | grep -v '^$' | wc -l)
    
    if [ "$exited_count" -gt 0 ]; then
        echo -e "${YELLOW}🛑 已停止的容器 ($exited_count 个):${NC}"
        docker ps -a --filter "status=exited" --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Size}}" | head -8
        echo -e "${BLUE}清理命令:${NC} ${GREEN}docker container prune -f${NC}"
        echo ""
        
        # 显示具体的容器清理命令
        echo -e "${BLUE}逐个删除命令:${NC}"
        docker ps -a --filter "status=exited" --format "docker rm {{.Names}}" | head -5
        echo ""
    fi
    
    # 死掉的容器
    local dead_containers=$(docker ps -a --filter "status=dead" -q)
    local dead_count=$(echo "$dead_containers" | grep -v '^$' | wc -l)
    
    if [ "$dead_count" -gt 0 ]; then
        echo -e "${RED}💀 死掉的容器 ($dead_count 个):${NC}"
        docker ps -a --filter "status=dead" --format "table {{.Names}}\t{{.Image}}\t{{.Status}}"
        echo -e "${BLUE}清理命令:${NC} ${GREEN}docker ps -a --filter status=dead -q | xargs docker rm${NC}"
        echo ""
    fi
    
    if [ "$exited_count" -eq 0 ] && [ "$dead_count" -eq 0 ]; then
        echo "✅ 没有可回收的容器"
        echo ""
    fi
    
    # 3. 可回收卷详情
    echo -e "${CYAN}💿 可回收卷:${NC}"
    echo "--------------------------------"
    
    local dangling_volumes=$(docker volume ls -f dangling=true -q)
    local volume_count=$(echo "$dangling_volumes" | grep -v '^$' | wc -l)
    
    if [ "$volume_count" -gt 0 ]; then
        echo -e "${YELLOW}🗂️  未使用的卷 ($volume_count 个):${NC}"
        docker volume ls -f dangling=true --format "table {{.Driver}}\t{{.Name}}"
        echo -e "${BLUE}清理命令:${NC} ${GREEN}docker volume prune -f${NC}"
        echo ""
        
        # 显示卷的详细信息和大小
        echo -e "${BLUE}卷详细信息:${NC}"
        echo "$dangling_volumes" | head -3 | while read vol; do
            if [ ! -z "$vol" ]; then
                echo "卷名: $vol"
                docker volume inspect $vol --format "  路径: {{.Mountpoint}}" 2>/dev/null
                echo "  删除: docker volume rm $vol"
                echo ""
            fi
        done
    else
        echo "✅ 没有未使用的卷"
        echo ""
    fi
    
    # 4. 可回收网络详情
    echo -e "${CYAN}🌐 可回收网络:${NC}"
    echo "--------------------------------"
    
    local unused_networks=$(docker network ls --filter "dangling=true" -q)
    local network_count=$(echo "$unused_networks" | grep -v '^$' | wc -l)
    
    if [ "$network_count" -gt 0 ]; then
        echo -e "${YELLOW}🔗 未使用的网络 ($network_count 个):${NC}"
        docker network ls --filter "dangling=true" --format "table {{.Name}}\t{{.Driver}}\t{{.Scope}}"
        echo -e "${BLUE}清理命令:${NC} ${GREEN}docker network prune -f${NC}"
        echo ""
    else
        echo "✅ 没有未使用的网络"
        echo ""
    fi
    
    # 5. 构建缓存详情
    echo -e "${CYAN}🔧 构建缓存:${NC}"
    echo "--------------------------------"
    
    if docker buildx version >/dev/null 2>&1; then
        local cache_info=$(docker buildx du 2>/dev/null)
        if [ ! -z "$cache_info" ] && [ "$cache_info" != "No build cache" ]; then
            echo -e "${YELLOW}🏗️  构建缓存详情:${NC}"
            echo "$cache_info"
            echo -e "${BLUE}清理命令:${NC} ${GREEN}docker buildx prune -f${NC}"
            echo ""
        else
            echo "✅ 没有构建缓存"
            echo ""
        fi
    else
        echo "ℹ️  不支持 buildx，无法检查构建缓存"
        echo ""
    fi
    
    # 6. 生成清理脚本
    echo -e "${WHITE}📝 生成清理脚本${NC}"
    echo "=================================="
    
    # 计算总的可回收空间
    echo -e "${CYAN}💾 可回收空间统计:${NC}"
    local total_reclaimable_images=$(docker system df | grep Images | awk '{print $5}' | sed 's/[()]//g')
    local total_reclaimable_containers=$(docker system df | grep Containers | awk '{print $5}' | sed 's/[()]//g')
    local total_reclaimable_volumes=$(docker system df | grep "Local Volumes" | awk '{print $5}' | sed 's/[()]//g')
    
    echo -e "  镜像可回收: ${YELLOW}${total_reclaimable_images}${NC}"
    echo -e "  容器可回收: ${YELLOW}${total_reclaimable_containers}${NC}"
    echo -e "  卷可回收: ${YELLOW}${total_reclaimable_volumes}${NC}"
    
    # 计算总的可回收百分比
    local total_used=$(docker system df | awk 'NR>1 {total+=$3} END {print total}')
    local total_reclaimable=$(docker system df | awk 'NR>1 {gsub(/[KMGT]B/, "", $5); gsub(/[()]/, "", $5); gsub(/%/, "", $5); total+=$5} END {print total}')
    
    if [ ! -z "$total_reclaimable" ] && [ "$total_reclaimable" != "0" ]; then
        echo -e "  总可回收比例: ${YELLOW}约 ${total_reclaimable}%${NC}"
    fi
    echo ""
    
    local cleanup_script="docker_cleanup_$(date +%Y%m%d_%H%M%S).sh"
    
    cat > "$cleanup_script" << 'CLEANUP_EOF'
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

CLEANUP_EOF
    
    chmod +x "$cleanup_script"
    
    echo -e "${GREEN}✅ 已生成清理脚本: $cleanup_script${NC}"
    echo -e "${BLUE}执行方式:${NC}"
    echo "  ./$cleanup_script                    # 执行所有清理"
    echo "  docker container prune -f           # 只清理容器"
    echo "  docker image prune -f               # 只清理悬空镜像"
    echo "  docker image prune -a -f            # 清理所有未使用镜像"
    echo "  docker system prune -a -f           # 清理所有资源"
    echo ""
    
    # 7. 清理风险提示
    echo -e "${YELLOW}⚠️  清理注意事项:${NC}"
    echo "• 清理前请确保重要数据已备份"
    echo "• 某些镜像删除后需要重新下载"
    echo "• 在生产环境中请谨慎操作"
    echo "• 建议先运行 'docker system df' 查看当前状态"
    echo ""
    
    # 8. 智能清理建议
    echo -e "${WHITE}🎯 智能清理建议${NC}"
    echo "=================================="
    
    # 根据不同情况给出建议
    local total_size_gb=$(docker system df | awk 'NR>1 {gsub(/[KMGT]B/, ""); total+=$3} END {print total/1024}')
    
    if (( $(echo "$total_size_gb > 20" | bc -l) )); then
        echo -e "${RED}🚨 磁盘使用较高 (${total_size_gb}GB+)${NC}"
        echo "建议立即清理："
        echo "  1. 优先清理构建缓存: docker buildx prune -f"
        echo "  2. 清理停止的容器: docker container prune -f"
        echo "  3. 清理悬空镜像: docker image prune -f"
    elif (( $(echo "$total_size_gb > 10" | bc -l) )); then
        echo -e "${YELLOW}⚠️  磁盘使用中等 (${total_size_gb}GB)${NC}"
        echo "建议定期清理："
        echo "  1. 清理停止的容器: docker container prune -f"
        echo "  2. 清理悬空镜像: docker image prune -f"
    else
        echo -e "${GREEN}✅ 磁盘使用正常 (${total_size_gb}GB)${NC}"
        echo "保持良好习惯："
        echo "  1. 定期运行: docker system prune -f"
        echo "  2. 监控镜像增长情况"
    fi
    
    echo ""
    echo -e "${BLUE}📋 常用清理命令速查:${NC}"
    echo "  docker system prune -f              # 快速清理（推荐）"
    echo "  docker system prune -a -f           # 深度清理（慎用）"
    echo "  docker container prune -f           # 只清理容器"
    echo "  docker image prune -f               # 只清理悬空镜像"
    echo "  docker volume prune -f              # 只清理卷"
    echo "  docker builder prune -f             # 只清理构建缓存"
    echo ""
}

# 获取镜像详情
get_images_detail() {
    echo -e "${WHITE}🖼️  镜像详情 (前$IMAGE_LIMIT个)${NC}"
    echo "=================================="
    
    docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedSince}}" | head -n $((IMAGE_LIMIT + 1))
    echo ""
    
    # 显示悬空镜像
    local dangling_images=$(docker images -f "dangling=true" -q | wc -l)
    if [ "$dangling_images" -gt 0 ]; then
        echo -e "${YELLOW}⚠️  发现 $dangling_images 个悬空镜像${NC}"
        echo ""
    fi
}

# 获取容器详情
get_containers_detail() {
    echo -e "${WHITE}📦 容器详情 (前$CONTAINER_LIMIT个)${NC}"
    echo "=================================="
    
    # 运行中的容器
    echo -e "${GREEN}🟢 运行中的容器:${NC}"
    docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}" | head -n $((CONTAINER_LIMIT / 2 + 1))
    echo ""
    
    # 停止的容器
    echo -e "${RED}🔴 停止的容器:${NC}"
    docker ps -a --filter "status=exited" --format "table {{.Names}}\t{{.Image}}\t{{.Status}}" | head -n $((CONTAINER_LIMIT / 2 + 1))
    echo ""
}

# 获取卷详情
get_volumes_detail() {
    echo -e "${WHITE}💿 卷详情${NC}"
    echo "=================================="
    
    docker volume ls --format "table {{.Driver}}\t{{.Name}}" | head -n 16
    echo ""
    
    # 检查未使用的卷
    local unused_volumes=$(docker volume ls -f dangling=true -q | wc -l)
    if [ "$unused_volumes" -gt 0 ]; then
        echo -e "${YELLOW}⚠️  发现 $unused_volumes 个未使用的卷${NC}"
        echo ""
    fi
}

# 获取网络详情
get_networks_detail() {
    echo -e "${WHITE}🌐 网络详情${NC}"
    echo "=================================="
    
    docker network ls --format "table {{.Name}}\t{{.Driver}}\t{{.Scope}}"
    echo ""
}

# 获取构建缓存详情
get_build_cache_detail() {
    echo -e "${WHITE}🔧 构建缓存详情${NC}"
    echo "=================================="
    
    # 检查是否支持buildx
    if docker buildx version >/dev/null 2>&1; then
        docker buildx du 2>/dev/null || echo "无构建缓存数据"
    else
        echo "不支持 buildx，无法显示构建缓存详情"
    fi
    echo ""
}

# 获取资源使用统计
get_resource_stats() {
    echo -e "${WHITE}📈 资源使用统计${NC}"
    echo "=================================="
    
    # 获取运行中容器的资源使用情况
    if [ "$(docker ps -q | wc -l)" -gt 0 ]; then
        echo -e "${CYAN}容器资源使用情况:${NC}"
        docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}" | head -n 11
    else
        echo "没有运行中的容器"
    fi
    echo ""
}

# 清理未使用的资源
clean_unused_resources() {
    echo -e "${WHITE}🧹 清理未使用的资源${NC}"
    echo "=================================="
    
    read -p "确定要清理未使用的资源吗？(y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}正在清理未使用的容器...${NC}"
        docker container prune -f
        
        echo -e "${YELLOW}正在清理未使用的镜像...${NC}"
        docker image prune -f
        
        echo -e "${YELLOW}正在清理未使用的卷...${NC}"
        docker volume prune -f
        
        echo -e "${YELLOW}正在清理未使用的网络...${NC}"
        docker network prune -f
        
        echo -e "${GREEN}✅ 清理完成${NC}"
    else
        echo "取消清理操作"
    fi
    echo ""
}

# 导出JSON格式数据
export_json_data() {
    local output_file="docker_monitor_$(date +%Y%m%d_%H%M%S).json"
    
    echo -e "${WHITE}📄 导出JSON数据${NC}"
    echo "=================================="
    
    cat > "$output_file" << EOF
{
    "timestamp": "$(date -Iseconds)",
    "docker_info": $(docker info --format '{{json .}}'),
    "system_df": $(docker system df --format '{{json .}}'),
    "images": $(docker images --format '{{json .}}' | jq -s '.'),
    "containers": $(docker ps -a --format '{{json .}}' | jq -s '.'),
    "volumes": $(docker volume ls --format '{{json .}}' | jq -s '.'),
    "networks": $(docker network ls --format '{{json .}}' | jq -s '.')
}
EOF
    
    echo -e "${GREEN}✅ 数据已导出到: $output_file${NC}"
    echo ""
}

# 持续监控模式
watch_mode() {
    echo -e "${WHITE}👀 持续监控模式 (按 Ctrl+C 退出)${NC}"
    echo "=================================="
    
    while true; do
        clear
        echo -e "${PURPLE}🔄 Docker 监控 - $(date)${NC}"
        echo ""
        get_docker_system_info
        get_disk_usage
        get_resource_stats
        echo -e "${CYAN}下次刷新: 5秒后...${NC}"
        sleep 5
    done
}

# 显示摘要信息
show_summary() {
    get_docker_system_info
    get_disk_usage
}

# 显示所有信息
show_all() {
    get_docker_system_info
    get_disk_usage
    get_images_detail
    get_containers_detail
    get_volumes_detail
    get_networks_detail
    get_build_cache_detail
    get_resource_stats
    get_reclaimable_details
}

# 主函数
main() {
    # 检查依赖
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker 未安装${NC}"
        exit 1
    fi
    
    # 解析命令行参数
    case "${1:-}" in
        -h|--help)
            show_help
            ;;
        -a|--all)
            show_all
            ;;
        -s|--summary)
            show_summary
            ;;
        -i|--images)
            get_images_detail
            ;;
        -c|--containers)
            get_containers_detail
            ;;
        -v|--volumes)
            get_volumes_detail
            ;;
        -n|--networks)
            get_networks_detail
            ;;
        -b|--build-cache)
            get_build_cache_detail
            ;;
        -r|--reclaimable)
            get_reclaimable_details
            ;;
        -w|--watch)
            watch_mode
            ;;
        --clean-unused)
            clean_unused_resources
            ;;
        --export-json)
            export_json_data
            ;;
        -l|--log)
            show_all | tee -a "$LOG_FILE"
            echo -e "${GREEN}✅ 日志已保存到: $LOG_FILE${NC}"
            ;;
        "")
            show_summary
            ;;
        *)
            echo -e "${RED}❌ 未知选项: $1${NC}"
            echo "使用 $SCRIPT_NAME --help 查看帮助信息"
            exit 1
            ;;
    esac
}

# 信号处理
trap 'echo -e "\n${YELLOW}👋 监控已停止${NC}"; exit 0' INT TERM

# 执行主函数
main "$@" 