FROM hub-reg.nvxclouds.com/library/openjdk:17-slim

# 设置工作目录
WORKDIR /app

# 安装curl用于健康检查
# RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制jar文件
COPY target/order-service-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 设置环境变量
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=prod

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/orders/health || exit 1

# 启动应用
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar 