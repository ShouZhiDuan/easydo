# Istio 服务网格演示项目

这个项目是一个Maven多模块项目，包含两个Spring Boot微服务，用于演示Istio服务网格的各种功能特性。

## 项目结构

```
istio-project/
├── pom.xml                # 父级POM文件，统一管理依赖和构建
├── user-service/          # 用户服务子模块
│   ├── pom.xml           # 用户服务POM文件
│   ├── Dockerfile        # 用户服务Docker构建文件
│   └── src/              # 用户服务源代码
├── order-service/         # 订单服务子模块
│   ├── pom.xml           # 订单服务POM文件
│   ├── Dockerfile        # 订单服务Docker构建文件
│   └── src/              # 订单服务源代码
├── k8s/                   # Kubernetes部署文件
├── istio/                 # Istio配置文件
├── scripts/               # 构建和部署脚本
├── Makefile              # 便捷构建命令
└── README.md
```

## 服务简介

### User Service (用户服务)
- **端口**: 8080
- **功能**: 用户管理、调用订单服务
- **API路径**: `/api/users`
- **特性**: 熔断器、重试、限流

### Order Service (订单服务)
- **端口**: 8080
- **功能**: 订单管理
- **API路径**: `/api/orders`
- **特性**: 性能测试端点、错误模拟

## Istio功能演示

### 1. 服务发现
- 自动注册和发现服务
- DNS解析和负载均衡

### 2. 流量管理
- **网关配置**: 统一入口管理
- **虚拟服务**: 路由规则配置
- **目标规则**: 负载均衡和连接池设置

### 3. 限流
- **本地限流**: Envoy本地令牌桶算法
- **全局限流**: 配置化限流策略

### 4. 熔断和容错
- **连接池**: 控制并发连接数
- **异常检测**: 自动剔除异常实例
- **超时和重试**: 自动故障恢复

### 5. 金丝雀部署
- **流量分割**: 90% v1 + 10% v2
- **基于头部路由**: canary=true路由到v2

### 6. 蓝绿部署
- **版本切换**: 基于请求头路由
- **零停机部署**: 瞬时流量切换

### 7. 安全
- **mTLS**: 服务间加密通信
- **JWT认证**: API访问控制
- **授权策略**: 基于身份的访问控制

### 8. 可观测性
- **分布式追踪**: Jaeger集成
- **指标收集**: Prometheus集成
- **访问日志**: 结构化日志输出

## 快速开始

### 前置条件
- Docker
- Kubernetes (minikube/kind/k3s)
- Istio
- kubectl
- Maven
- jq (用于测试脚本)

### 1. 构建项目
```bash
# 方式一：使用脚本构建
chmod +x scripts/*.sh
./scripts/build.sh

# 方式二：使用Makefile构建
make build

# 方式三：仅构建Maven项目
mvn clean package -DskipTests
# 或者
make maven-build
```

### 2. 部署到Kubernetes
```bash
# 使用脚本部署
./scripts/deploy.sh

# 或者使用Makefile
make deploy
```

### 3. 运行测试
```bash
# 使用脚本测试
./scripts/test.sh

# 或者使用Makefile
make test
```

## API使用示例

### 用户服务API

```bash
# 健康检查
curl http://localhost/api/users/health

# 获取所有用户
curl http://localhost/api/users

# 获取特定用户
curl http://localhost/api/users/1

# 创建用户
curl -X POST http://localhost/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","fullName":"John Doe"}'

# 获取用户订单 (跨服务调用)
curl http://localhost/api/users/1/orders
```

### 订单服务API

```bash
# 健康检查
curl http://localhost/api/orders/health

# 获取用户订单
curl http://localhost/api/orders/user/1

# 创建订单
curl -X POST http://localhost/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productName":"Laptop","quantity":1,"price":999.99}'
```

## 功能测试

