#!/bin/bash

# Order Service Docker镜像构建脚本
# 构建order-service:latest镜像

set -e  # 遇到错误立即退出

# 脚本信息
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICE_NAME="order-service"
IMAGE_NAME="order-service:latest"

echo "🚀 开始构建 $SERVICE_NAME Docker镜像..."
echo "📁 工作目录: $SCRIPT_DIR"

# 检查Dockerfile是否存在
if [ ! -f "$SCRIPT_DIR/Dockerfile" ]; then
    echo "❌ 错误: 未找到 Dockerfile"
    exit 1
fi

# 步骤1: 清理旧的构建产物
echo "🧹 清理旧的构建产物..."
mvn clean -q

# 步骤2: 编译打包应用
echo "📦 编译打包 $SERVICE_NAME..."
mvn package -DskipTests -q

# 检查jar文件是否生成
JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" | head -1)
if [ -z "$JAR_FILE" ]; then
    echo "❌ 错误: 未找到生成的jar文件"
    exit 1
fi

echo "✅ 找到jar文件: $JAR_FILE"

# 步骤3: 构建Docker镜像
echo "🐳 构建Docker镜像: $IMAGE_NAME"
docker build -t $IMAGE_NAME .

# 检查镜像是否构建成功
if docker images | grep -q "$SERVICE_NAME.*latest"; then
    echo "✅ Docker镜像构建成功!"
    echo "📊 镜像信息:"
    docker images | grep "$SERVICE_NAME.*latest"
else
    echo "❌ Docker镜像构建失败"
    exit 1
fi

# 步骤4: 显示构建摘要
echo ""
echo "🎉 构建完成摘要:"
echo "   服务名称: $SERVICE_NAME"
echo "   镜像标签: $IMAGE_NAME"
echo "   JAR文件: $JAR_FILE"
echo ""
echo "💡 使用方法:"
echo "   docker run -p 8080:8080 $IMAGE_NAME"
echo ""
echo "🔍 镜像验证:"
echo "   docker run --rm $IMAGE_NAME java -version" 