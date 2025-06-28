# Istio 可观测性控制台启动指南

## 1. 检查组件状态
```bash
# 检查所有可观测性组件
kubectl get pods -n istio-system | grep -E "(kiali|jaeger|grafana|prometheus)"
```

## 2. 启动控制台端口转发
```bash
# 启动Kiali (服务网格可视化)
kubectl port-forward -n istio-system svc/kiali 20001:20001 &

# 启动Grafana (监控仪表板)
kubectl port-forward -n istio-system svc/grafana 3000:3000 &

# 启动Jaeger (链路追踪)
kubectl port-forward -n istio-system pod/$(kubectl get pods -n istio-system | grep jaeger | awk '{print $1}') 16686:16686 &

# 启动Prometheus (指标收集)
kubectl port-forward -n istio-system svc/prometheus 9090:9090 &
```

## 3. 访问地址
- **Kiali**: http://localhost:20001 (服务拓扑图)
- **Grafana**: http://localhost:3000 (监控仪表板)
- **Jaeger**: http://localhost:16686 (链路追踪)
- **Prometheus**: http://localhost:9090 (指标查询)

## 4. 生成测试数据

### 4.1 启动服务端口转发
```bash
# 启动用户服务端口转发
kubectl port-forward -n istio-demo svc/user-service 8080:8080 &

# 启动订单服务端口转发（可选）
kubectl port-forward -n istio-demo svc/order-service 8081:8080 &
```

### 4.2 基础测试请求
```bash
# 生成基础链路追踪数据
for i in {1..10}; do 
  curl -s http://localhost:8080/api/users/trace
  echo "Request $i completed"
  sleep 1
done
```

### 4.3 不同类型的测试场景

#### 正常流量测试
```bash
# 持续正常流量（30秒）
for i in {1..30}; do 
  curl -s http://localhost:8080/api/users/trace > /dev/null
  curl -s http://localhost:8080/api/users/health > /dev/null
  echo "Normal traffic batch $i"
  sleep 1
done
```

#### 高并发测试
```bash
# 并发请求测试
for i in {1..5}; do
  for j in {1..10}; do
    curl -s http://localhost:8080/api/users/trace > /dev/null &
  done
  echo "Concurrent batch $i sent (10 requests)"
  sleep 2
  wait  # 等待所有后台请求完成
done
```

#### 错误场景测试
```bash
# 生成404错误
for i in {1..5}; do
  curl -s http://localhost:8080/api/users/nonexistent > /dev/null
  echo "404 Error request $i"
  sleep 1
done

# 生成连接错误（服务不存在的端口）
for i in {1..3}; do
  curl -s http://localhost:9999/api/test > /dev/null 2>&1
  echo "Connection error $i"
  sleep 1
done
```

#### 混合流量测试
```bash
# 混合不同类型的请求
for i in {1..20}; do
  # 70% 正常请求
  if [ $((i % 10)) -lt 7 ]; then
    curl -s http://localhost:8080/api/users/trace > /dev/null
    echo "Normal request $i"
  # 20% 健康检查
  elif [ $((i % 10)) -lt 9 ]; then
    curl -s http://localhost:8080/api/users/health > /dev/null
    echo "Health check $i"
  # 10% 错误请求
  else
    curl -s http://localhost:8080/api/users/error > /dev/null 2>&1
    echo "Error request $i"
  fi
  sleep 0.5
done
```

### 4.4 一键生成完整测试数据
```bash
#!/bin/bash
echo "🎯 开始生成完整测试数据..."

# 启动服务端口转发
kubectl port-forward -n istio-demo svc/user-service 8080:8080 &
FORWARD_PID=$!
sleep 3

echo "📊 生成基础链路追踪数据..."
for i in {1..10}; do curl -s http://localhost:8080/api/users/trace > /dev/null; done

echo "🚀 生成高并发数据..."
for i in {1..20}; do curl -s http://localhost:8080/api/users/trace > /dev/null & done
wait

echo "❌生成错误数据..."
for i in {1..5}; do curl -s http://localhost:8080/api/users/nonexistent > /dev/null 2>&1; done

echo "✅ 测试数据生成完成！"
echo "现在可以在控制台中查看追踪数据了"

# 停止端口转发
kill $FORWARD_PID 2>/dev/null
```

### 4.5 使用独立测试脚本（推荐）
```bash
# 使用专用的测试数据生成脚本
chmod +x scripts/generate-test-data.sh

# 交互式选择测试类型
./scripts/generate-test-data.sh

# 或直接执行特定测试
./scripts/generate-test-data.sh all      # 执行所有测试
./scripts/generate-test-data.sh basic    # 只执行基础测试
./scripts/generate-test-data.sh mixed    # 只执行混合流量测试
```

## 5. 停止端口转发
```bash
# 停止所有端口转发
pkill -f "kubectl port-forward"
```

## 6. 快速启动脚本 (可选)
```bash
#!/bin/bash
echo "🚀 启动所有可观测性控制台..."
kubectl port-forward -n istio-system svc/kiali 20001:20001 &
kubectl port-forward -n istio-system svc/grafana 3000:3000 &
kubectl port-forward -n istio-system pod/$(kubectl get pods -n istio-system | grep jaeger | awk '{print $1}') 16686:16686 &
kubectl port-forward -n istio-system svc/prometheus 9090:9090 &
echo "✅ 控制台已启动，访问地址："
echo "Kiali: http://localhost:20001"
echo "Grafana: http://localhost:3000"
echo "Jaeger: http://localhost:16686"
echo "Prometheus: http://localhost:9090"
```

## 7. Jaeger追踪故障排查

### 问题：Jaeger中找不到微服务

如果在Jaeger Service下拉菜单中只看到 `jaeger-all-in-one`，请按以下步骤操作：

#### 7.1 检查配置
```bash
# 检查追踪采样率
kubectl get configmap istio -n istio-system -o yaml | grep -A5 "tracing"

# 检查Telemetry配置
kubectl get telemetry -n istio-demo

# 验证Istio配置
istioctl proxy-config bootstrap $(kubectl get pods -n istio-demo | grep user-service | head -1 | awk '{print $1}') -n istio-demo | grep "tracing"
```

#### 7.2 修复配置
```bash
# 重新应用追踪配置
kubectl apply -f istio/observability.yaml

# 重启服务以应用新配置
kubectl rollout restart deployment -n istio-demo
kubectl rollout restart deployment/istiod -n istio-system

# 等待重启完成
kubectl rollout status deployment -n istio-demo --timeout=120s
```

#### 7.3 强制生成测试数据
```bash
# 手动生成大量测试请求
kubectl port-forward -n istio-demo svc/user-service 8080:8080 > /dev/null 2>&1 &
for i in {1..50}; do 
  curl -s http://localhost:8080/api/users/trace > /dev/null
  echo "Request $i sent"
  sleep 0.1
done
pkill -f "kubectl port-forward.*user-service"
```

#### 7.4 检查可能的服务名称
在Jaeger Service下拉菜单中查找：
- `user-service.istio-demo`
- `order-service.istio-demo` 
- `user-service.istio-demo.svc.cluster.local`
- `istio-proxy`
- 或者选择 "All Services" 查看所有追踪

#### 7.5 验证追踪数据
```bash
# 检查Jaeger日志
kubectl logs -n istio-system $(kubectl get pods -n istio-system | grep jaeger | awk '{print $1}') --tail=20

# 检查sidecar日志
kubectl logs -n istio-demo $(kubectl get pods -n istio-demo | grep user-service | head -1 | awk '{print $1}') -c istio-proxy --tail=10
``` 