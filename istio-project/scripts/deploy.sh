#!/bin/bash

set -e

echo "Deploying Istio Demo to Kubernetes..."

# 创建命名空间
echo "Creating namespace..."
kubectl apply -f k8s/namespace.yaml

# 等待命名空间创建完成
kubectl wait --for=condition=Ready namespace/istio-demo --timeout=60s

# 部署应用
echo "Deploying applications..."
kubectl apply -f k8s/user-service-deployment.yaml
kubectl apply -f k8s/order-service-deployment.yaml

# 等待应用部署完成
echo "Waiting for deployments to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/user-service-v1 -n istio-demo
kubectl wait --for=condition=available --timeout=300s deployment/order-service-v1 -n istio-demo

# 部署Istio配置
echo "Applying Istio configurations..."
kubectl apply -f istio/gateway.yaml
kubectl apply -f istio/destination-rules.yaml
kubectl apply -f istio/rate-limiting.yaml
kubectl apply -f istio/circuit-breaker.yaml
kubectl apply -f istio/security.yaml
kubectl apply -f istio/observability.yaml

echo "Deployment completed successfully!"

# 显示服务状态
echo "Service status:"
kubectl get pods -n istio-demo
kubectl get services -n istio-demo
kubectl get virtualservices -n istio-demo
kubectl get destinationrules -n istio-demo

# 获取Ingress Gateway地址
echo "Getting Ingress Gateway address..."
INGRESS_HOST=$(kubectl get po -l istio=ingressgateway -n istio-system -o jsonpath='{.items[0].status.hostIP}')
INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')

echo "Access your services at: http://$INGRESS_HOST:$INGRESS_PORT"
echo "User Service: http://$INGRESS_HOST:$INGRESS_PORT/api/users"
echo "Order Service: http://$INGRESS_HOST:$INGRESS_PORT/api/orders" 