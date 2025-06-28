#!/bin/bash

echo "构建 Order Service v1 镜像..."

# 检查是否在正确的目录
if [ ! -f "../order-service/pom.xml" ]; then
    echo "错误: 请在 argo-rollout 目录下运行此脚本"
    exit 1
fi

# 构建 JAR 包
echo "构建 Maven 项目..."
cd ../order-service
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Maven 构建失败"
    exit 1
fi

# 创建 v1 版本的 Dockerfile
cat > Dockerfile.v1 << 'EOF'
FROM hub-reg.nvxclouds.com/library/openjdk:17-slim

WORKDIR /app

# 安装必要的工具
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制 JAR 文件
COPY target/*.jar app.jar

# 设置环境变量
ENV APP_VERSION=v1
ENV SPRING_PROFILES_ACTIVE=prod

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/orders/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EOF

# 构建 Docker 镜像
echo "构建 Docker 镜像 order-service:v1..."
docker build -f Dockerfile.v1 -t order-service:v1 .

if [ $? -eq 0 ]; then
    echo "成功构建 order-service:v1 镜像"
    docker images | grep order-service
else
    echo "构建镜像失败"
    exit 1
fi

# 清理临时文件
rm -f Dockerfile.v1

echo "v1 版本构建完成!" 