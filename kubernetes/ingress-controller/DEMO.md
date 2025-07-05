# Kubernetes Ingress Controller 演示指南

## 演示概述

本演示展示了如何在 Kubernetes 集群中部署 NGINX Ingress Controller，并通过多种场景测试其功能。

## 前置条件

- 运行中的 Kubernetes 集群
- kubectl 命令行工具
- curl 命令行工具

## 演示步骤

### 步骤1: 部署 Ingress Controller

```bash
# 1. 进入项目目录
cd kubernetes/ingress-controller

# 2. 部署 NGINX Ingress Controller
kubectl apply -f deploy.yaml

# 3. 检查部署状态
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx

# 4. 等待 Pod 就绪
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=300s
```

**预期输出**:
```
namespace/ingress-nginx created
serviceaccount/ingress-nginx created
...
pod/ingress-nginx-controller-xxx Ready
```

### 步骤2: 部署测试应用

```bash
# 1. 部署测试应用
kubectl apply -f myapp.yaml

# 2. 检查应用状态
kubectl get pods,svc

# 3. 等待应用就绪
kubectl wait --for=condition=ready pod -l app=myapp --timeout=120s
```

**预期输出**:
```
deployment.apps/myapp-deployment created
service/myapp-service created
pod/myapp-deployment-xxx Ready
```

### 步骤3: 获取 Ingress 访问地址

```bash
# 获取 Ingress Controller 的外部 IP
INGRESS_IP=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# 如果是本地环境，可能显示为 localhost
if [ -z "$INGRESS_IP" ]; then
    INGRESS_IP="localhost"
fi

# 获取端口
INGRESS_PORT=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.spec.ports[0].port}')

echo "Ingress Controller 访问地址: $INGRESS_IP:$INGRESS_PORT"
```

### 步骤4: 测试基础 Ingress 场景

#### 场景1: 基于域名的路由

```bash
# 1. 部署 Ingress 规则
kubectl apply -f my-ingress.yaml

# 2. 检查 Ingress 状态
kubectl get ingress

# 3. 测试访问
curl -H "Host: myapp.example.com" http://$INGRESS_IP:$INGRESS_PORT/
```

**预期结果**: 返回 nginx 默认页面

#### 场景2: 无 Host 路由

```bash
# 1. 部署无 Host 的 Ingress
kubectl apply -f my-ingress-no-host.yaml

# 2. 直接访问 IP
curl http://$INGRESS_IP:$INGRESS_PORT/
```

**预期结果**: 返回 nginx 默认页面

#### 场景3: 通配符域名路由

```bash
# 1. 部署通配符 Ingress
kubectl apply -f my-ingress-wildcard.yaml

# 2. 测试不同的子域名
curl -H "Host: api.example.com" http://$INGRESS_IP:$INGRESS_PORT/
curl -H "Host: web.example.com" http://$INGRESS_IP:$INGRESS_PORT/
curl -H "Host: test.local" http://$INGRESS_IP:$INGRESS_PORT/
```

**预期结果**: 所有请求都返回 nginx 默认页面

### 步骤5: 测试高级 Ingress 场景

```bash
# 1. 部署高级场景配置
kubectl apply -f ingress-test-scenarios.yaml

# 2. 等待规则生效
sleep 10

# 3. 查看所有 Ingress 规则
kubectl get ingress
```

#### 场景4: 基于路径的路由

```bash
# 测试不同路径的路由
curl -H "Host: myapp.local" http://$INGRESS_IP:$INGRESS_PORT/api/users
curl -H "Host: myapp.local" http://$INGRESS_IP:$INGRESS_PORT/web/dashboard
```

#### 场景5: 速率限制测试

```bash
# 连续发送请求测试速率限制
echo "测试速率限制 (每秒10个请求):"
for i in {1..15}; do
    code=$(curl -s -o /dev/null -w "%{http_code}" -H "Host: ratelimit.example.com" "http://$INGRESS_IP:$INGRESS_PORT/")
    echo "请求 $i: HTTP $code"
    if [ "$code" = "429" ]; then
        echo "✅ 速率限制生效!"
        break
    fi
    sleep 0.1
done
```

**预期结果**: 前几个请求返回 200，后续请求返回 429

#### 场景6: 基本认证测试

