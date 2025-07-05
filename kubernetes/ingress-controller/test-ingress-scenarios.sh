#!/bin/bash

# Ingress 测试场景脚本
# 使用方法: ./test-ingress-scenarios.sh

set -e

echo "=== Ingress Controller 测试场景 ==="
echo "开始时间: $(date)"

# 获取 Ingress Controller 的外部 IP
INGRESS_IP=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
if [ -z "$INGRESS_IP" ]; then
    INGRESS_IP="localhost"
fi

INGRESS_PORT=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.spec.ports[0].port}')
if [ -z "$INGRESS_PORT" ]; then
    INGRESS_PORT="80"
fi

echo "Ingress Controller 地址: $INGRESS_IP:$INGRESS_PORT"

# 函数：测试 HTTP 请求
test_http() {
    local url="$1"
    local expected_code="$2"
    local description="$3"
    local extra_args="$4"
    
    echo "测试: $description"
    echo "URL: $url"
    
    if curl -s -o /dev/null -w "%{http_code}" $extra_args "$url" | grep -q "$expected_code"; then
        echo "✅ 测试通过 (HTTP $expected_code)"
    else
        echo "❌ 测试失败"
        echo "实际响应:"
        curl -s -w "HTTP状态码: %{http_code}\n" $extra_args "$url" | head -10
    fi
    echo "---"
}

# 函数：等待 Pod 就绪
wait_for_pods() {
    echo "等待应用 Pod 就绪..."
    kubectl wait --for=condition=ready pod -l app=myapp --timeout=120s
    echo "✅ 应用 Pod 已就绪"
}

# 函数：部署基础 Ingress 规则
deploy_basic_ingress() {
    echo "部署基础 Ingress 规则..."
    kubectl apply -f my-ingress.yaml
    kubectl apply -f my-ingress-no-host.yaml
    kubectl apply -f my-ingress-wildcard.yaml
    kubectl apply -f my-ingress-ip.yaml
    
    echo "等待 Ingress 规则生效..."
    sleep 10
    echo "✅ 基础 Ingress 规则已部署"
}

# 函数：部署高级 Ingress 场景
deploy_advanced_ingress() {
    echo "部署高级 Ingress 场景..."
    kubectl apply -f ingress-test-scenarios.yaml
    
    echo "等待高级 Ingress 规则生效..."
    sleep 15
    echo "✅ 高级 Ingress 规则已部署"
}

# 主测试函数
main() {
    echo "1. 检查 Ingress Controller 状态"
    kubectl get pods -n ingress-nginx
    kubectl get svc -n ingress-nginx
    
    echo -e "\n2. 等待应用就绪"
    wait_for_pods
    
    echo -e "\n3. 部署基础 Ingress 规则"
    deploy_basic_ingress
    
    echo -e "\n4. 开始基础场景测试"
    
    # 测试场景1: 基于域名的访问
    test_http "http://$INGRESS_IP:$INGRESS_PORT/" "200" "无Host头访问" "-H 'Host: '"
    
    # 测试场景2: 基于域名的访问
    test_http "http://$INGRESS_IP:$INGRESS_PORT/" "200" "指定Host头访问" "-H 'Host: myapp.example.com'"
    
    # 测试场景3: 通配符域名访问
    test_http "http://$INGRESS_IP:$INGRESS_PORT/" "200" "通配符域名访问 (.local)" "-H 'Host: test.local'"
    
    # 测试场景4: 通配符域名访问
    test_http "http://$INGRESS_IP:$INGRESS_PORT/" "200" "通配符域名访问 (.example.com)" "-H 'Host: api.example.com'"
    
    echo -e "\n5. 部署高级 Ingress 场景"
    deploy_advanced_ingress
    
    echo -e "\n6. 开始高级场景测试"
    
    # 测试场景5: 基于路径的路由
    test_http "http://$INGRESS_IP:$INGRESS_PORT/api/users" "200" "API路径路由" "-H 'Host: myapp.local'"
    test_http "http://$INGRESS_IP:$INGRESS_PORT/web/dashboard" "200" "Web路径路由" "-H 'Host: myapp.local'"
    
    # 测试场景6: 速率限制
    echo "测试速率限制 (连续发送15个请求):"
    for i in {1..15}; do
        code=$(curl -s -o /dev/null -w "%{http_code}" -H "Host: ratelimit.example.com" "http://$INGRESS_IP:$INGRESS_PORT/")
        echo "请求 $i: HTTP $code"
        if [ "$code" = "429" ]; then
            echo "✅ 速率限制生效 (HTTP 429)"
            break
        fi
        sleep 0.1
    done
    
    # 测试场景7: 基本认证
    test_http "http://$INGRESS_IP:$INGRESS_PORT/" "401" "基本认证 (无凭据)" "-H 'Host: auth.example.com'"
    test_http "http://$INGRESS_IP:$INGRESS_PORT/" "200" "基本认证 (有效凭据)" "-H 'Host: auth.example.com' -u admin:admin"
    
    # 测试场景8: CORS 预检请求
    test_http "http://$INGRESS_IP:$INGRESS_PORT/" "200" "CORS预检请求" "-H 'Host: cors.example.com' -H 'Origin: https://myapp.com' -X OPTIONS"
    
    echo -e "\n7. 查看 Ingress 状态"
    kubectl get ingress
    
    echo -e "\n8. 查看 Ingress 详细信息"
    kubectl describe ingress my-ingress
    
    echo -e "\n=== 测试完成 ==="
    echo "结束时间: $(date)"
}

# 清理函数
cleanup() {
    echo "清理测试资源..."
    kubectl delete -f ingress-test-scenarios.yaml --ignore-not-found=true
    kubectl delete -f my-ingress.yaml --ignore-not-found=true
    kubectl delete -f my-ingress-no-host.yaml --ignore-not-found=true
    kubectl delete -f my-ingress-wildcard.yaml --ignore-not-found=true
    kubectl delete -f my-ingress-ip.yaml --ignore-not-found=true
    echo "✅ 清理完成"
}

# 帮助函数
show_help() {
    echo "使用方法: $0 [选项]"
    echo "选项:"
    echo "  test     - 运行所有测试场景"
    echo "  cleanup  - 清理测试资源"
    echo "  help     - 显示此帮助信息"
}

# 主逻辑
case "${1:-test}" in
    test)
        main
        ;;
    cleanup)
        cleanup
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