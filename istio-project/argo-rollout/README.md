# Order Service 金丝雀发布演示

本演示展示了如何使用 Argo Rollouts 和 Istio 实现 order-service 的金丝雀发布。

## 架构概述

```
用户请求 → Istio Gateway → VirtualService → 
            ↓
         Order Service
         ├── v1 (stable) - 80% 流量
         └── v2 (canary) - 20% 流量
```

## 文件说明

- `order-service-rollout.yaml` - Argo Rollout 配置
- `order-service-service.yaml` - Kubernetes Service 配置
- `istio-virtualservice.yaml` - Istio 流量路由配置
- `analysis-template.yaml` - 发布分析模板
- `build-v1.sh` - 构建 v1 版本镜像
- `build-v2.sh` - 构建 v2 版本镜像
- `demo.sh` - 演示脚本

## 快速开始

### 1. 检查前置条件
```bash
./demo.sh check
```

### 2. 构建镜像
```bash
./demo.sh build
```

### 3. 部署初始版本
```bash
./demo.sh deploy
```

### 4. 开始金丝雀发布
```bash
./demo.sh canary
```

### 5. 监控发布进度
```bash
./demo.sh watch
```

## 金丝雀发布策略

本演示使用以下发布策略：

1. **20%** 流量到新版本 → 暂停等待手动确认
2. **40%** 流量到新版本 → 自动等待 30 秒
3. **60%** 流量到新版本 → 自动等待 30 秒
4. **80%** 流量到新版本 → 自动等待 30 秒
5. **100%** 流量到新版本 → 发布完成

## 版本差异

### v1 版本特性
- 基础 order-service 功能
- 标准 JVM 配置
- APP_VERSION=v1

### v2 版本特性
- 增强的订单处理
- 改进的响应时间
- 更好的错误处理
- 高级分析功能
- 优化的 JVM 配置 (G1GC)
- APP_VERSION=v2

## 监控指标

分析模板监控以下指标：
- **成功率**: ≥ 95%
- **响应时间**: P95 ≤ 500ms

## 常用命令

### 查看发布状态
```bash
kubectl get rollout order-service-rollout -n rollout-demo
kubectl describe rollout order-service-rollout -n rollout-demo
```

### 手动推进发布
```bash
kubectl patch rollout order-service-rollout -n rollout-demo --type='merge' -p='{"status":{"verifyingPreview":true}}'
```

### 暂停发布
```bash
kubectl patch rollout order-service-rollout -n rollout-demo --type='merge' -p='{"spec":{"paused":true}}'
```

### 回滚发布
```bash
kubectl patch rollout order-service-rollout -n rollout-demo --type='merge' -p='{"spec":{"template":{"spec":{"containers":[{"name":"order-service","image":"order-service:v1"}]}}}}'
```

### 重新开始发布
```bash
kubectl patch rollout order-service-rollout -n rollout-demo --type='merge' -p='{"spec":{"paused":false}}'
```

## 测试接口

### 健康检查
```bash
curl http://localhost:8080/api/orders/health
```

### 服务信息
```bash
curl http://localhost:8080/api/orders/info
```

### 版本特定测试
```bash
# 测试 v1 版本
curl -H "end-user: stable" http://localhost:8080/api/orders/info

# 测试 v2 版本 (金丝雀)
curl -H "end-user: canary" http://localhost:8080/api/orders/info
```

## 清理资源

```bash
./demo.sh cleanup
```

## 故障排除

### 1. 发布卡住
检查分析指标是否满足条件：
```bash
kubectl get analysisrun -n rollout-demo
kubectl describe analysisrun <analysis-run-name> -n rollout-demo
```

### 2. 镜像拉取失败
确保镜像已正确构建：
```bash
docker images | grep order-service
```

### 3. 服务不可用
检查 Pod 状态：
```bash
kubectl get pods -n rollout-demo -l app=order-service
kubectl logs -n rollout-demo -l app=order-service
```

## 注意事项

1. 确保已安装 Argo Rollouts Controller
2. 确保已安装 Istio 服务网格  
3. 确保 Prometheus 正在运行（用于指标收集）
4. **无需安装 kubectl argo rollouts 插件** - 本演示使用标准 kubectl 命令
5. 生产环境建议调整分析阈值和暂停时间 