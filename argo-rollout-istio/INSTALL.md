# Istio 与 Bookinfo 应用安装指南

本指南将帮助您在 Kubernetes 集群中安装 Istio 服务网格和 Bookinfo 示例应用程序。

## 目录
- [前置条件](#前置条件)
- [安装 Istio](#安装-istio)
- [部署示例应用](#部署示例应用)
- [配置流量管理](#配置流量管理)
- [安装可观测性工具](#安装可观测性工具)
- [验证安装](#验证安装)
- [故障排除](#故障排除)

## 前置条件

在开始之前，请确保您的环境满足以下要求：

- 已安装并配置 `kubectl` 命令行工具
- 已安装 `istioctl` 命令行工具
- 可访问的 Kubernetes 集群（版本 1.27+）
- 集群具有足够的资源（至少 4 CPU 和 8GB 内存）

## 安装 Istio

### 1. 安装 Istio 控制平面

使用无网关配置安装 Istio：

```bash
istioctl install -f samples/bookinfo/demo-profile-no-gateways.yaml -y
```

安装成功后，您将看到以下输出：

```
        |\          
        | \         
        |  \        
        |   \       
      /||    \      
     / ||     \     
    /  ||      \    
   /   ||       \   
  /    ||        \  
 /     ||         \ 
/______||__________\
____________________
  \__       _____/  
     \_____/        

✔ Istio core installed ⛵️                                                                                                           
✔ Istiod installed 🧠                                                                                                                                     
✔ Installation complete 
```

### 2. 启用 Sidecar 自动注入

为 `default` 命名空间启用 Istio sidecar 自动注入：

```bash
kubectl label namespace default istio-injection=enabled
```

### 3. 安装 Kubernetes Gateway API CRD

安装 Kubernetes Gateway API 自定义资源定义：

```bash
kubectl get crd gateways.gateway.networking.k8s.io &> /dev/null || \
{ kubectl kustomize "github.com/kubernetes-sigs/gateway-api/config/crd?ref=v1.3.0" | kubectl apply -f -; }
```

成功安装后，您将看到：

```
customresourcedefinition.apiextensions.k8s.io/gatewayclasses.gateway.networking.k8s.io created
customresourcedefinition.apiextensions.k8s.io/gateways.gateway.networking.k8s.io created
customresourcedefinition.apiextensions.k8s.io/grpcroutes.gateway.networking.k8s.io created
customresourcedefinition.apiextensions.k8s.io/httproutes.gateway.networking.k8s.io created
customresourcedefinition.apiextensions.k8s.io/referencegrants.gateway.networking.k8s.io created
```

## 部署示例应用

### 1. 部署 Bookinfo 应用

部署 Bookinfo 示例应用程序：

```bash
kubectl apply -f samples/bookinfo/platform/kube/bookinfo.yaml
```

部署成功后，您将看到：

```
service/details created
serviceaccount/bookinfo-details created
deployment.apps/details-v1 created
service/ratings created
serviceaccount/bookinfo-ratings created
deployment.apps/ratings-v1 created
service/reviews created
serviceaccount/bookinfo-reviews created
deployment.apps/reviews-v1 created
deployment.apps/reviews-v2 created
deployment.apps/reviews-v3 created
service/productpage created
serviceaccount/bookinfo-productpage created
deployment.apps/productpage-v1 created
```

### 2. 验证应用部署

检查应用是否在集群内正常运行：

```bash
kubectl exec "$(kubectl get pod -l app=ratings -o jsonpath='{.items[0].metadata.name}')" -c ratings -- curl -sS productpage:9080/productpage | grep -o "<title>.*</title>"
```

如果应用运行正常，您应该看到页面标题输出。

## 配置流量管理

### 1. 创建网关

创建 Bookinfo 网关配置：

```bash
kubectl apply -f samples/bookinfo/gateway-api/bookinfo-gateway.yaml
```

成功创建后，您将看到：

```
gateway.gateway.networking.k8s.io/bookinfo-gateway created
httproute.gateway.networking.k8s.io/bookinfo created
```

### 2. 配置网关服务类型

设置网关服务为 ClusterIP 类型：

```bash
kubectl annotate gateway bookinfo-gateway networking.istio.io/service-type=ClusterIP --namespace=default
```

### 3. 验证网关状态

检查网关状态：

```bash
kubectl get gateway
```

您应该看到类似以下输出：

```
NAME               CLASS   ADDRESS                                            PROGRAMMED   AGE
bookinfo-gateway   istio   bookinfo-gateway-istio.default.svc.cluster.local   True         89s
```

## 访问应用程序

### 1. 端口转发

使用端口转发访问应用程序：

```bash
kubectl port-forward svc/bookinfo-gateway-istio 8080:80
```

### 2. 访问应用

打开浏览器访问：`http://localhost:8080/productpage`

您应该能看到 Bookinfo 应用的产品页面。

## 安装可观测性工具

### 1. 安装 Kiali 和其他插件

```bash
kubectl apply -f samples/addons
```

### 2. 等待 Kiali 部署完成

```bash
kubectl rollout status deployment/kiali -n istio-system
```

部署完成后，您将看到：

```
Waiting for deployment "kiali" rollout to finish: 0 of 1 updated replicas are available...
deployment "kiali" successfully rolled out
```

## 验证安装

### 1. 生成测试流量

生成一些测试流量以便在 Kiali 中查看：

```bash
for i in $(seq 1 100); do curl -s -o /dev/null "http://localhost:8080/productpage"; done
```

### 2. 访问 Kiali 仪表板

打开 Kiali 仪表板：

```bash
istioctl dashboard kiali
```

这将在您的默认浏览器中打开 Kiali 仪表板，您可以在其中查看服务网格的拓扑图和指标。

## 故障排除

### 常见问题

1. **Pod 无法启动**
   - 检查资源配额：`kubectl describe pod <pod-name>`
   - 验证镜像是否可用：`kubectl get events`

2. **Sidecar 未注入**
   - 确认命名空间标签：`kubectl get namespace default --show-labels`
   - 检查 Istio 控制平面状态：`kubectl get pods -n istio-system`

3. **网关无法访问**
   - 检查服务状态：`kubectl get svc`
   - 验证端口转发：`netstat -tlnp | grep 8080`

### 清理安装

如果需要清理安装，可以使用以下命令：

```bash
# 删除 Bookinfo 应用
kubectl delete -f samples/bookinfo/platform/kube/bookinfo.yaml

# 删除网关配置
kubectl delete -f samples/bookinfo/gateway-api/bookinfo-gateway.yaml

# 删除插件
kubectl delete -f samples/addons

# 卸载 Istio
istioctl uninstall --purge -y
```

## 下一步

安装完成后，您可以：

- 探索 [Istio 流量管理功能](https://istio.io/latest/docs/tasks/traffic-management/)
- 配置 [安全策略](https://istio.io/latest/docs/tasks/security/)
- 学习 [可观测性功能](https://istio.io/latest/docs/tasks/observability/)

如果遇到问题，请参考 [Istio 官方文档](https://istio.io/latest/docs/) 或提交 issue。




