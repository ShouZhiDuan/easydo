# Argo Rollouts 快速入门教程

本文档将引导您完成 Argo Rollouts 的基本使用流程，包括环境准备、部署、金丝雀更新、手动验证、中止回滚等核心操作。

## 一、 前提条件

在开始之前，请确保您的环境满足以下要求。本教程将以 **Linux** 环境为例。

### 1. 准备一个 Kubernetes 集群

如果您没有可用的 Kubernetes 集群，可以使用 `minikube` 或 `kind` 在本地快速创建一个。

*   **选项一：使用 Minikube**
    ```bash
    # 启动一个 minikube 集群
    minikube start
    ```

*   **选项二：使用 Kind**
    ```bash
    # 创建一个 kind 集群
    kind create cluster
    ```

### 2. 安装 Argo Rollouts 控制器

Argo Rollouts 控制器是运行在集群中的核心组件，负责管理 `Rollout` 资源的生命周期。

```bash
# 1. 为 Argo Rollouts 创建一个独立的命名空间
kubectl create namespace argo-rollouts

# 2. 在该命名空间中安装 Argo Rollouts 控制器
kubectl apply -n argo-rollouts -f https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml
```

### 3. 安装 kubectl 的 Argo Rollouts 插件 (Linux)

此插件极大地增强了 `kubectl`，使其能够方便地与 `Rollout` 资源交互。

```bash
# 1. 从 GitHub Releases 页面下载最新版本的 Linux 插件
# 注意：本处下载适用于 amd64 架构
curl -sLO https://github.com/argoproj/argo-rollouts/releases/latest/download/kubectl-argo-rollouts-linux-amd64

# 2. 为下载的二进制文件授予执行权限
chmod +x ./kubectl-argo-rollouts-linux-amd64

# 3. 将二进制文件移动到您的系统 PATH 路径下，以便全局调用
#    注意：此命令通常需要 sudo 权限
sudo mv ./kubectl-argo-rollouts-linux-amd64 /usr/local/bin/kubectl-argo-rollouts

# 4. 验证插件是否安装成功
kubectl argo rollouts version
```

---

## 二、 完整操作步骤

现在，您可以按照以下步骤，端到端地体验 Argo Rollouts 的核心功能。

### 第1步：部署初始应用

我们首先部署一个 `Rollout` 资源和一个对应的 `Service`。该 `Rollout` 已预先配置了金丝雀发布策略。

```bash
# 部署 Rollout 资源 (定义了发布策略和应用模板)
kubectl apply -f https://raw.githubusercontent.com/argoproj/argo-rollouts/master/docs/getting-started/basic/rollout.yaml

# 部署 Service 资源 (用于将流量路由到应用)
kubectl apply -f https://raw.githubusercontent.com/argoproj/argo-rollouts/master/docs/getting-started/basic/service.yaml

# (推荐) 实时观察 Rollout 的状态，等待其变为 Healthy
# 初次部署会直接完成，并跳过所有金丝雀发布步骤
echo "⏳ 等待初始版本部署完成..."
kubectl argo rollouts get rollout rollouts-demo --watch
```

### 第2步：执行第一次金丝雀更新 (Blue -> Yellow)

接下来，我们将应用从初始的 "blue" 版本更新到 "yellow" 版本，触发一次金丝雀发布。

```bash
# 使用插件命令更新镜像
kubectl argo rollouts set image rollouts-demo rollouts-demo=argoproj/rollouts-demo:yellow

# 实时观察发布过程。应用会先部署一个 canary Pod (占20%的副本)
# 然后发布会自动进入 Paused (暂停) 状态，等待人工审核
echo "⏳ 应用正在更新到 yellow 版本，发布将暂停在金丝雀步骤..."
kubectl argo rollouts get rollout rollouts-demo --watch
```

### 第3步：手动推进 (Promote) 更新

当 `Rollout` 处于 `Paused` 状态时，意味着我们可以对新版本进行验证。确认无误后，手动"推进"发布。

```bash
# 手动确认并推进发布流程
kubectl argo rollouts promote rollouts-demo

# 再次观察状态。Rollout 会自动完成后续的发布步骤，
# 直到所有 Pod 都更新为 "yellow" 版本，并最终变为 Healthy
echo "✅ 手动确认完毕，发布将继续进行直到完成..."
kubectl argo rollouts get rollout rollouts-demo --watch
```

### 第4步：模拟失败更新并中止回滚 (Yellow -> Red -> Abort)

现在，我们模拟一次失败的发布。我们尝试更新到 "red" 版本，但在金丝雀阶段"发现问题"，并手动"中止"发布，使其安全地回滚到上一个稳定的 "yellow" 版本。

```bash
# 更新到 "red" 版本，这将再次触发金丝雀发布并暂停
kubectl argo rollouts set image rollouts-demo rollouts-demo=argoproj/rollouts-demo:red

# 观察状态，等待其再次进入 Paused 状态
echo "⏳ 应用正在更新到 red 版本，将再次暂停在金丝雀步骤..."
kubectl argo rollouts get rollout rollouts-demo --watch

# 此时，我们模拟发现 "red" 版本存在问题，决定中止发布
kubectl argo rollouts abort rollouts-demo

# 观察状态。Rollout 会自动缩容 "red" 版本的 Pod，并扩容回 "yellow" 版本。
# 注意，此时 Rollout 状态会变为 Degraded (降级)，因为它期望的是 red，但实际运行的是 yellow
echo "🚨 发布已中止，正在回滚到 yellow 版本..."
kubectl argo rollouts get rollout rollouts-demo --watch
```

### 第5步：将 Rollout 恢复至健康状态

尽管应用已回滚，但 `Rollout` 资源本身因为期望状态与实际运行状态不符，被标记为 `Degraded`。我们需要将它的期望状态也重置为 "yellow" 版本，使其恢复 `Healthy` 状态。

```bash
# 将期望的镜像版本重新设置为稳定的 "yellow" 版本
kubectl argo rollouts set image rollouts-demo rollouts-demo=argoproj/rollouts-demo:yellow

# 再次观察状态。由于期望状态和实际状态达成一致，Rollout 会立刻恢复为 Healthy
echo "🛠️ 重置期望状态，Rollout 将恢复 Healthy..."
kubectl argo rollouts get rollout rollouts-demo --watch
```

---

恭喜！您已成功完成了 Argo Rollouts 的基础操作。要实现更高级的流量管理（如基于权重的流量切分），您需要将其与 Ingress 控制器或服务网格（如 Istio）集成。 