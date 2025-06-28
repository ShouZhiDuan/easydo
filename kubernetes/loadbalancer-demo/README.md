# LoadBalancer Demo 示例

这是一个简单的 LoadBalancer 使用示例，展示如何部署一个 Web 应用并通过 LoadBalancer 对外提供服务。

## 📋 示例内容

### 1. 组件说明
- **Deployment**: 部署 3 个 Nginx Pod 副本
- **ConfigMap**: 自定义 HTML 页面，显示负载均衡效果
- **LoadBalancer Service**: 对外暴露服务

### 2. 文件结构
```
loadbalancer-demo/
├── simple-web-app.yaml    # 主要配置文件
├── test-commands.sh       # 自动化测试脚本
└── README.md             # 本说明文件
```

## 🚀 快速开始

### 方法1: 使用自动化脚本
```bash
# 进入demo目录
cd loadbalancer-demo

# 给脚本执行权限
chmod +x test-commands.sh

# 运行测试脚本
./test-commands.sh
```

### 方法2: 手动部署
```bash
# 1. 部署应用
kubectl apply -f simple-web-app.yaml

# 2. 查看部署状态
kubectl get all -l app=simple-web

# 3. 查看LoadBalancer状态
kubectl get svc simple-web-loadbalancer
```

## 🔍 预期结果

### LoadBalancer状态
```bash
kubectl get svc simple-web-loadbalancer
```

**可能的输出**：

#### 本地环境 (Docker Desktop/Minikube)
```
NAME                      TYPE           CLUSTER-IP     EXTERNAL-IP   PORT(S)
simple-web-loadbalancer   LoadBalancer   10.96.1.100    localhost     80:30123/TCP
```
**访问**: `http://localhost`

#### 云环境 (AWS/阿里云/腾讯云)
```
NAME                      TYPE           CLUSTER-IP     EXTERNAL-IP                                   PORT(S)
simple-web-loadbalancer   LoadBalancer   10.96.1.100    a1b2c3-nlb.us-east-1.elb.amazonaws.com      80:30123/TCP
```
**访问**: `http://a1b2c3-nlb.us-east-1.elb.amazonaws.com`

#### Pending状态
```
NAME                      TYPE           CLUSTER-IP     EXTERNAL-IP   PORT(S)
simple-web-loadbalancer   LoadBalancer   10.96.1.100    <pending>     80:30123/TCP
```
**访问**: `http://localhost:30123` (使用NodePort)

## 🧪 测试负载均衡

### 1. 浏览器测试
1. 打开访问地址
2. 点击 "🔄 刷新信息" 按钮
3. 观察页面背景颜色变化（模拟不同Pod响应）

### 2. 命令行测试
```bash
# 多次请求，观察响应
for i in {1..10}; do
  curl -s http://访问地址 | grep "当前服务信息" -A 3
  echo "---"
done
```

### 3. 监控Pod状态
```bash
# 实时监控Pod
kubectl get pods -l app=simple-web -w

# 查看Pod详细信息
kubectl get pods -l app=simple-web -o wide
```

## 🔧 故障排除

### LoadBalancer处于Pending状态
```bash
# 检查云厂商控制器
kubectl get pods -n kube-system | grep cloud-controller

# 查看Service事件
kubectl describe svc simple-web-loadbalancer

# 使用NodePort访问
kubectl get svc simple-web-loadbalancer
# 使用显示的NodePort端口访问: http://localhost:NodePort
```

### Pod无法启动
```bash
# 查看Pod状态
kubectl get pods -l app=simple-web

# 查看Pod日志
kubectl logs deployment/simple-web-app

# 查看Pod详细信息
kubectl describe pods -l app=simple-web
```

### 访问连接失败
```bash
# 使用端口转发
kubectl port-forward svc/simple-web-loadbalancer 8080:80

# 然后访问: http://localhost:8080
```

## 🎯 学习要点

### 1. LoadBalancer工作流程
```
用户请求 → LoadBalancer → NodePort → ClusterIP → Pod负载均衡
```

### 2. 负载均衡验证
- 每个请求可能路由到不同的Pod
- Service自动发现健康的Pod
- 故障Pod会被自动移除

### 3. 环境差异
- **本地环境**: 使用localhost或需要端口转发
- **云环境**: 自动分配外部IP/域名
- **私有环境**: 可能需要安装MetalLB

## 🗑️ 清理资源

```bash
# 删除所有创建的资源
kubectl delete -f simple-web-app.yaml

# 验证清理结果
kubectl get all -l app=simple-web
```

## 📚 扩展练习

1. **修改副本数**: 将replicas改为5，观察负载均衡效果
2. **添加健康检查**: 在容器中添加livenessProbe和readinessProbe
3. **配置资源限制**: 添加resources.limits和resources.requests
4. **尝试不同端口**: 修改Service端口，观察访问变化

这个示例帮助你理解LoadBalancer的基本概念和实际使用方法！ 