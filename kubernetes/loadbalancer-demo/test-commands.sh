#!/bin/bash

# LoadBalancer Demo 测试脚本
echo "🚀 开始部署 LoadBalancer Demo..."

# 1. 部署应用
echo "📦 部署Web应用..."
kubectl apply -f simple-web-app.yaml

# 2. 等待Pod启动
echo "⏳ 等待Pod启动..."
kubectl wait --for=condition=ready pod -l app=simple-web --timeout=60s

# 3. 查看部署状态
echo "📊 查看部署状态:"
echo "================================"
kubectl get deployments simple-web-app
echo ""
kubectl get pods -l app=simple-web -o wide
echo ""

# 4. 查看Service状态
echo "🌐 查看LoadBalancer Service状态:"
echo "================================"
kubectl get svc simple-web-loadbalancer
echo ""

# 5. 获取访问地址
echo "🔗 获取访问地址:"
echo "================================"
EXTERNAL_IP=$(kubectl get svc simple-web-loadbalancer -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
EXTERNAL_HOSTNAME=$(kubectl get svc simple-web-loadbalancer -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

if [ "$EXTERNAL_IP" != "" ]; then
    echo "✅ 外部IP: http://$EXTERNAL_IP"
    ACCESS_URL="http://$EXTERNAL_IP"
elif [ "$EXTERNAL_HOSTNAME" != "" ]; then
    echo "✅ 外部域名: http://$EXTERNAL_HOSTNAME"
    ACCESS_URL="http://$EXTERNAL_HOSTNAME"
else
    # 获取NodePort端口号
    NODE_PORT=$(kubectl get svc simple-web-loadbalancer -o jsonpath='{.spec.ports[0].nodePort}')
    echo "⚠️  LoadBalancer pending，使用NodePort访问:"
    echo "   http://localhost:$NODE_PORT"
    ACCESS_URL="http://localhost:$NODE_PORT"
fi

echo ""
echo "📝 详细信息:"
kubectl describe svc simple-web-loadbalancer

echo ""
echo "🧪 测试负载均衡效果:"
echo "================================"
echo "如果有外部IP，执行以下测试:"
echo ""

# 6. 负载均衡测试 (如果有外部访问地址)
if [ "$EXTERNAL_IP" != "" ] || [ "$EXTERNAL_HOSTNAME" != "" ]; then
    echo "正在测试负载均衡..."
    for i in {1..5}; do
        echo "请求 $i:"
        curl -s $ACCESS_URL | grep -E "(主机名|hostname)" || echo "连接测试 $i"
        sleep 1
    done
else
    echo "请手动在浏览器中访问: $ACCESS_URL"
    echo "或者使用端口转发: kubectl port-forward svc/simple-web-loadbalancer 8080:80"
    echo "然后访问: http://localhost:8080"
fi

echo ""
echo "🎯 使用说明:"
echo "================================"
echo "1. 在浏览器中打开访问地址"
echo "2. 点击'刷新信息'按钮多次，观察负载均衡效果"
echo "3. 每次刷新可能连接到不同的Pod"
echo ""
echo "📊 监控命令:"
echo "kubectl get pods -l app=simple-web -w"
echo "kubectl logs -f deployment/simple-web-app"
echo ""
echo "🗑️  清理命令:"
echo "kubectl delete -f simple-web-app.yaml" 