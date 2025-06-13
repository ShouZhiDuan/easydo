#!/bin/bash

# HTTP-FTP 系统测试脚本

echo "=== HTTP-FTP 通信系统测试脚本 ==="
echo

# 检查服务是否运行
check_service() {
    local service_name=$1
    local port=$2
    local url=$3
    
    echo "检查 $service_name (端口 $port)..."
    if curl -s "$url" > /dev/null; then
        echo "✓ $service_name 运行正常"
        return 0
    else
        echo "✗ $service_name 未运行或无响应"
        return 1
    fi
}

# 执行测试请求
test_request() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    echo
    echo "--- $description ---"
    echo "请求: $method $url"
    if [ ! -z "$data" ]; then
        echo "数据: $data"
    fi
    echo
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}\nTIME:%{time_total}" "$url")
    else
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}\nTIME:%{time_total}" -X POST -H "Content-Type: application/json" -d "$data" "$url")
    fi
    
    echo "响应:"
    echo "$response"
    echo
    
    # 提取 HTTP 状态码
    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    time_total=$(echo "$response" | grep "TIME:" | cut -d: -f2)
    
    if [ "$http_code" = "200" ]; then
        echo "✓ 测试成功 (HTTP $http_code, 耗时: ${time_total}s)"
    else
        echo "✗ 测试失败 (HTTP $http_code)"
    fi
    
    echo "================================================"
}

# 主测试流程
echo "1. 检查服务状态"
echo

service_a_ok=false
service_b_ok=false

if check_service "Service A" "8080" "http://localhost:8080/api/health"; then
    service_a_ok=true
fi

if check_service "Service B" "8081" "http://localhost:8081/api/health"; then
    service_b_ok=true
fi

echo

if [ "$service_a_ok" = false ] || [ "$service_b_ok" = false ]; then
    echo "⚠ 部分服务未运行，请先启动服务:"
    echo "   ./start-services.sh"
    echo
    echo "是否继续测试? (y/N)"
    read -r continue_test
    if [ "$continue_test" != "y" ] && [ "$continue_test" != "Y" ]; then
        echo "测试已取消"
        exit 1
    fi
fi

echo "2. 开始功能测试"
echo

# 测试健康检查
test_request "GET" "http://localhost:8080/api/health" "" "Service A 健康检查"
test_request "GET" "http://localhost:8081/api/health" "" "Service B 健康检查"

# 测试 Service B 状态
test_request "GET" "http://localhost:8081/api/status" "" "Service B 状态查询"
test_request "GET" "http://localhost:8081/api/listener/status" "" "Service B 监听器状态"

# 测试 GET 请求
test_request "GET" "http://localhost:8080/api/request?test=value&param=123" "" "GET 请求测试"

# 测试 POST 请求
test_request "POST" "http://localhost:8080/api/request" '{"name":"test","value":123,"array":[1,2,3]}' "POST 请求测试"

# 测试复杂 POST 请求
test_request "POST" "http://localhost:8080/api/request" '{"user":{"id":1,"name":"张三","email":"zhangsan@example.com"},"action":"create","timestamp":"2024-01-01T10:00:00Z"}' "复杂 POST 请求测试"

echo
echo "=== 测试完成 ==="
echo
echo "注意事项:"
echo "1. 如果测试失败，请检查:"
echo "   - FTP 服务器是否可访问"
echo "   - 网络连接是否正常"
echo "   - 服务日志是否有错误信息"
echo
echo "2. 查看详细日志:"
echo "   - Service A: logs/service-a.log"
echo "   - Service B: logs/service-b.log"
echo
echo "3. 如需重新测试，请运行: ./test-system.sh"
echo "4. 停止服务: ./stop-services.sh"