#!/bin/bash

# Istio 测试数据生成脚本
echo "🎯 Istio 微服务测试数据生成器"
echo "=================================="

# 检查服务状态
echo "🔍 检查服务状态..."
kubectl get pods -n istio-demo 2>/dev/null || {
    echo "❌ istio-demo 命名空间不存在或服务未运行"
    echo "请先部署服务: kubectl apply -f k8s/"
    exit 1
}

# 启动端口转发
echo "🌐 启动服务端口转发..."
kubectl port-forward -n istio-demo svc/user-service 8080:8080 > /dev/null 2>&1 &
USER_SERVICE_PID=$!

# 等待端口转发生效
sleep 3

# 检查服务是否可访问
if ! curl -s http://localhost:8080/api/users/health > /dev/null 2>&1; then
    echo "❌ 服务无法访问，请检查端口转发"
    kill $USER_SERVICE_PID 2>/dev/null
    exit 1
fi

echo "✅ 服务已就绪，开始生成测试数据..."
echo ""

# 函数：生成基础数据
generate_basic_data() {
    echo "📊 生成基础链路追踪数据..."
    for i in {1..10}; do 
        response=$(curl -s http://localhost:8080/api/users/trace)
        echo "[$i/10] 基础请求完成"
        sleep 0.5
    done
    echo ""
}

# 函数：生成高并发数据
generate_concurrent_data() {
    echo "🚀 生成高并发测试数据..."
    for batch in {1..3}; do
        echo "发送并发批次 $batch (15个并发请求)..."
        for i in {1..15}; do
            curl -s http://localhost:8080/api/users/trace > /dev/null &
        done
        sleep 1
        wait
        echo "并发批次 $batch 完成"
    done
    echo ""
}

# 函数：生成错误数据
generate_error_data() {
    echo "❌ 生成错误场景数据..."
    
    # 404 错误
    for i in {1..5}; do
        curl -s http://localhost:8080/api/users/nonexistent > /dev/null 2>&1
        echo "[$i/5] 404错误请求"
        sleep 0.3
    done
    
    # 连接错误
    for i in {1..3}; do
        curl -s http://localhost:9999/api/test > /dev/null 2>&1
        echo "[$i/3] 连接错误请求"
        sleep 0.3
    done
    echo ""
}

# 函数：生成混合流量
generate_mixed_traffic() {
    echo "🌈 生成混合流量数据..."
    for i in {1..20}; do
        case $((i % 10)) in
            [0-6]) # 70% 正常请求
                curl -s http://localhost:8080/api/users/trace > /dev/null
                echo "[$i/20] 正常请求"
                ;;
            [7-8]) # 20% 健康检查
                curl -s http://localhost:8080/api/users/health > /dev/null
                echo "[$i/20] 健康检查"
                ;;
            9) # 10% 错误请求
                curl -s http://localhost:8080/api/users/error > /dev/null 2>&1
                echo "[$i/20] 错误请求"
                ;;
        esac
        sleep 0.3
    done
    echo ""
}

# 主执行流程
echo "开始执行测试数据生成..."
echo ""

# 询问用户要执行哪种测试
if [ "$1" = "all" ]; then
    generate_basic_data
    generate_concurrent_data
    generate_error_data
    generate_mixed_traffic
elif [ "$1" = "basic" ]; then
    generate_basic_data
elif [ "$1" = "concurrent" ]; then
    generate_concurrent_data
elif [ "$1" = "error" ]; then
    generate_error_data
elif [ "$1" = "mixed" ]; then
    generate_mixed_traffic
else
    echo "请选择要执行的测试类型："
    echo "1) 基础数据 (basic)"
    echo "2) 高并发数据 (concurrent)"
    echo "3) 错误数据 (error)"
    echo "4) 混合流量 (mixed)"
    echo "5) 全部执行 (all)"
    echo ""
    read -p "请输入选择 (1-5): " choice
    
    case $choice in
        1) generate_basic_data ;;
        2) generate_concurrent_data ;;
        3) generate_error_data ;;
        4) generate_mixed_traffic ;;
        5) 
            generate_basic_data
            generate_concurrent_data
            generate_error_data
            generate_mixed_traffic
            ;;
        *) echo "无效选择" ;;
    esac
fi

# 清理
echo "🧹 清理端口转发..."
kill $USER_SERVICE_PID 2>/dev/null

echo ""
echo "✅ 测试数据生成完成！"
echo ""
echo "📊 现在可以在以下控制台查看数据："
echo "• Kiali: http://localhost:20001 (服务拓扑图)"
echo "• Jaeger: http://localhost:16686 (链路追踪)"
echo "• Grafana: http://localhost:3000 (监控仪表板)"
echo ""
echo "💡 使用方法："
echo "  ./generate-test-data.sh all      # 执行所有测试"
echo "  ./generate-test-data.sh basic    # 只执行基础测试"
echo "  ./generate-test-data.sh mixed    # 只执行混合流量测试"
echo ""
echo "�� 现在请尝试："
echo "  ./scripts/generate-test-data.sh all  # 生成更多样化的测试数据" 