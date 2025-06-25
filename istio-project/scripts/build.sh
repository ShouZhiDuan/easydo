#!/bin/bash

set -e

echo "Building Istio Demo Services..."

# 构建所有模块 (使用父pom)
echo "Building all modules with parent POM..."
mvn clean package -DskipTests

# 构建User Service Docker镜像
echo "Building User Service Docker image..."
cd user-service
docker build -t user-service:latest .
docker tag user-service:latest user-service:v1
cd ..

# 构建Order Service Docker镜像
echo "Building Order Service Docker image..."
cd order-service
docker build -t order-service:latest .
docker tag order-service:latest order-service:v1
cd ..

echo "Build completed successfully!"

# 可选：推送到本地registry
if [ "$1" = "push" ]; then
    echo "Pushing images to local registry..."
    docker tag user-service:latest localhost:5000/user-service:latest
    docker tag order-service:latest localhost:5000/order-service:latest
    docker push localhost:5000/user-service:latest
    docker push localhost:5000/order-service:latest
    echo "Images pushed to local registry!"
fi

echo "Available images:"
docker images | grep -E "(user-service|order-service)" 