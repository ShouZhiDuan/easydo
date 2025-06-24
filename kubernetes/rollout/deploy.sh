#!/bin/bash

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印彩色信息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查kubectl是否可用
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl 未安装或不在PATH中"
        exit 1
    fi
    
    if ! kubectl cluster-info &> /dev/null; then
        print_error "无法连接到Kubernetes集群"
        exit 1
    fi
    
    print_success "Kubernetes集群连接正常"
}

# 检查Argo Rollouts是否安装
check_argo_rollouts() {
    if ! kubectl get crd rollouts.argoproj.io &> /dev/null; then
        print_warning "Argo Rollouts CRD未找到，正在安装..."
        kubectl create namespace argo-rollouts --dry-run=client -o yaml | kubectl apply -f -
        kubectl apply -n argo-rollouts -f https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml
        print_success "Argo Rollouts 安装完成"
    else
        print_success "Argo Rollouts 已安装"
    fi
}

# 部署应用
deploy_apps() {
    print_info "部署分析模板..."
    kubectl apply -f analysis-template.yaml
    kubectl apply -f gray-analysis-template.yaml
    
    print_info "部署蓝绿部署示例应用..."
    kubectl apply -f nginx-rollout.yaml
    
    print_info "部署Canary部署示例应用..."
    kubectl apply -f canary-rollout.yaml
    
    print_info "部署灰度发布示例应用..."
    kubectl apply -f gray-release.yaml
    
    print_info "部署地理位置灰度发布应用..."
    kubectl apply -f geo-gray-release.yaml
    
    print_success "所有应用部署完成"
}

# 查看状态
show_status() {
    echo
    print_info "=== Rollout 状态 ==="
    kubectl get rollouts
    
    echo
    print_info "=== Services 状态 ==="
    kubectl get svc | grep -E "(nginx|canary|gray|geo)"
    
    echo
    print_info "=== Pods 状态 ==="
    kubectl get pods -l "app in (nginx,canary-demo,gray-release,geo-gray)"
    
    echo
    print_info "=== 分析任务状态 ==="
    kubectl get analysisruns 2>/dev/null || echo "暂无分析任务"
}

# 更新应用版本
update_image() {
    local app_name=$1
    local new_image=$2
    
    if [[ -z "$app_name" || -z "$new_image" ]]; then
        print_error "使用方法: $0 update <app_name> <new_image>"
        print_info "示例: $0 update nginx-rollout nginx:1.21"
        exit 1
    fi
    
    print_info "更新 $app_name 的镜像为 $new_image..."
    kubectl argo rollouts set image $app_name $app_name=$new_image
    
    print_info "查看更新状态..."
    kubectl argo rollouts status $app_name
}

# 手动推进rollout
promote_rollout() {
    local app_name=$1
    
    if [[ -z "$app_name" ]]; then
        print_error "使用方法: $0 promote <app_name>"
        exit 1
    fi
    
    print_info "推进 $app_name 的rollout..."
    kubectl argo rollouts promote $app_name
}

# 回滚rollout
abort_rollout() {
    local app_name=$1
    
    if [[ -z "$app_name" ]]; then
        print_error "使用方法: $0 abort <app_name>"
        exit 1
    fi
    
    print_warning "中止 $app_name 的rollout..."
    kubectl argo rollouts abort $app_name
}

# 重启rollout
restart_rollout() {
    local app_name=$1
    
    if [[ -z "$app_name" ]]; then
        print_error "使用方法: $0 restart <app_name>"
        exit 1
    fi
    
    print_info "重启 $app_name 的rollout..."
    kubectl argo rollouts restart $app_name
}

# 清理资源
cleanup() {
    print_warning "清理所有资源..."
    kubectl delete -f nginx-rollout.yaml --ignore-not-found=true
    kubectl delete -f canary-rollout.yaml --ignore-not-found=true
    kubectl delete -f gray-release.yaml --ignore-not-found=true
    kubectl delete -f geo-gray-release.yaml --ignore-not-found=true
    kubectl delete -f analysis-template.yaml --ignore-not-found=true
    kubectl delete -f gray-analysis-template.yaml --ignore-not-found=true
    print_success "清理完成"
}

# 测试灰度发布
test_gray_release() {
    local app_name=${1:-"gray-release-app"}
    local user_type=${2:-"beta"}
    
    print_info "测试灰度发布应用: $app_name"
    
    # 获取Service端口
    local nodeport=$(kubectl get svc gray-public-service -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "30082")
    local geo_nodeport=$(kubectl get svc geo-gray-public-service -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "30083")
    
    echo
    print_info "=== 测试访问地址 ==="
    echo "🌐 基于用户类型的灰度发布:"
    echo "   普通用户: http://localhost:$nodeport"
    echo "   Beta用户: http://localhost:$nodeport (添加Header: X-Canary-User: beta)"
    echo "   内部用户: http://localhost:$nodeport (添加Header: X-Canary-User: internal)"
    echo
    echo "🗺️  基于地理位置的灰度发布:"
    echo "   默认访问: http://localhost:$geo_nodeport"
    echo "   华东地区: http://localhost:$geo_nodeport?region=华东"
    echo "   华南地区: http://localhost:$geo_nodeport?region=华南"
    echo
    print_info "=== 测试命令示例 ==="
    echo "# 普通请求"
    echo "curl http://localhost:$nodeport"
    echo
    echo "# Beta用户请求"
    echo "curl -H 'X-Canary-User: beta' http://localhost:$nodeport"
    echo
    echo "# 设置用户地区Cookie"
    echo "curl -H 'Cookie: user_region=华东' http://localhost:$geo_nodeport"
}

# 显示帮助信息
show_help() {
    echo "Argo Rollouts 部署管理脚本"
    echo
    echo "使用方法:"
    echo "  $0 [命令] [参数]"
    echo
    echo "命令:"
    echo "  deploy              部署所有示例应用"
    echo "  status              查看应用状态"
    echo "  update <app> <img>  更新应用镜像"
    echo "  promote <app>       推进rollout"
    echo "  abort <app>         中止rollout"
    echo "  restart <app>       重启rollout"
    echo "  test [app] [type]   测试灰度发布应用"
    echo "  cleanup             清理所有资源"
    echo "  help                显示此帮助信息"
    echo
    echo "示例:"
    echo "  $0 deploy"
    echo "  $0 update nginx-rollout nginx:1.21"
    echo "  $0 promote canary-demo"
    echo "  $0 test gray-release-app beta"
    echo "  $0 status"
    echo
    echo "灰度发布应用列表:"
    echo "  • nginx-rollout        - 蓝绿部署 (端口: 30080)"
    echo "  • canary-demo          - 金丝雀部署 (端口: 30081)"
    echo "  • gray-release-app     - 用户分组灰度发布 (端口: 30082)"
    echo "  • geo-gray-release     - 地理位置灰度发布 (端口: 30083)"
}

# 主逻辑
main() {
    case "${1:-deploy}" in
        "deploy")
            check_kubectl
            check_argo_rollouts
            deploy_apps
            sleep 3
            show_status
            ;;
        "status")
            show_status
            ;;
        "update")
            update_image "$2" "$3"
            ;;
        "promote")
            promote_rollout "$2"
            ;;
        "abort")
            abort_rollout "$2"
            ;;
        "restart")
            restart_rollout "$2"
            ;;
        "test")
            test_gray_release "$2" "$3"
            ;;
        "cleanup")
            cleanup
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            print_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@" 