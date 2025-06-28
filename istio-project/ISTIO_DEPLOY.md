# Istio 生产环境安装部署完整教程

## 目录
1. [系统要求](#系统要求)
2. [预备环境准备](#预备环境准备)
3. [Kubernetes集群部署](#kubernetes集群部署)
4. [Istio安装](#istio安装)
5. [生产环境配置](#生产环境配置)
6. [安全配置](#安全配置)
7. [监控和可观测性](#监控和可观测性)
8. [高可用配置](#高可用配置)
9. [性能优化](#性能优化)
10. [故障排查](#故障排查)
11. [升级和维护](#升级和维护)

## 系统要求

### 硬件要求
- **Master节点**: 4 CPU, 8GB RAM, 100GB SSD
- **Worker节点**: 8 CPU, 16GB RAM, 200GB SSD
- **最小集群**: 3个Master节点 + 3个Worker节点

### 软件要求
- **操作系统**: Ubuntu 20.04 LTS 或 22.04 LTS
- **Kubernetes**: v1.26+ 
- **Istio**: v1.19+ (推荐最新稳定版)
- **容器运行时**: containerd v1.6+

### 网络要求
- 节点间互联网络: 10Gbps (推荐)
- 互联网访问: 用于拉取镜像
- 端口开放: 见详细端口清单

## 预备环境准备

### 1. 系统初始化

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装必要工具
sudo apt install -y curl wget vim git htop net-tools

# 配置时区
sudo timedatectl set-timezone Asia/Shanghai

# 关闭swap
sudo swapoff -a
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab

# 配置内核参数
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1
EOF

sudo sysctl --system
```

### 2. 安装Docker和containerd

```bash
# 卸载旧版本
sudo apt remove -y docker docker-engine docker.io containerd runc

# 安装依赖
sudo apt install -y apt-transport-https ca-certificates curl gnupg lsb-release

# 添加Docker官方GPG key
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 添加Docker仓库
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装Docker Engine
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io

# 配置containerd
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml

# 修改containerd配置使用systemd cgroup driver
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml

# 重启containerd
sudo systemctl restart containerd
sudo systemctl enable containerd

# 配置Docker daemon
sudo mkdir -p /etc/docker
cat <<EOF | sudo tee /etc/docker/daemon.json
{
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  },
  "storage-driver": "overlay2"
}
EOF

sudo systemctl restart docker
sudo systemctl enable docker
```

## Kubernetes集群部署

### 1. 安装kubeadm, kubelet, kubectl

```bash
# 添加Kubernetes APT仓库
curl -fsSL https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo gpg --dearmor -o /usr/share/keyrings/kubernetes-archive-keyring.gpg

echo "deb [signed-by=/usr/share/keyrings/kubernetes-archive-keyring.gpg] https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee /etc/apt/sources.list.d/kubernetes.list

# 安装kubelet kubeadm kubectl
sudo apt update
sudo apt install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

# 启用kubelet
sudo systemctl enable kubelet
```

### 2. 初始化Master节点

```bash
# 初始化第一个Master节点
sudo kubeadm init \
  --pod-network-cidr=10.244.0.0/16 \
  --service-cidr=10.96.0.0/12 \
  --apiserver-advertise-address=<MASTER_IP> \
  --control-plane-endpoint=<LOAD_BALANCER_IP>:6443 \
  --upload-certs

# 配置kubectl
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# 安装网络插件 (Flannel)
kubectl apply -f https://raw.githubusercontent.com/flannel-io/flannel/master/Documentation/kube-flannel.yml
```

### 3. 添加其他Master节点

```bash
# 在其他Master节点上执行
sudo kubeadm join <LOAD_BALANCER_IP>:6443 \
  --token <TOKEN> \
  --discovery-token-ca-cert-hash sha256:<HASH> \
  --control-plane \
  --certificate-key <CERTIFICATE_KEY>
```

### 4. 添加Worker节点

```bash
# 在Worker节点上执行
sudo kubeadm join <LOAD_BALANCER_IP>:6443 \
  --token <TOKEN> \
  --discovery-token-ca-cert-hash sha256:<HASH>
```

## Istio安装

### 1. 下载Istio

```bash
# 下载最新版本的Istio
curl -L https://istio.io/downloadIstio | sh -

# 或者下载指定版本
curl -L https://istio.io/downloadIstio | ISTIO_VERSION=1.19.0 sh -

# 进入Istio目录
cd istio-1.19.0

# 添加istioctl到PATH
export PATH=$PWD/bin:$PATH
echo 'export PATH=$PWD/bin:$PATH' >> ~/.bashrc
```

### 2. 预检查

```bash
# 检查集群是否准备好安装Istio
istioctl x precheck
```

### 3. 安装Istio

```bash
# 生产环境推荐使用demo配置作为基础，然后进行定制
istioctl install --set values.defaultRevision=default

# 或者使用自定义配置文件
istioctl install -f istio-production.yaml
```

### 4. 创建生产环境配置文件

```yaml
# istio-production.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
metadata:
  name: istio-production
spec:
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster1
      network: network1
    pilot:
      env:
        EXTERNAL_ISTIOD: false
        PILOT_ENABLE_WORKLOAD_ENTRY_AUTOREGISTRATION: true
        PILOT_ENABLE_CROSS_CLUSTER_WORKLOAD_ENTRY: true
        PILOT_TRACE_SAMPLING: 1.0
  components:
    pilot:
      k8s:
        resources:
          requests:
            cpu: 500m
            memory: 2048Mi
          limits:
            cpu: 1000m
            memory: 4096Mi
        hpaSpec:
          maxReplicas: 5
          minReplicas: 2
          scaleTargetRef:
            apiVersion: apps/v1
            kind: Deployment
            name: istiod
          metrics:
          - type: Resource
            resource:
              name: cpu
              target:
                type: Utilization
                averageUtilization: 80
    ingressGateways:
    - name: istio-ingressgateway
      enabled: true
      k8s:
        resources:
          requests:
            cpu: 100m
            memory: 128Mi
          limits:
            cpu: 2000m
            memory: 1024Mi
        hpaSpec:
          maxReplicas: 5
          minReplicas: 2
          scaleTargetRef:
            apiVersion: apps/v1
            kind: Deployment
            name: istio-ingressgateway
          metrics:
          - type: Resource
            resource:
              name: cpu
              target:
                type: Utilization
                averageUtilization: 80
        service:
          type: LoadBalancer
          ports:
          - port: 15021
            targetPort: 15021
            name: status-port
          - port: 80
            targetPort: 8080
            name: http2
          - port: 443
            targetPort: 8443
            name: https
    egressGateways:
    - name: istio-egressgateway
      enabled: true
      k8s:
        resources:
          requests:
            cpu: 100m
            memory: 128Mi
          limits:
            cpu: 2000m
            memory: 1024Mi
```

### 5. 验证安装

```bash
# 检查Istio组件状态
kubectl get pods -n istio-system

# 检查Istio版本
istioctl version

# 验证安装
istioctl verify-install
```

## 生产环境配置

### 1. 启用Sidecar自动注入

```bash
# 为命名空间启用自动注入
kubectl label namespace default istio-injection=enabled

# 或者使用修订版本标签
kubectl label namespace default istio.io/rev=default
```

### 2. 配置资源限制

```yaml
# sidecar-resources.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: istio-sidecar-injector
  namespace: istio-system
data:
  config: |
    policy: enabled
    alwaysInjectSelector:
      []
    neverInjectSelector:
      []
    injectedAnnotations:
      "sidecar.istio.io/proxyCPU": "100m"
      "sidecar.istio.io/proxyMemory": "128Mi"
      "sidecar.istio.io/proxyCPULimit": "200m"
      "sidecar.istio.io/proxyMemoryLimit": "256Mi"
    template: |
      rewriteAppHTTPProbers: true
      # ... 其他配置
```

### 3. 配置网关

```yaml
# production-gateway.yaml
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: production-gateway
  namespace: istio-system
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
    tls:
      httpsRedirect: true
  - port:
      number: 443
      name: https
      protocol: HTTPS
    tls:
      mode: SIMPLE
      credentialName: production-tls
    hosts:
    - "*"
```

## 安全配置

### 1. 启用mTLS

```yaml
# mtls-policy.yaml
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: istio-system
spec:
  mtls:
    mode: STRICT
```

### 2. 配置授权策略

```yaml
# auth-policy.yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: default-deny
  namespace: istio-system
spec:
  {}
---
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-ingress
  namespace: istio-system
spec:
  selector:
    matchLabels:
      app: istio-ingressgateway
  action: ALLOW
  rules:
  - from:
    - source:
        principals: ["cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account"]
```

### 3. 配置JWT认证

```yaml
# jwt-auth.yaml
apiVersion: security.istio.io/v1beta1
kind: RequestAuthentication
metadata:
  name: jwt-auth
  namespace: production
spec:
  jwtRules:
  - issuer: "https://your-issuer.com"
    jwksUri: "https://your-issuer.com/.well-known/jwks.json"
    audiences:
    - "your-audience"
```

## 监控和可观测性

### 1. 安装Prometheus

```bash
# 使用Istio提供的Prometheus配置
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.19/samples/addons/prometheus.yaml
```

### 2. 安装Grafana

```bash
# 安装Grafana
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.19/samples/addons/grafana.yaml

# 访问Grafana Dashboard
kubectl port-forward -n istio-system svc/grafana 3000:3000
```

### 3. 安装Jaeger

```bash
# 安装Jaeger
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.19/samples/addons/jaeger.yaml
```

### 4. 安装Kiali

```bash
# 安装Kiali
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.19/samples/addons/kiali.yaml

# 访问Kiali Dashboard
kubectl port-forward -n istio-system svc/kiali 20001:20001
```

### 5. 配置监控告警

```yaml
# prometheus-rules.yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: istio-alerts
  namespace: istio-system
spec:
  groups:
  - name: istio.rules
    rules:
    - alert: IstioHighRequestLatency
      expr: histogram_quantile(0.99, sum(rate(istio_request_duration_milliseconds_bucket[5m])) by (le)) > 1000
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: "High request latency detected"
        description: "99th percentile latency is above 1000ms"
    - alert: IstioHighErrorRate
      expr: sum(rate(istio_requests_total{response_code!~"2.."}[5m])) / sum(rate(istio_requests_total[5m])) > 0.1
      for: 5m
      labels:
        severity: critical
      annotations:
        summary: "High error rate detected"
        description: "Error rate is above 10%"
```

## 高可用配置

### 1. 多区域部署

```yaml
# multi-zone-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: istiod
  namespace: istio-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: istiod
  template:
    spec:
      affinity:
        podAntiAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
          - labelSelector:
              matchLabels:
                app: istiod
            topologyKey: topology.kubernetes.io/zone
```

### 2. 配置外部负载均衡器

```yaml
# external-lb.yaml
apiVersion: v1
kind: Service
metadata:
  name: istio-ingressgateway-external
  namespace: istio-system
spec:
  type: LoadBalancer
  selector:
    istio: ingressgateway
  ports:
  - name: http2
    port: 80
    targetPort: 8080
  - name: https
    port: 443
    targetPort: 8443
  externalTrafficPolicy: Local
  sessionAffinity: ClientIP
```

### 3. 配置集群间通信

```yaml
# cross-cluster-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: cacerts
  namespace: istio-system
type: Opaque
data:
  root-cert.pem: <base64-encoded-root-cert>
  cert-chain.pem: <base64-encoded-cert-chain>
  ca-cert.pem: <base64-encoded-ca-cert>
  ca-key.pem: <base64-encoded-ca-key>
```

## 性能优化

### 1. 调整资源配置

```yaml
# performance-tuning.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
metadata:
  name: performance-tuning
spec:
  values:
    global:
      proxy:
        resources:
          requests:
            cpu: 100m
            memory: 128Mi
          limits:
            cpu: 500m
            memory: 512Mi
    pilot:
      env:
        PILOT_PUSH_THROTTLE: 100
        PILOT_MAX_REQUESTS_PER_SECOND: 25
```

### 2. 优化网络配置

```bash
# 调整内核参数
cat <<EOF | sudo tee /etc/sysctl.d/istio-performance.conf
net.core.rmem_max = 16777216
net.core.wmem_max = 16777216
net.ipv4.tcp_rmem = 4096 87380 16777216
net.ipv4.tcp_wmem = 4096 65536 16777216
net.core.netdev_max_backlog = 5000
net.ipv4.tcp_window_scaling = 1
EOF

sudo sysctl --system
```

### 3. 配置连接池

```yaml
# connection-pool.yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: production-connection-pool
spec:
  host: "*.local"
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
        connectTimeout: 30s
        keepAlive:
          time: 7200s
          interval: 75s
      http:
        http1MaxPendingRequests: 10
        http2MaxRequests: 100
        maxRequestsPerConnection: 10
        maxRetries: 3
        consecutiveGatewayErrors: 5
        interval: 30s
        baseEjectionTime: 30s
```

## 故障排查

### 1. 常用诊断命令

```bash
# 检查Istio组件状态
kubectl get pods -n istio-system

# 检查代理配置
istioctl proxy-config cluster <pod-name> -n <namespace>

# 检查服务发现
istioctl proxy-config endpoints <pod-name> -n <namespace>

# 检查路由配置
istioctl proxy-config routes <pod-name> -n <namespace>

# 检查监听器配置
istioctl proxy-config listeners <pod-name> -n <namespace>

# 分析配置
istioctl analyze

# 查看代理日志
kubectl logs -f <pod-name> -c istio-proxy -n <namespace>
```

### 2. 常见问题解决

#### 问题1: Pod启动失败
```bash
# 检查sidecar注入
kubectl get pods -o yaml | grep -A 10 -B 10 "istio-proxy"

# 检查资源限制
kubectl describe pod <pod-name> -n <namespace>
```

#### 问题2: 服务间通信失败
```bash
# 检查服务发现
istioctl proxy-config endpoints <pod-name> -n <namespace>

# 检查策略配置
kubectl get peerauthentication,authorizationpolicy -A

# 测试连通性
kubectl exec -it <pod-name> -c istio-proxy -- curl -v http://target-service:port
```

### 3. 性能调试

```bash
# 启用详细日志
kubectl patch deployment istiod -n istio-system -p '{"spec":{"template":{"spec":{"containers":[{"name":"discovery","args":["discovery","--log_output_level=all:debug"]}]}}}}'

# 分析性能指标
kubectl port-forward -n istio-system svc/prometheus 9090:9090
```

## 升级和维护

### 1. 升级准备

```bash
# 备份当前配置
kubectl get istiooperator -n istio-system -o yaml > istio-operator-backup.yaml

# 检查升级兼容性
istioctl x precheck
```

### 2. 金丝雀升级

```bash
# 安装新版本控制平面
istioctl install --set revision=1-19-1

# 标记命名空间使用新版本
kubectl label namespace production istio.io/rev=1-19-1 --overwrite

# 重启应用以使用新版本
kubectl rollout restart deployment -n production

# 验证升级
istioctl proxy-status

# 清理旧版本
istioctl x uninstall --revision=1-18-2
```

### 3. 维护任务

```bash
# 清理过期的配置
kubectl delete istiooperator -n istio-system <old-operator>

# 更新证书
kubectl create secret generic cacerts -n istio-system \
  --from-file=root-cert.pem \
  --from-file=cert-chain.pem \
  --from-file=ca-cert.pem \
  --from-file=ca-key.pem

# 定期检查
istioctl verify-install
```

## 安全最佳实践

### 1. 证书管理
- 使用自动化证书管理工具如cert-manager
- 定期轮换根证书和中间证书
- 监控证书过期时间

### 2. 访问控制
- 实施最小权限原则
- 使用命名空间隔离
- 定期审计访问策略

### 3. 网络安全
- 启用严格的mTLS模式
- 配置出口网关控制外部访问
- 使用网络策略限制Pod间通信

## 监控指标

### 关键指标
- **延迟**: 请求响应时间
- **错误率**: 4xx/5xx错误百分比
- **吞吐量**: 每秒请求数
- **资源使用**: CPU/内存使用率

### 告警规则
- 高延迟告警 (>1000ms)
- 高错误率告警 (>5%)
- 资源使用告警 (>80%)
- 证书过期告警 (<30天)

## 总结

本教程涵盖了Istio生产环境的完整部署流程，包括系统准备、Kubernetes集群搭建、Istio安装配置、安全设置、监控部署和维护升级等各个方面。

在实际部署时，请根据具体的业务需求和环境特点调整配置参数，并建议在测试环境中充分验证后再部署到生产环境。

定期进行安全审计、性能监控和系统维护，确保Istio服务网格在生产环境中稳定高效运行。 