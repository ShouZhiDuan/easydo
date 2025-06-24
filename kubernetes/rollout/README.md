# Argo Rollouts 示例应用

本目录包含使用 Argo Rollouts 进行渐进式部署的示例应用。

## 🚀 快速开始

### 1. 前置条件

- Kubernetes 集群 (v1.16+)
- kubectl 已配置并能连接到集群
- (可选) Argo Rollouts CLI

### 2. 一键部署

```bash
# 部署所有示例应用
./deploy.sh deploy

# 或者手动部署
kubectl apply -f analysis-template.yaml
kubectl apply -f nginx-rollout.yaml
kubectl apply -f canary-rollout.yaml
```

### 3. 查看状态

```bash
# 使用脚本查看
./deploy.sh status

# 或者手动查看
kubectl get rollouts
kubectl get svc
kubectl get pods
```

## 📁 文件说明

### `nginx-rollout.yaml`
- **部署策略**: 蓝绿部署 (Blue-Green)
- **应用**: Nginx Web服务器
- **特性**: 包含预发布和发布后分析
- **端口**: NodePort 30080

### `canary-rollout.yaml`
- **部署策略**: 金丝雀部署 (Canary)
- **应用**: Nginx 演示应用
- **特性**: 分步骤流量切换 (20% → 40% → 60% → 80% → 100%)
- **端口**: NodePort 30081

### `gray-release.yaml`
- **部署策略**: 灰度发布 (基于用户分组)
- **应用**: 用户分组灰度发布演示
- **特性**: 基于Header和Cookie的用户分流
- **端口**: NodePort 30082

### `geo-gray-release.yaml`
- **部署策略**: 地理位置灰度发布
- **应用**: 分地区灰度发布演示
- **特性**: 基于地理位置的分阶段发布
- **端口**: NodePort 30083

### `analysis-template.yaml`
- **功能**: 健康检查和性能分析模板
- **包含**: HTTP成功率检查和基准测试
- **用途**: 在部署过程中进行自动化验证

### `gray-analysis-template.yaml`
- **功能**: 灰度发布专用分析模板
- **包含**: 用户分组分析、业务指标监控、安全检查
- **用途**: 支持复杂的灰度发布场景验证

### `deploy.sh`
- **功能**: 一站式管理脚本
- **包含**: 部署、更新、推进、回滚等操作

## 🎯 使用示例

### 1. 部署应用

```bash
# 部署所有应用
./deploy.sh deploy
```

### 2. 更新应用版本 (触发Rollout)

```bash
# 更新nginx应用到新版本
./deploy.sh update nginx-rollout nginx:1.21

# 更新canary应用
./deploy.sh update canary-demo nginx:1.21-alpine

# 更新灰度发布应用
./deploy.sh update gray-release-app nginx:1.21

# 更新地理位置灰度发布应用
./deploy.sh update geo-gray-release nginx:1.21-alpine
```

### 3. 手动控制Rollout

```bash
# 推进rollout到下一步
./deploy.sh promote canary-demo

# 中止rollout (回滚)
./deploy.sh abort nginx-rollout

# 重启rollout
./deploy.sh restart canary-demo
```

### 4. 访问应用

```bash
# 获取集群节点IP
kubectl get nodes -o wide

# 访问应用
# 蓝绿部署应用: http://<NODE_IP>:30080
# 金丝雀部署应用: http://<NODE_IP>:30081
# 用户分组灰度发布: http://<NODE_IP>:30082
# 地理位置灰度发布: http://<NODE_IP>:30083
```

### 5. 测试灰度发布

```bash
# 测试灰度发布功能
./deploy.sh test

# 普通用户访问
curl http://localhost:30082

# Beta用户访问 (会路由到灰度版本)
curl -H "X-Canary-User: beta" http://localhost:30082

# 内部用户访问
curl -H "X-Canary-User: internal" http://localhost:30082

# 设置地区Cookie访问地理位置灰度
curl -H "Cookie: user_region=华东" http://localhost:30083

# 通过URL参数指定地区
curl "http://localhost:30083?region=华南"
```

## 📊 监控和观察

### 使用 kubectl 监控

```bash
# 实时查看rollout状态
kubectl get rollouts -w

# 查看rollout详细信息
kubectl describe rollout nginx-rollout

# 查看rollout历史
kubectl rollout history rollout/nginx-rollout
```

### 使用 Argo Rollouts CLI (可选)

```bash
# 安装Argo Rollouts CLI
curl -LO https://github.com/argoproj/argo-rollouts/releases/latest/download/kubectl-argo-rollouts-darwin-amd64
chmod +x ./kubectl-argo-rollouts-darwin-amd64
sudo mv ./kubectl-argo-rollouts-darwin-amd64 /usr/local/bin/kubectl-argo-rollouts

# 查看rollout状态
kubectl argo rollouts get rollout nginx-rollout

# 监控rollout进度
kubectl argo rollouts get rollout nginx-rollout --watch
```

## 🔧 部署策略说明

### 蓝绿部署 (Blue-Green)

- **优势**: 零停机时间，快速回滚
- **流程**: 
  1. 部署新版本到预发布环境
  2. 运行健康检查和分析
  3. 手动或自动切换流量
  4. 旧版本保留一段时间后清理

### 金丝雀部署 (Canary)

- **优势**: 渐进式验证，风险可控
- **流程**:
  1. 20% 流量到新版本，暂停等待手动确认
  2. 40% 流量，自动等待10秒
  3. 60% 流量，自动等待10秒
  4. 80% 流量，自动等待10秒
  5. 100% 流量，完成部署

### 灰度发布 (Gray Release)

- **优势**: 基于用户特征的精准发布，业务风险最小
- **流程**:
  1. 识别目标用户群体（Beta用户、内部用户等）
  2. 仅对目标用户开放新版本
  3. 收集用户反馈和业务指标
  4. 逐步扩大用户范围
  5. 最终全量发布

### 地理位置灰度发布 (Geographic Gray Release)

- **优势**: 按地区逐步推进，降低区域性风险
- **流程**:
  1. 开发测试环境验证 (5%)
  2. 华东地区用户 (20%)
  3. 华南地区用户 (40%)
  4. 华北华中地区用户 (70%)
  5. 西部地区和全国用户 (100%)

## 🧹 清理资源

```bash
# 清理所有资源
./deploy.sh cleanup

# 或者手动清理
kubectl delete -f nginx-rollout.yaml
kubectl delete -f canary-rollout.yaml
kubectl delete -f analysis-template.yaml
```

## 🔍 故障排除

### 常见问题

1. **Rollout 卡在某个步骤**
   ```bash
   # 检查分析任务状态
   kubectl get analysisruns
   
   # 手动推进
   ./deploy.sh promote <rollout-name>
   ```

2. **服务无法访问**
   ```bash
   # 检查服务和端点
   kubectl get svc,ep
   
   # 检查节点端口是否开放
   kubectl get svc -o wide
   ```

3. **Argo Rollouts 控制器未运行**
   ```bash
   # 检查控制器状态
   kubectl get pods -n argo-rollouts
   
   # 重新安装
   ./deploy.sh deploy
   ```

## 📚 参考资料

- [Argo Rollouts 官方文档](https://argoproj.github.io/argo-rollouts/)
- [Kubernetes 渐进式交付指南](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/#rolling-update-deployment)
- [蓝绿部署 vs 金丝雀部署](https://argoproj.github.io/argo-rollouts/concepts/#deployment-strategies) 