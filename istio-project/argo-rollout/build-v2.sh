#!/bin/bash

echo "构建 Order Service v2 镜像..."

# 检查是否在正确的目录
if [ ! -f "../order-service/pom.xml" ]; then
    echo "错误: 请在 argo-rollout 目录下运行此脚本"
    exit 1
fi

# 先修改源代码，添加 v2 特性
echo "为 v2 版本添加新特性..."
cd ../order-service

# 创建一个临时的 application-v2.yml
cat > src/main/resources/application-v2.yml << 'EOF'
spring:
  application:
    name: order-service
  datasource:
    url: jdbc:h2:mem:orderdb
    driverClassName: org.h2.Driver
    username: sa
    password: password
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: false
  h2:
    console:
      enabled: true

server:
  port: 8080

app:
  version: v2
  features:
    - "Enhanced order processing"
    - "Improved response times"  
    - "Better error handling"
    - "Advanced analytics"

logging:
  level:
    com.istio.demo: INFO
EOF

# 构建 JAR 包
echo "构建 Maven 项目..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Maven 构建失败"
    exit 1
fi

# 创建 v2 版本的 Dockerfile
cat > Dockerfile.v2 << 'EOF'
FROM hub-reg.nvxclouds.com/library/openjdk:17-slim

WORKDIR /app

# 安装必要的工具
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制 JAR 文件
COPY target/*.jar app.jar

# 设置环境变量 - v2 版本
ENV APP_VERSION=v2
ENV SPRING_PROFILES_ACTIVE=v2

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/orders/health || exit 1

# 启动应用 - v2 版本有更好的JVM调优
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-jar", "/app/app.jar"]
EOF

# 构建 Docker 镜像
echo "构建 Docker 镜像 order-service:v2..."
docker build -f Dockerfile.v2 -t order-service:v2 .

if [ $? -eq 0 ]; then
    echo "成功构建 order-service:v2 镜像"
    docker images | grep order-service
else
    echo "构建镜像失败"
    exit 1
fi

# 清理临时文件
rm -f Dockerfile.v2

echo "v2 版本构建完成!"
echo "v2 版本新特性:"
echo "- 增强的订单处理"
echo "- 改进的响应时间"
echo "- 更好的错误处理"
echo "- 高级分析功能" 