### 限流测试
```bash
# 快速发送多个请求测试限流
for i in {1..20}; do
  curl -w "Status: %{http_code}\n" http://localhost/api/users/health
done
```

### 熔断测试
```bash
# 触发错误以测试熔断器
curl http://localhost/api/users/1/error
```

### 金丝雀部署测试
```bash
# 普通请求 (90%概率到v1)
curl http://localhost/api/users/info

# 金丝雀请求 (100%到v2)
curl -H "canary: true" http://localhost/api/users/info
```

### 蓝绿部署测试
```bash
# 蓝色版本 (默认)
curl http://localhost/api/orders/info

# 绿色版本
curl -H "version: green" http://localhost/api/orders/info
```

## 监控和追踪

### Prometheus指标
```bash
# 用户服务指标
curl http://localhost/api/users/actuator/prometheus

# 订单服务指标
curl http://localhost/api/orders/actuator/prometheus
```

### Jaeger追踪
访问Jaeger UI查看分布式追踪信息。

### Kiali服务网格可视化
访问Kiali UI查看服务网格拓扑和流量。

## 配置说明

### 数据库配置
项目使用MySQL数据库，配置信息如下：
- **主机**: localhost
- **端口**: 33306  
- **数据库**: test
- **用户名**: root
- **密码**: (空)

两个服务使用同一个MySQL数据库实例，通过不同的表来隔离数据：
- `user-service`: users表
- `order-service`: orders表

### Maven配置
- `pom.xml` - 父级POM，统一管理所有依赖版本和插件
- `user-service/pom.xml` - 用户服务POM，继承父级配置
- `order-service/pom.xml` - 订单服务POM，继承父级配置

### 应用配置
- `user-service/src/main/resources/application.yml` - 用户服务配置
- `order-service/src/main/resources/application.yml` - 订单服务配置

### Kubernetes配置
- `k8s/namespace.yaml` - 命名空间配置
- `k8s/*-deployment.yaml` - 部署和服务配置

### Istio配置
- `istio/gateway.yaml` - 网关和虚拟服务
- `istio/destination-rules.yaml` - 目标规则
- `istio/rate-limiting.yaml` - 限流配置
- `istio/circuit-breaker.yaml` - 熔断配置
- `istio/canary-deployment.yaml` - 金丝雀部署
- `istio/blue-green-deployment.yaml` - 蓝绿部署
- `istio/security.yaml` - 安全策略
- `istio/observability.yaml` - 可观测性配置

### 构建和部署
- `scripts/build.sh` - 构建脚本
- `scripts/deploy.sh` - 部署脚本
- `scripts/test.sh` - 测试脚本
- `Makefile` - 便捷构建命令

## 故障排除

### 常见问题

1. **Pod无法启动**
   ```bash
   kubectl describe pod <pod-name> -n istio-demo
   kubectl logs <pod-name> -n istio-demo
   ```

2. **服务无法访问**
   ```bash
   kubectl get services -n istio-demo
   kubectl get virtualservices -n istio-demo
   ```

3. **Istio配置问题**
   ```bash
   istioctl analyze -n istio-demo
   istioctl proxy-config cluster <pod-name> -n istio-demo
   ```

### 清理环境
```bash
kubectl delete namespace istio-demo
```

## 扩展功能

这个项目可以进一步扩展以下功能：

1. **多版本管理**: 实现更复杂的版本管理策略
2. **A/B测试**: 基于用户特征的流量分割
3. **自动扩缩容**: HPA和VPA集成
4. **混沌工程**: Chaos Monkey集成
5. **高级安全**: OAuth2, RBAC等
6. **多集群部署**: 跨集群服务网格

## 参考资料

- [Istio官方文档](https://istio.io/docs/)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Kubernetes官方文档](https://kubernetes.io/docs/)
- [Prometheus监控](https://prometheus.io/docs/)
- [Jaeger追踪](https://www.jaegertracing.io/docs/) 