# Kubernetes Ingress Controller 部署与测试指南

本指南提供了完整的 Kubernetes Ingress Controller 部署和各种使用场景的测试方法。

## 目录结构

```
kubernetes/ingress-controller/
├── deploy.yaml                    # NGINX Ingress Controller 部署文件
├── myapp.yaml                     # 测试应用部署文件
├── my-ingress.yaml                # 基础 Ingress 配置
├── my-ingress-no-host.yaml        # 无 Host 的 Ingress 配置
├── my-ingress-wildcard.yaml       # 通配符 Host 的 Ingress 配置
├── my-ingress-ip.yaml             # 基于 IP 的 Ingress 配置
├── ingress-test-scenarios.yaml    # 高级 Ingress 场景配置
├── test-ingress-scenarios.sh      # 自动化测试脚本
└── README.md                      # 本文件
```

## 快速开始

### 1. 部署 Ingress Controller

```bash
# 进入目录
cd kubernetes/ingress-controller

# 部署 NGINX Ingress Controller
kubectl apply -f deploy.yaml

# 检查部署状态
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx
```

### 2. 部署测试应用

```bash
# 部署测试应用
kubectl apply -f myapp.yaml

# 检查应用状态
kubectl get pods,svc
```

### 3. 运行自动化测试

```bash
# 给脚本添加执行权限
chmod +x test-ingress-scenarios.sh

# 运行所有测试场景
./test-ingress-scenarios.sh test

# 清理测试资源
./test-ingress-scenarios.sh cleanup
```

## Ingress 场景详解

### 场景1: 基础域名路由

**文件**: `my-ingress.yaml`

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-ingress
spec:
  ingressClassName: nginx
  rules:
  - host: "myapp.example.com"
    http:
      paths:
      - path: "/"
        pathType: Prefix
        backend:
          service:
            name: myapp-service
            port:
              number: 18080
```

**测试方法**:
```bash
# 获取 Ingress IP
INGRESS_IP=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# 测试访问
curl -H "Host: myapp.example.com" http://$INGRESS_IP/
```

### 场景2: 无 Host 路由

**文件**: `my-ingress-no-host.yaml`

适用于不需要特定域名的场景，直接通过 IP 访问。

**测试方法**:
```bash
curl http://$INGRESS_IP/
```

### 场景3: 通配符域名路由

**文件**: `my-ingress-wildcard.yaml`

支持通配符域名匹配，如 `*.example.com`。

**测试方法**:
```bash
curl -H "Host: api.example.com" http://$INGRESS_IP/
curl -H "Host: web.example.com" http://$INGRESS_IP/
```

### 场景4: 基于路径的路由

**配置示例**:
```yaml
spec:
  rules:
  - host: "myapp.local"
    http:
      paths:
      - path: /api(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: api-service
            port:
              number: 8080
      - path: /web(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: web-service
            port:
              number: 80
```

**测试方法**:
```bash
curl -H "Host: myapp.local" http://$INGRESS_IP/api/users
curl -H "Host: myapp.local" http://$INGRESS_IP/web/dashboard
```

### 场景5: HTTPS 重定向

**关键注解**:
```yaml
annotations:
  nginx.ingress.kubernetes.io/ssl-redirect: "true"
  nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
```

### 场景6: 会话亲和性

**关键注解**:
```yaml
annotations:
  nginx.ingress.kubernetes.io/affinity: "cookie"
  nginx.ingress.kubernetes.io/session-cookie-name: "route"
  nginx.ingress.kubernetes.io/session-cookie-expires: "86400"
```

### 场景7: 速率限制

**关键注解**:
```yaml
annotations:
  nginx.ingress.kubernetes.io/rate-limit-rps: "10"
  nginx.ingress.kubernetes.io/rate-limit-connections: "5"
```

**测试方法**:
```bash
# 连续发送请求测试速率限制
for i in {1..15}; do
  curl -H "Host: ratelimit.example.com" http://$INGRESS_IP/
  sleep 0.1
done
```

### 场景8: 基本认证

**配置步骤**:
1. 创建认证 Secret
2. 在 Ingress 中添加认证注解

**测试方法**:
```bash
# 无认证访问 (应该返回 401)
curl -H "Host: auth.example.com" http://$INGRESS_IP/

# 有认证访问 (应该返回 200)
curl -H "Host: auth.example.com" -u admin:admin http://$INGRESS_IP/
```

### 场景9: CORS 支持

**关键注解**:
```yaml
annotations:
  nginx.ingress.kubernetes.io/enable-cors: "true"
  nginx.ingress.kubernetes.io/cors-allow-origin: "https://myapp.com"
  nginx.ingress.kubernetes.io/cors-allow-methods: "GET, POST, PUT, DELETE, OPTIONS"
```

## 常用命令

### 查看 Ingress 状态
```bash
# 查看所有 Ingress
kubectl get ingress

# 查看 Ingress 详细信息
kubectl describe ingress <ingress-name>

# 查看 Ingress Controller 日志
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
```

### 调试 Ingress
```bash
# 查看 Ingress Controller 配置
kubectl exec -n ingress-nginx <ingress-pod-name> -- cat /etc/nginx/nginx.conf

# 测试 DNS 解析
kubectl exec -n ingress-nginx <ingress-pod-name> -- nslookup <service-name>
```

### 性能测试
```bash
# 使用 ab 进行压力测试
ab -n 1000 -c 10 -H "Host: myapp.example.com" http://$INGRESS_IP/

# 使用 wrk 进行性能测试
wrk -t12 -c400 -d30s -H "Host: myapp.example.com" http://$INGRESS_IP/
```

## 故障排除

### 常见问题

1. **Ingress 无法访问**
   - 检查 Ingress Controller 是否正常运行
   - 确认 Service 和 Pod 状态
   - 验证 Ingress 规则配置

2. **Host 头不匹配**
   - 确认 Host 头设置正确
   - 检查通配符配置
   - 验证 DNS 解析

3. **后端服务不可达**
   - 检查 Service 端口配置
   - 验证 Pod 健康状态
   - 确认网络策略

### 日志分析
```bash
# 查看 Ingress Controller 日志
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx -f

# 查看特定 Pod 日志
kubectl logs -n ingress-nginx <pod-name> -f
```

## 最佳实践

1. **安全性**
   - 启用 HTTPS 重定向
   - 配置适当的 CORS 策略
   - 使用基本认证或 OAuth

2. **性能优化**
   - 配置会话亲和性
   - 设置适当的速率限制
   - 使用连接池

3. **监控**
   - 配置 Prometheus 监控
   - 设置告警规则
   - 定期检查日志

4. **高可用性**
   - 部署多个 Ingress Controller 实例
   - 配置负载均衡
   - 设置健康检查

## 参考资料

- [NGINX Ingress Controller 官方文档](https://kubernetes.github.io/ingress-nginx/)
- [Kubernetes Ingress 概念](https://kubernetes.io/docs/concepts/services-networking/ingress/)
- [NGINX 注解参考](https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/) 