```bash
# 测试无认证访问
echo "测试无认证访问:"
curl -s -o /dev/null -w "HTTP状态码: %{http_code}\n" -H "Host: auth.example.com" "http://$INGRESS_IP:$INGRESS_PORT/"

# 测试有认证访问
echo "测试有认证访问 (admin:admin):"
curl -s -o /dev/null -w "HTTP状态码: %{http_code}\n" -H "Host: auth.example.com" -u admin:admin "http://$INGRESS_IP:$INGRESS_PORT/"
```

**预期结果**: 无认证返回 401，有认证返回 200

#### 场景7: CORS 测试

```bash
# 测试 CORS 预检请求
echo "测试 CORS 预检请求:"
curl -s -H "Host: cors.example.com" \
     -H "Origin: https://myapp.com" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     "http://$INGRESS_IP:$INGRESS_PORT/"
```

### 步骤6: 监控和调试

#### 查看 Ingress 状态

```bash
# 查看所有 Ingress 资源
kubectl get ingress

# 查看特定 Ingress 的详细信息
kubectl describe ingress my-ingress

# 查看 Ingress Controller 日志
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx --tail=50
```

#### 调试网络连接

```bash
# 测试服务连接
kubectl exec -n ingress-nginx $(kubectl get pods -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx -o jsonpath='{.items[0].metadata.name}') -- curl -s myapp-service.default.svc.cluster.local:18080

# 查看 nginx 配置
kubectl exec -n ingress-nginx $(kubectl get pods -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx -o jsonpath='{.items[0].metadata.name}') -- cat /etc/nginx/nginx.conf | grep -A 20 "server_name myapp.example.com"
```

### 步骤7: 性能测试

```bash
# 使用 ab 进行简单的性能测试
ab -n 100 -c 10 -H "Host: myapp.example.com" http://$INGRESS_IP:$INGRESS_PORT/

# 如果有 wrk 工具
# wrk -t4 -c100 -d30s -H "Host: myapp.example.com" http://$INGRESS_IP:$INGRESS_PORT/
```

### 步骤8: 清理资源

```bash
# 清理 Ingress 规则
kubectl delete -f ingress-test-scenarios.yaml
kubectl delete -f my-ingress.yaml
kubectl delete -f my-ingress-no-host.yaml
kubectl delete -f my-ingress-wildcard.yaml
kubectl delete -f my-ingress-ip.yaml

# 清理测试应用
kubectl delete -f myapp.yaml

# 清理 Ingress Controller (可选)
kubectl delete -f deploy.yaml
```

## 自动化测试脚本

为了简化测试过程，我们提供了自动化测试脚本：

```bash
# 给脚本添加执行权限
chmod +x test-ingress-scenarios.sh

# 运行完整测试
./test-ingress-scenarios.sh test

# 只清理资源
./test-ingress-scenarios.sh cleanup

# 查看帮助
./test-ingress-scenarios.sh help
```

## 常见问题解决

### 问题1: Ingress Controller 启动失败

**症状**: Pod 状态为 CrashLoopBackOff 或 ImagePullBackOff

**解决方案**:
```bash
# 检查 Pod 日志
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx

# 检查镜像是否可用
kubectl describe pod -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
```

### 问题2: Ingress 规则不生效

**症状**: 访问返回 404 或连接被拒绝

**解决方案**:
```bash
# 检查 Ingress 状态
kubectl get ingress
kubectl describe ingress <ingress-name>

# 检查后端服务
kubectl get svc
kubectl get endpoints
```

### 问题3: 证书问题

**症状**: HTTPS 访问失败

**解决方案**:
```bash
# 检查证书 Secret
kubectl get secrets
kubectl describe secret <tls-secret-name>

# 检查 Ingress TLS 配置
kubectl describe ingress <ingress-name>
```

## 总结

通过本演示，你已经学会了：

1. ✅ 部署 NGINX Ingress Controller
2. ✅ 配置基础的 Ingress 路由规则
3. ✅ 实现基于域名和路径的路由
4. ✅ 配置高级功能（认证、速率限制、CORS）
5. ✅ 监控和调试 Ingress
6. ✅ 进行性能测试

Ingress Controller 是 Kubernetes 中管理外部访问的重要组件，掌握其使用方法对于构建生产级应用至关重要。 