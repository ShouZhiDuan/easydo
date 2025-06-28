# GitLab Runner 配置指南

> 为Istio项目配置GitLab Runner，支持Kubernetes部署和Docker构建

## 🎯 概述

GitLab CI/CD需要Runner来执行Pipeline中的Jobs。对于我们的Istio项目，需要配置能够：
- 构建Docker镜像
- 访问Kubernetes集群
- 执行kubectl和argo rollouts命令
- 支持并行构建

## 🏗️ Runner类型选择

### 1. Kubernetes Runner（推荐生产环境）

**优势**：
- ✅ 资源隔离性好
- ✅ 可扩展性强
- ✅ 与K8s集群集成简单
- ✅ 支持多并发任务

**适用场景**：生产环境、大团队

### 2. Docker Runner（适合开发环境）

**优势**：
- ✅ 配置简单
- ✅ 本地测试方便
- ✅ 资源占用相对较少

**适用场景**：开发测试、小团队

### 3. Shell Runner（最基础）

**优势**：
- ✅ 配置最简单
- ✅ 调试方便

**缺点**：
- ❌ 隔离性差
- ❌ 依赖宿主机环境

## 🚀 Kubernetes Runner 配置（推荐）

### 步骤1：安装GitLab Runner

```bash
# 添加GitLab官方Helm仓库
helm repo add gitlab https://charts.gitlab.io
helm repo update

# 创建namespace
kubectl create namespace gitlab-runner

# 获取Registration Token
# 在GitLab项目的 Settings -> CI/CD -> Runners 中获取
```

### 步骤2：创建配置文件

```yaml
# gitlab-runner-values.yaml
gitlabUrl: https://gitlab.example.com/  # 替换为你的GitLab URL

runnerRegistrationToken: "your-registration-token"  # 替换为实际Token

# Runner配置
runners:
  config: |
    [[runners]]
      [runners.kubernetes]
        namespace = "gitlab-runner"
        image = "ubuntu:20.04"
        
        # 支持特权模式（Docker in Docker需要）
        privileged = true
        
        # 资源限制
        cpu_limit = "2"
        memory_limit = "4Gi"
        cpu_request = "500m"
        memory_request = "1Gi"
        
        # 服务账户配置
        service_account = "gitlab-runner"
        
        # Volume挂载
        [[runners.kubernetes.volumes.host_path]]
          name = "docker-sock"
          mount_path = "/var/run/docker.sock"
          host_path = "/var/run/docker.sock"
        
        [[runners.kubernetes.volumes.config_map]]
          name = "kubeconfig"
          mount_path = "/root/.kube"
          
        # 节点选择器（可选）
        [runners.kubernetes.node_selector]
          "gitlab-runner" = "true"

# RBAC配置
rbac:
  create: true
  rules:
    - apiGroups: [""]
      resources: ["pods", "pods/exec", "pods/log", "services", "secrets"]
      verbs: ["get", "list", "watch", "create", "patch", "delete"]
    - apiGroups: ["apps"]
      resources: ["deployments", "replicasets"]
      verbs: ["get", "list", "watch", "create", "patch", "delete"]
    - apiGroups: ["networking.istio.io"]
      resources: ["virtualservices", "destinationrules", "gateways"]
      verbs: ["get", "list", "watch", "create", "patch", "delete"]
    - apiGroups: ["argoproj.io"]
      resources: ["rollouts"]
      verbs: ["get", "list", "watch", "create", "patch", "delete"]

# Service Account
serviceAccount:
  create: true
  name: gitlab-runner
```

### 步骤3：部署Runner

```bash
# 部署GitLab Runner
helm install gitlab-runner gitlab/gitlab-runner \
  -n gitlab-runner \
  -f gitlab-runner-values.yaml

# 验证部署
kubectl get pods -n gitlab-runner
kubectl logs -n gitlab-runner deployment/gitlab-runner
```

### 步骤4：配置Kubeconfig访问

