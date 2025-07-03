#!/bin/bash

# 批量部署脚本
# 作者: shouzhi
# 功能: 批量在多个服务器上执行部署操作

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 服务器配置
USERNAME="root"
PASSWORD="1qazse4@Nvx2024"
CONFIG_FILE="servers.conf"

# 读取服务器配置文件
read_server_config() {
    if [ ! -f "$CONFIG_FILE" ]; then
        echo -e "${RED}错误: 配置文件 $CONFIG_FILE 不存在${NC}"
        echo -e "${YELLOW}请创建配置文件并添加服务器IP地址${NC}"
        exit 1
    fi
    
    # 读取配置文件，忽略空行和注释行
    SERVERS=()
    while IFS= read -r line; do
        # 去除前后空格
        line=$(echo "$line" | xargs)
        # 跳过空行和注释行
        if [[ -n "$line" && ! "$line" =~ ^# ]]; then
            SERVERS+=("$line")
        fi
    done < "$CONFIG_FILE"
    
    if [ ${#SERVERS[@]} -eq 0 ]; then
        echo -e "${RED}错误: 配置文件中没有找到有效的服务器IP${NC}"
        exit 1
    fi
}



# 检查依赖
check_dependencies() {
    echo -e "${BLUE}检查依赖工具...${NC}"
    
    # 检查sshpass是否安装
    if ! command -v sshpass &> /dev/null; then
        echo -e "${RED}错误: sshpass 未安装${NC}"
        echo -e "${YELLOW}请安装sshpass:${NC}"
        echo "  - Ubuntu/Debian: sudo apt-get install sshpass"
        echo "  - CentOS/RHEL: sudo yum install sshpass"
        echo "  - macOS: brew install sshpass"
        exit 1
    fi
    
    # 检查下载工具
    if ! command -v wget &> /dev/null && ! command -v curl &> /dev/null; then
        echo -e "${RED}错误: 未找到wget或curl下载工具${NC}"
        echo -e "${YELLOW}请安装wget或curl:${NC}"
        echo "  - Ubuntu/Debian: sudo apt-get install wget 或 sudo apt-get install curl"
        echo "  - CentOS/RHEL: sudo yum install wget 或 sudo yum install curl"
        echo "  - macOS: brew install wget 或 curl (通常已预装)"
        exit 1
    fi
    
    # 显示将使用的下载工具
    if command -v wget &> /dev/null; then
        echo -e "${GREEN}下载工具: wget${NC}"
    else
        echo -e "${GREEN}下载工具: curl${NC}"
    fi
    
    echo -e "${GREEN}依赖检查完成${NC}"
}

# 在单个服务器上执行命令
execute_on_server() {
    local server_ip=$1
    echo -e "${BLUE}正在连接服务器: $server_ip${NC}"
    
    # 定义要执行的命令序列 - 在远程服务器上动态选择下载工具
    local commands="
        cd ~ &&
        echo '开始下载文件...' &&
        url='http://192.168.50.207:31200/nuowei/deploy/ssh_id_rsa.tar.gz' &&
        filename='ssh_id_rsa.tar.gz' &&
        if command -v wget &> /dev/null; then
            echo '使用wget下载...' &&
            wget \$url
        elif command -v curl &> /dev/null; then
            echo '使用curl下载...' &&
            curl -L -o \$filename \$url
        else
            echo '错误: 未找到wget或curl下载工具' &&
            exit 1
        fi &&
        echo '下载完成，开始解压...' &&
        tar -zxvf ssh_id_rsa.tar.gz -C /root/baize &&
        echo '解压完成，配置SSH...' &&
        source ./baize/baize-edge-infra/ssh-agent.sh &&
        echo 'SSH配置完成，更新代码...' &&
        cd /root/baize/baize-edge-infra &&
        git pull &&
        echo '所有操作完成'
    "
    
    # 执行SSH命令
    sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 "$USERNAME@$server_ip" "$commands"
    
    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        echo -e "${GREEN}✓ 服务器 $server_ip 部署成功${NC}"
        return 0
    else
        echo -e "${RED}✗ 服务器 $server_ip 部署失败 (退出码: $exit_code)${NC}"
        return 1
    fi
}

# 测试服务器连接
test_connection() {
    local server_ip=$1
    echo -e "${YELLOW}测试连接到 $server_ip...${NC}"
    
    sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 "$USERNAME@$server_ip" "echo 'Connection test successful'" &>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ 连接测试成功${NC}"
        return 0
    else
        echo -e "${RED}✗ 连接测试失败${NC}"
        return 1
    fi
}

# 主函数
main() {
    echo -e "${BLUE}===========================================${NC}"
    echo -e "${BLUE}        批量服务器部署脚本启动${NC}"
    echo -e "${BLUE}===========================================${NC}"
    
    # 检查依赖
    check_dependencies
    
    # 读取服务器配置
    read_server_config
    
    # 显示将要操作的服务器
    echo -e "${YELLOW}将要操作的服务器列表:${NC}"
    for server in "${SERVERS[@]}"; do
        echo "  - $server"
    done
    echo
    
    # 确认操作
    read -p "确认要在以上服务器执行部署操作吗? (y/N): " confirm
    if [[ ! $confirm =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}操作已取消${NC}"
        exit 0
    fi
    
    # 记录开始时间
    start_time=$(date +%s)
    
    # 统计变量
    total_servers=${#SERVERS[@]}
    successful_servers=0
    failed_servers=0
    failed_list=()
    
    echo -e "${BLUE}开始批量部署...${NC}"
    echo
    
    # 遍历所有服务器
    for server in "${SERVERS[@]}"; do
        echo -e "${BLUE}===========================================${NC}"
        echo -e "${BLUE}处理服务器: $server${NC}"
        echo -e "${BLUE}===========================================${NC}"
        
        # 测试连接
        if test_connection "$server"; then
            # 执行部署
            if execute_on_server "$server"; then
                ((successful_servers++))
            else
                ((failed_servers++))
                failed_list+=("$server")
            fi
        else
            echo -e "${RED}跳过服务器 $server (连接失败)${NC}"
            ((failed_servers++))
            failed_list+=("$server")
        fi
        
        echo
    done
    
    # 计算执行时间
    end_time=$(date +%s)
    execution_time=$((end_time - start_time))
    
    # 显示总结
    echo -e "${BLUE}===========================================${NC}"
    echo -e "${BLUE}            部署结果总结${NC}"
    echo -e "${BLUE}===========================================${NC}"
    echo -e "总服务器数量: $total_servers"
    echo -e "${GREEN}成功: $successful_servers${NC}"
    echo -e "${RED}失败: $failed_servers${NC}"
    echo -e "执行时间: ${execution_time}秒"
    
    if [ $failed_servers -gt 0 ]; then
        echo -e "${RED}失败的服务器:${NC}"
        for failed_server in "${failed_list[@]}"; do
            echo -e "  - ${RED}$failed_server${NC}"
        done
    fi
    
    echo -e "${BLUE}===========================================${NC}"
    
    # 返回适当的退出码
    if [ $failed_servers -eq 0 ]; then
        echo -e "${GREEN}所有服务器部署完成!${NC}"
        exit 0
    else
        echo -e "${YELLOW}部分服务器部署失败，请检查日志${NC}"
        exit 1
    fi
}

# 帮助信息
show_help() {
    echo "批量服务器部署脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示此帮助信息"
    echo "  -t, --test     仅测试连接，不执行部署"
    echo ""
    echo "配置:"
    echo "  修改 $CONFIG_FILE 文件来设置目标服务器IP"
    echo "  默认用户名: $USERNAME"
    echo "  默认密码: [已配置]"
}

# 仅测试连接
test_only() {
    echo -e "${BLUE}===========================================${NC}"
    echo -e "${BLUE}        测试服务器连接${NC}"
    echo -e "${BLUE}===========================================${NC}"
    
    check_dependencies
    read_server_config
    
    for server in "${SERVERS[@]}"; do
        test_connection "$server"
    done
}

# 参数处理
case "$1" in
    -h|--help)
        show_help
        exit 0
        ;;
    -t|--test)
        test_only
        exit 0
        ;;
    "")
        main
        ;;
    *)
        echo -e "${RED}未知选项: $1${NC}"
        show_help
        exit 1
        ;;
esac 