#!/bin/bash

# 生产环境部署模板脚本
# 使用环境变量动态生成Istio配置

set -e

# 配置参数
SERVICE_NAME=${1:-"user-service"}
NEW_VERSION=${2:-"v2"}
OLD_VERSION=${3:-"v1"}
DEPLOYMENT_TYPE=${4:-"canary"}  # canary, blue-green, rolling
TRAFFIC_SPLIT=${5:-"10"}        # 新版本流量百分比

NAMESPACE="istio-demo"
TEMPLATES_DIR="./templates"
OUTPUT_DIR="./generated"

echo "🚀 开始部署 ${SERVICE_NAME} 版本 ${NEW_VERSION}"
echo "部署类型: ${DEPLOYMENT_TYPE}"

# 创建输出目录
mkdir -p ${OUTPUT_DIR}

# 根据部署类型生成配置
case ${DEPLOYMENT_TYPE} in
  "canary")
    echo "📊 生成金丝雀部署配置..."
    generate_canary_config
    ;;
  "blue-green")
    echo "🔄 生成蓝绿部署配置..."
    generate_blue_green_config
    ;;
  "rolling")
    echo "⚡ 生成滚动更新配置..."
    generate_rolling_config
    ;;
  *)
    echo "❌ 不支持的部署类型: ${DEPLOYMENT_TYPE}"
    exit 1
    ;;
esac

# 应用配置
kubectl apply -f ${OUTPUT_DIR}/

echo "✅ 部署完成！"

# 金丝雀部署配置生成函数
generate_canary_config() {
  cat > ${OUTPUT_DIR}/${SERVICE_NAME}-canary.yaml << EOF
# 新版本部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${SERVICE_NAME}-${NEW_VERSION}
  namespace: ${NAMESPACE}
  labels:
    app: ${SERVICE_NAME}
    version: ${NEW_VERSION}
    deployment-type: canary
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ${SERVICE_NAME}
      version: ${NEW_VERSION}
  template:
    metadata:
      labels:
        app: ${SERVICE_NAME}
        version: ${NEW_VERSION}
    spec:
      containers:
      - name: ${SERVICE_NAME}
        image: ${SERVICE_NAME}:${NEW_VERSION}
        ports:
        - containerPort: 8080
        env:
        - name: APP_VERSION
          value: "${NEW_VERSION}"
---
# 流量分配策略
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: ${SERVICE_NAME}-canary-vs
  namespace: ${NAMESPACE}
  annotations:
    deployment.timestamp: "$(date -u +%Y%m%d%H%M%S)"
spec:
  hosts:
  - ${SERVICE_NAME}
  http:
  # 基于Header的路由（用于测试）
  - match:
    - headers:
        canary:
          exact: "true"
    route:
    - destination:
        host: ${SERVICE_NAME}
        subset: ${NEW_VERSION}
  # 流量分配
  - route:
    - destination:
        host: ${SERVICE_NAME}
        subset: ${OLD_VERSION}
      weight: $((100 - TRAFFIC_SPLIT))
    - destination:
        host: ${SERVICE_NAME}
        subset: ${NEW_VERSION}
      weight: ${TRAFFIC_SPLIT}
---
# 目标规则
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: ${SERVICE_NAME}-canary-dr
  namespace: ${NAMESPACE}
spec:
  host: ${SERVICE_NAME}
  subsets:
  - name: ${OLD_VERSION}
    labels:
      version: ${OLD_VERSION}
  - name: ${NEW_VERSION}
    labels:
      version: ${NEW_VERSION}
EOF
}

# 蓝绿部署配置生成函数
generate_blue_green_config() {
  cat > ${OUTPUT_DIR}/${SERVICE_NAME}-blue-green.yaml << EOF
# Green版本部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${SERVICE_NAME}-green
  namespace: ${NAMESPACE}
  labels:
    app: ${SERVICE_NAME}
    version: green
    deployment-type: blue-green
spec:
  replicas: 2
  selector:
    matchLabels:
      app: ${SERVICE_NAME}
      version: green
  template:
    metadata:
      labels:
        app: ${SERVICE_NAME}
        version: green
    spec:
      containers:
      - name: ${SERVICE_NAME}
        image: ${SERVICE_NAME}:${NEW_VERSION}
        ports:
        - containerPort: 8080
        env:
        - name: APP_VERSION
          value: "${NEW_VERSION}"
---
# 蓝绿切换策略
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: ${SERVICE_NAME}-blue-green-vs
  namespace: ${NAMESPACE}
  annotations:
    deployment.timestamp: "$(date -u +%Y%m%d%H%M%S)"
    active.version: "blue"  # 当前激活版本
spec:
  hosts:
  - ${SERVICE_NAME}
  http:
  # 测试流量路由到Green
  - match:
    - headers:
        version:
          exact: "green"
    route:
    - destination:
        host: ${SERVICE_NAME}
        subset: green
  # 生产流量路由到Blue（当前版本）
  - route:
    - destination:
        host: ${SERVICE_NAME}
        subset: blue
---
# 目标规则
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: ${SERVICE_NAME}-blue-green-dr
  namespace: ${NAMESPACE}
spec:
  host: ${SERVICE_NAME}
  subsets:
  - name: blue
    labels:
      version: ${OLD_VERSION}
  - name: green
    labels:
      version: green
EOF
}

# 滚动更新配置生成函数
generate_rolling_config() {
  cat > ${OUTPUT_DIR}/${SERVICE_NAME}-rolling.yaml << EOF
# 滚动更新部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${SERVICE_NAME}
  namespace: ${NAMESPACE}
  labels:
    app: ${SERVICE_NAME}
    deployment-type: rolling
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  selector:
    matchLabels:
      app: ${SERVICE_NAME}
  template:
    metadata:
      labels:
        app: ${SERVICE_NAME}
        version: ${NEW_VERSION}
    spec:
      containers:
      - name: ${SERVICE_NAME}
        image: ${SERVICE_NAME}:${NEW_VERSION}
        ports:
        - containerPort: 8080
        env:
        - name: APP_VERSION
          value: "${NEW_VERSION}"
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
EOF
} 