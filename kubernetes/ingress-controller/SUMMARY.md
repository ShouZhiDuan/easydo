# Kubernetes Ingress Controller 部署总结

## 🎯 项目概述

本项目提供了完整的 Kubernetes Ingress Controller 部署方案和多种使用场景的测试示例。

## 📁 项目结构

```
kubernetes/ingress-controller/
├── deploy.yaml                    # NGINX Ingress Controller 部署文件
├── myapp.yaml                     # 测试应用 (nginx)
├── my-ingress.yaml                # 基础域名路由
├── my-ingress-no-host.yaml        # 无Host路由
├── my-ingress-wildcard.yaml       # 通配符域名路由
├── my-ingress-ip.yaml             # IP地址路由
├── ingress-test-scenarios.yaml    # 高级场景合集
├── test-ingress-scenarios.sh      # 自动化测试脚本
├── README.md                      # 详细使用指南
├── DEMO.md                        # 演示步骤
└── SUMMARY.md                     # 本文件
```

## 🚀 已完成的部署

### 1. Ingress Controller 部署
✅ **NGINX Ingress Controller** 已成功部署
- Namespace: `ingress-nginx`
- Service Type: LoadBalancer
- External IP: `localhost`
- Ports: 80, 443

### 2. 测试应用部署
✅ **测试应用** 已成功部署
- Deployment: `myapp-deployment` (2 replicas)
- Service: `myapp-service` (端口 18080)
- Image: `nginx:1.21`

## 🎭 Ingress 场景示例

### 基础场景

#### 1. 域名路由 (`my-ingress.yaml`)
```yaml
spec:
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

#### 2. 无Host路由 (`my-ingress-no-host.yaml`)
```yaml
spec:
  rules:
  - http:  # 没有host字段，直接通过IP访问
      paths:
      - path: "/"
        pathType: Prefix
        backend:
          service:
            name: myapp-service
            port:
              number: 18080
```

#### 3. 通配符域名路由 (`my-ingress-wildcard.yaml`)
```yaml
spec:
  rules:
  - host: "*.local"  # 匹配所有.local域名
  - host: "*.example.com"  # 匹配所有.example.com子域名
```

#### 4. IP地址路由 (`my-ingress-ip.yaml`)
```yaml
spec:
  rules:
  - host: "127.0.0.1"  # 直接使用IP作为host
```

### 高级场景 (`ingress-test-scenarios.yaml`)

#### 1. 多域名路由
- `app1.example.com` 和 `app2.example.com` 路由到同一服务

#### 2. 基于路径的路由
- `/api/*` 路径路由
- `/web/*` 路径路由
- 支持路径重写

#### 3. HTTPS 重定向
```yaml
annotations:
  nginx.ingress.kubernetes.io/ssl-redirect: "true"
  nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
```

#### 4. 会话亲和性
```yaml
annotations:
  nginx.ingress.kubernetes.io/affinity: "cookie"
  nginx.ingress.kubernetes.io/session-cookie-name: "route"
```

#### 5. 速率限制
```yaml
annotations:
  nginx.ingress.kubernetes.io/rate-limit-rps: "10"
  nginx.ingress.kubernetes.io/rate-limit-connections: "5"
```

#### 6. 基本认证
- 预设用户名/密码: `admin/admin`
- Secret: `basic-auth`

#### 7. CORS 支持
```yaml
annotations:
  nginx.ingress.kubernetes.io/enable-cors: "true"
  nginx.ingress.kubernetes.io/cors-allow-origin: "https://myapp.com"
```

#### 8. 负载均衡算法
```yaml
annotations:
  nginx.ingress.kubernetes.io/upstream-hash-by: "$request_uri"
  nginx.ingress.kubernetes.io/load-balance: "round_robin"
```

## 🧪 测试方法

### 快速测试
```bash
# 1. 获取访问地址
INGRESS_IP="localhost"
INGRESS_PORT="80"

# 2. 测试基础访问
curl http://$INGRESS_IP:$INGRESS_PORT/

# 3. 测试域名路由
curl -H "Host: myapp.example.com" http://$INGRESS_IP:$INGRESS_PORT/

# 4. 测试通配符域名
curl -H "Host: api.example.com" http://$INGRESS_IP:$INGRESS_PORT/
```

### 自动化测试
```bash
# 运行完整测试套件
chmod +x test-ingress-scenarios.sh
./test-ingress-scenarios.sh test

# 清理测试资源
./test-ingress-scenarios.sh cleanup
```

## 🔧 常用命令

### 查看状态
```bash
# 查看 Ingress Controller
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx

# 查看 Ingress 规则
kubectl get ingress
kubectl describe ingress <name>

# 查看日志
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
```

### 部署/清理
```bash
# 部署所有资源
kubectl apply -f deploy.yaml
kubectl apply -f myapp.yaml
kubectl apply -f ingress-test-scenarios.yaml

# 清理所有资源
kubectl delete -f ingress-test-scenarios.yaml
kubectl delete -f myapp.yaml
kubectl delete -f deploy.yaml
```

## 📊 性能测试

### 压力测试
```bash
# 使用 ab 工具
ab -n 1000 -c 10 -H "Host: myapp.example.com" http://localhost:80/

# 使用 wrk 工具
wrk -t4 -c100 -d30s -H "Host: myapp.example.com" http://localhost:80/
```

### 速率限制测试
```bash
# 连续请求测试
for i in {1..20}; do
  curl -H "Host: ratelimit.example.com" http://localhost:80/
  sleep 0.1
done
```

## 🛠️ 故障排除

### 常见问题

1. **Ingress Controller 启动慢**
   - 原因: 镜像拉取时间较长
   - 解决: 等待镜像拉取完成

2. **Webhook 验证失败**
   - 原因: admission webhook 未就绪
   - 解决: 等待 admission jobs 完成

3. **访问返回 404**
   - 检查 Ingress 规则配置
   - 验证 Service 和 Endpoints
   - 确认 Host 头设置

### 调试命令
```bash
# 查看详细状态
kubectl describe pod -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx

# 查看事件
kubectl get events -n ingress-nginx

# 测试服务连通性
kubectl exec -it <pod-name> -- curl myapp-service:18080
```

## 🎉 项目特色

1. **完整性**: 涵盖从基础到高级的所有常见场景
2. **实用性**: 提供可直接使用的 YAML 配置
3. **自动化**: 包含完整的测试脚本
4. **文档化**: 详细的使用说明和故障排除指南

## 📚 学习收获

通过本项目，你将掌握：
- ✅ Kubernetes Ingress Controller 的部署和配置
- ✅ 多种路由策略的实现方法
- ✅ 高级功能的配置技巧
- ✅ 性能测试和故障排除方法
- ✅ 生产环境的最佳实践

## 🔗 相关资源

- [NGINX Ingress Controller 官方文档](https://kubernetes.github.io/ingress-nginx/)
- [Kubernetes Ingress 概念](https://kubernetes.io/docs/concepts/services-networking/ingress/)
- [NGINX 注解参考](https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/)

---

**注意**: 在生产环境中使用时，请根据实际需求调整配置，特别是安全相关的设置。 