```bash
# 创建用于CI/CD的ServiceAccount
kubectl create serviceaccount cicd-deployer -n istio-demo

# 绑定ClusterRole
kubectl create clusterrolebinding cicd-deployer-binding \
  --clusterrole=cluster-admin \
  --serviceaccount=istio-demo:cicd-deployer

# 获取Token
TOKEN=$(kubectl get secret $(kubectl get sa cicd-deployer -n istio-demo -o jsonpath='{.secrets[0].name}') -n istio-demo -o jsonpath='{.data.token}' | base64 -d)

# 创建ConfigMap存储kubeconfig
kubectl create configmap kubeconfig-staging \
  --from-literal=config="
apiVersion: v1
kind: Config
clusters:
- name: staging
  cluster:
    server: https://your-k8s-api-server:6443
    certificate-authority-data: $(kubectl config view --raw -o json | jq -r '.clusters[0].cluster."certificate-authority-data"')
contexts:
- name: staging
  context:
    cluster: staging
    user: cicd-deployer
current-context: staging
users:
- name: cicd-deployer
  user:
    token: $TOKEN
" -n gitlab-runner
```

## 🐳 Docker Runner 配置（开发环境）

### 步骤1：安装Docker Runner

```bash
# 在目标机器上安装GitLab Runner
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.deb.sh | sudo bash
sudo apt-get install gitlab-runner

# 或者使用Docker方式
docker run -d --name gitlab-runner --restart always \
  -v /srv/gitlab-runner/config:/etc/gitlab-runner \
  -v /var/run/docker.sock:/var/run/docker.sock \
  gitlab/gitlab-runner:latest
```

### 步骤2：注册Runner

```bash
# 交互式注册
gitlab-runner register

# 非交互式注册
gitlab-runner register \
  --non-interactive \
  --url "https://gitlab.example.com/" \
  --registration-token "your-token" \
  --executor "docker" \
  --docker-image "alpine:latest" \
  --description "docker-runner" \
  --tag-list "docker,build" \
  --docker-privileged=true \
  --docker-volumes="/var/run/docker.sock:/var/run/docker.sock"
```

### 步骤3：配置config.toml

```toml
# /etc/gitlab-runner/config.toml
concurrent = 4
check_interval = 0

[session_server]
  session_timeout = 1800

[[runners]]
  name = "docker-runner"
  url = "https://gitlab.example.com/"
  token = "your-runner-token"
  executor = "docker"
  [runners.custom_build_dir]
  [runners.cache]
    [runners.cache.s3]
    [runners.cache.gcs]
    [runners.cache.azure]
  [runners.docker]
    tls_verify = false
    image = "alpine:latest"
    privileged = true
    disable_entrypoint_overwrite = false
    oom_kill_disable = false
    disable_cache = false
    volumes = ["/var/run/docker.sock:/var/run/docker.sock", "/cache"]
    shm_size = 0
```

## 🔧 环境变量配置

在GitLab项目的 **Settings -> CI/CD -> Variables** 中配置：

### 必需变量

| 变量名 | 值 | 类型 | 描述 |
|--------|----|----- |------|
| `CI_REGISTRY` | `registry.example.com` | Variable | Docker镜像仓库地址 |
| `CI_REGISTRY_USER` | `gitlab-ci-token` | Variable | 镜像仓库用户名 |
| `CI_REGISTRY_PASSWORD` | `your-token` | Masked | 镜像仓库密码 |
| `KUBE_CONTEXT_STAGING` | `staging` | Variable | Staging环境K8s上下文 |
| `KUBE_CONTEXT_PRODUCTION` | `production` | Variable | 生产环境K8s上下文 |

### 可选变量（通知）

| 变量名 | 值 | 类型 | 描述 |
|--------|----|----- |------|
| `SLACK_WEBHOOK_URL` | `https://hooks.slack.com/...` | Masked | Slack通知URL |
| `WECHAT_WEBHOOK_URL` | `https://qyapi.weixin.qq.com/...` | Masked | 企业微信通知URL |

## 📋 Runner标签配置

为不同类型的作业配置适当的标签：

```yaml
# 在.gitlab-ci.yml中使用标签
build:
  stage: build
  tags:
    - docker
    - build
  # ...

deploy-production-auto:
  stage: deploy-production
  tags:
    - kubernetes
    - deploy
  # ...
```

