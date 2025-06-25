#!/bin/bash

set -e

# 获取Ingress Gateway地址
INGRESS_HOST=$(kubectl get po -l istio=ingressgateway -n istio-system -o jsonpath='{.items[0].status.hostIP}')
INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')
BASE_URL="http://$INGRESS_HOST:$INGRESS_PORT"

echo "Testing Istio Demo Services..."
echo "Base URL: $BASE_URL"

# 测试用户服务
echo -e "\n=== Testing User Service ==="
echo "1. Health check:"
curl -s "$BASE_URL/api/users/health" | jq .

echo -e "\n2. Get all users:"
curl -s "$BASE_URL/api/users" | jq .

echo -e "\n3. Get user by ID:"
curl -s "$BASE_URL/api/users/1" | jq .

echo -e "\n4. Create new user:"
curl -s -X POST "$BASE_URL/api/users" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","fullName":"Test User"}' | jq .

# 测试订单服务
echo -e "\n=== Testing Order Service ==="
echo "1. Health check:"
curl -s "$BASE_URL/api/orders/health" | jq .

echo -e "\n2. Get orders for user 1:"
curl -s "$BASE_URL/api/orders/user/1" | jq .

echo -e "\n3. Create new order:"
curl -s -X POST "$BASE_URL/api/orders" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productName":"Test Product","quantity":1,"price":99.99}' | jq .

# 测试服务间通信
echo -e "\n=== Testing Service Communication ==="
echo "Get user orders (user-service calls order-service):"
curl -s "$BASE_URL/api/users/1/orders" | jq .

# 测试负载和错误处理
echo -e "\n=== Testing Load and Error Handling ==="
echo "1. Test slow query:"
time curl -s "$BASE_URL/api/users/1/slow" | jq .

echo -e "\n2. Test error handling:"
curl -s "$BASE_URL/api/users/1/error" || echo "Error handled successfully"

echo -e "\n3. Test high load simulation:"
curl -s "$BASE_URL/api/orders/user/1/load" | jq .

# 测试限流
echo -e "\n=== Testing Rate Limiting ==="
echo "Making multiple requests to test rate limiting..."
for i in {1..10}; do
  echo "Request $i:"
  curl -s -w "Status: %{http_code}, Time: %{time_total}s\n" "$BASE_URL/api/users" -o /dev/null
  sleep 0.1
done

# 测试金丝雀部署
echo -e "\n=== Testing Canary Deployment ==="
echo "Request with canary header:"
curl -s -H "canary: true" "$BASE_URL/api/users/info" | jq .

echo -e "\n=== Testing Blue-Green Deployment ==="
echo "Request with green version header:"
curl -s -H "version: green" "$BASE_URL/api/orders/info" | jq .

echo -e "\nTesting completed!" 