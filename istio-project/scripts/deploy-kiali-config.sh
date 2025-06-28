#!/bin/bash

echo "🚀 部署Kiali可观测性配置..."

# 确保namespace存在且启用Istio注入
echo "📝 创建namespace..."
kubectl apply -f k8s/namespace.yaml

# 等待namespace创建完成
sleep 2

# 部署服务
echo "🔧 部署服务..."
kubectl apply -f k8s/user-service-deployment.yaml
kubectl apply -f k8s/order-service-deployment.yaml

# 等待服务启动
sleep 5

# 部署Istio配置
echo "🌐 部署Istio配置..."
kubectl apply -f istio/destination-rules.yaml
kubectl apply -f istio/virtual-services.yaml
kubectl apply -f istio/observability.yaml

# 等待配置生效
sleep 10

# 检查部署状态
echo "🔍 检查部署状态..."
kubectl get pods -n istio-demo
kubectl get services -n istio-demo
kubectl get destinationrules -n istio-demo
kubectl get virtualservices -n istio-demo
kubectl get telemetry -n istio-demo

echo "✅ 部署完成！"
echo ""
echo "📊 测试服务间通信："
echo "kubectl exec -n istio-demo -it deployment/user-service-v1 -- curl http://user-service:8080/api/users/trace"
echo ""
echo "🌐 Kiali访问："
echo "kubectl port-forward -n istio-system svc/kiali 20001:20001"
echo "然后访问 http://localhost:20001" 