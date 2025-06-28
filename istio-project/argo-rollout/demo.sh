#!/bin/bash

echo "=== Argo Rollout 金丝雀发布演示 ==="
echo ""

# 检查 kubectl 和 Argo Rollouts 插件
check_prerequisites() {
    echo "检查前置条件..."
    
    if ! command -v kubectl &> /dev/null; then
        echo "错误: kubectl 未安装"
        exit 1
    fi
    
    if ! kubectl get crd rollouts.argoproj.io &> /dev/null; then
        echo "错误: Argo Rollouts 未安装"
        echo "请先安装 Argo Rollouts Controller"
        exit 1
    fi
    
    echo "✓ 前置条件检查完成"
}

# 构建镜像
build_images() {
    echo ""
    echo "=== 构建镜像 ==="
    
    echo "构建 v1 镜像..."
    chmod +x build-v1.sh
    ./build-v1.sh
    
    echo ""
    echo "构建 v2 镜像..."
    chmod +x build-v2.sh
    ./build-v2.sh
    
    echo "✓ 镜像构建完成"
}

# 部署初始版本
deploy_initial() {
    echo ""
    echo "=== 部署初始版本 (v1) ==="
    
    # 创建命名空间并开启 Istio 注入
    kubectl create namespace rollout-demo --dry-run=client -o yaml | kubectl apply -f -
    kubectl label namespace rollout-demo istio-injection=enabled --overwrite
    
    # 应用配置
    kubectl apply -f order-service-service.yaml
    kubectl apply -f istio-virtualservice.yaml
    kubectl apply -f analysis-template.yaml
    kubectl apply -f order-service-rollout.yaml
    
    echo "等待 v1 版本启动..."
    kubectl wait --for=condition=available --timeout=300s rollout/order-service-rollout -n rollout-demo
    
    echo "✓ v1 版本部署完成"
}

# 开始金丝雀发布
start_canary() {
    echo ""
    echo "=== 开始金丝雀发布 (v1 -> v2) ==="
    
    # 更新镜像到 v2
    kubectl patch rollout order-service-rollout -n rollout-demo --type='merge' -p='{"spec":{"template":{"spec":{"containers":[{"name":"order-service","image":"order-service:v2","env":[{"name":"APP_VERSION","value":"v2"},{"name":"SPRING_PROFILES_ACTIVE","value":"v2"},{"name":"JAVA_OPTS","value":"-Xms128m -Xmx256m"}]}]}}}}'
    
    echo "✓ 金丝雀发布已开始"
    echo ""
    echo "监控发布进度:"
    echo "  kubectl get rollout order-service-rollout -n rollout-demo -w"
    echo ""
    echo "手动推进发布:"
    echo "  kubectl patch rollout order-service-rollout -n rollout-demo --type='merge' -p='{\"status\":{\"verifyingPreview\":true}}'"
    echo ""
    echo "回滚发布:"
    echo "  kubectl patch rollout order-service-rollout -n rollout-demo --type='merge' -p='{\"spec\":{\"template\":{\"spec\":{\"containers\":[{\"name\":\"order-service\",\"image\":\"order-service:v1\"}]}}}}'"
}

# 监控状态
watch_rollout() {
    echo ""
    echo "=== 监控发布状态 ==="
    kubectl get rollout order-service-rollout -n rollout-demo -w
}

# 测试服务
test_service() {
    echo ""
    echo "=== 测试服务 ==="
    
    # 端口转发
    echo "设置端口转发..."
    kubectl port-forward svc/order-service 8080:8080 -n rollout-demo &
    PF_PID=$!
    sleep 5
    
    echo "测试 health 接口..."
    curl -s http://localhost:8080/api/orders/health | jq .
    
    echo ""
    echo "测试 info 接口..."
    curl -s http://localhost:8080/api/orders/info | jq .
    
    # 清理端口转发
    kill $PF_PID 2>/dev/null
    
    echo "✓ 服务测试完成"
}

# 清理资源
cleanup() {
    echo ""
    echo "=== 清理资源 ==="
    
    kubectl delete rollout order-service-rollout -n rollout-demo --ignore-not-found=true
    kubectl delete service order-service order-service-canary -n rollout-demo --ignore-not-found=true
    kubectl delete virtualservice order-service-vs -n rollout-demo --ignore-not-found=true
    kubectl delete destinationrule order-service-dr -n rollout-demo --ignore-not-found=true
    kubectl delete analysistemplate success-rate -n rollout-demo --ignore-not-found=true
    
    echo "✓ 清理完成"
}

# 显示帮助
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  check      - 检查前置条件"
    echo "  build      - 构建 v1 和 v2 镜像"
    echo "  deploy     - 部署初始版本 (v1)"
    echo "  canary     - 开始金丝雀发布 (v1 -> v2)"
    echo "  watch      - 监控发布状态"
    echo "  test       - 测试服务"
    echo "  cleanup    - 清理所有资源"
    echo "  all        - 执行完整的演示流程"
    echo "  help       - 显示此帮助信息"
}

# 主函数
case "${1:-help}" in
    check)
        check_prerequisites
        ;;
    build)
        build_images
        ;;
    deploy)
        deploy_initial
        ;;
    canary)
        start_canary
        ;;
    watch)
        watch_rollout
        ;;
    test)
        test_service
        ;;
    cleanup)
        cleanup
        ;;
    all)
        check_prerequisites
        build_images
        deploy_initial
        test_service
        start_canary
        ;;
    help)
        show_help
        ;;
    *)
        echo "未知选项: $1"
        show_help
        exit 1
        ;;
esac 