## 🔒 安全配置

### 1. 网络安全

```yaml
# 限制Runner网络访问
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: gitlab-runner-netpol
  namespace: gitlab-runner
spec:
  podSelector:
    matchLabels:
      app: gitlab-runner
  policyTypes:
  - Egress
  egress:
  - to: []
    ports:
    - protocol: TCP
      port: 443  # GitLab HTTPS
    - protocol: TCP
      port: 6443 # Kubernetes API
```

### 2. 镜像安全

```bash
# 在Runner中集成镜像安全扫描
script:
  - trivy image --exit-code 1 --severity HIGH,CRITICAL $IMAGE_NAME
```

### 3. 秘钥管理

```yaml
# 使用Kubernetes Secrets
apiVersion: v1
kind: Secret
metadata:
  name: registry-credentials
  namespace: gitlab-runner
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: base64-encoded-config
```

## 🚨 故障排查

### 常见问题1：Runner无法连接GitLab

```bash
# 检查网络连接
kubectl exec -n gitlab-runner deployment/gitlab-runner -- \
  curl -I https://gitlab.example.com

# 检查DNS解析
kubectl exec -n gitlab-runner deployment/gitlab-runner -- \
  nslookup gitlab.example.com
```

### 常见问题2：Docker构建失败

```bash
# 检查Docker权限
kubectl logs -n gitlab-runner deployment/gitlab-runner

# 验证特权模式
kubectl describe pod -n gitlab-runner -l app=gitlab-runner
```

### 常见问题3：Kubernetes访问权限

```bash
# 验证ServiceAccount权限
kubectl auth can-i create deployments --as=system:serviceaccount:gitlab-runner:gitlab-runner

# 检查RBAC配置
kubectl get clusterrolebinding | grep gitlab-runner
```

## 📊 监控Runner状态

### 1. GitLab UI监控

访问 **Admin Area -> Overview -> Runners** 查看：
- Runner状态
- 作业执行情况
- 资源使用统计

### 2. Kubernetes监控

```bash
# 查看Runner Pod状态
kubectl get pods -n gitlab-runner

# 查看资源使用
kubectl top pods -n gitlab-runner

# 查看事件
kubectl get events -n gitlab-runner
```

### 3. Prometheus监控

```yaml
# 配置ServiceMonitor
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: gitlab-runner
spec:
  selector:
    matchLabels:
      app: gitlab-runner
  endpoints:
  - port: metrics
```

## 🎯 性能优化

### 1. 并发配置

```toml
# config.toml
concurrent = 10  # 根据资源情况调整

[[runners]]
  limit = 5  # 单个Runner的并发限制
```

### 2. 缓存优化

```yaml
# 使用分布式缓存
cache:
  type: s3
  s3:
    server_address: minio.example.com
    bucket_name: gitlab-runner-cache
```

### 3. 镜像优化

```dockerfile
# 使用多阶段构建减少镜像大小
FROM maven:3.9-openjdk-17 AS builder
COPY . .
RUN mvn package

FROM openjdk:17-jre-slim
COPY --from=builder target/*.jar app.jar
```

## 🚀 快速验证

配置完成后，执行以下步骤验证：

1. **推送测试代码**
   ```bash
   echo "# Test" >> README.md
   git add .
   git commit -m "test: trigger CI/CD"
   git push origin main
   ```

2. **检查Pipeline状态**
   - 访问GitLab项目的 **CI/CD -> Pipelines**
   - 确认所有阶段正常执行

3. **验证部署结果**
   ```bash
   kubectl get rollouts -n istio-demo
   kubectl get pods -n istio-demo
   ```

## 💡 最佳实践总结

✅ **使用Kubernetes Runner**：更好的隔离性和扩展性  
✅ **配置适当的资源限制**：避免资源竞争  
✅ **启用缓存**：加速构建过程  
✅ **使用标签管理**：精确控制作业分发  
✅ **配置监控告警**：及时发现问题  
✅ **定期更新Runner版本**：获得最新功能和安全修复  

通过这套配置，您的GitLab CI/CD流水线就可以完整运行，实现从代码提交到生产部署的全自动化流程！ 