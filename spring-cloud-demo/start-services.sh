#!/bin/bash

# Spring Cloud 微服务启动脚本

echo "=== Spring Cloud 微服务启动脚本 ==="
echo

# 检查 Java 环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到 Java 环境，请先安装 Java 17+"
    exit 1
fi

# 检查 Maven 环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到 Maven 环境，请先安装 Maven"
    exit 1
fi

echo "检查 Nacos 服务状态..."
if ! curl -s http://localhost:8848/nacos > /dev/null; then
    echo "警告: Nacos 服务未启动，请先启动 Nacos Server"
    echo "启动命令: ./nacos/bin/startup.sh -m standalone"
    echo "访问地址: http://localhost:8848/nacos (用户名/密码: nacos/nacos)"
    echo
else
    echo "✓ Nacos 服务已启动"
fi

echo "开始编译项目..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "错误: 项目编译失败"
    exit 1
fi
echo "✓ 项目编译成功"
echo

echo "启动服务生产者 (端口: 8081)..."
cd service-provider
nohup mvn spring-boot:run > ../logs/provider.log 2>&1 &
PROVIDER_PID=$!
echo "服务生产者 PID: $PROVIDER_PID"
cd ..

echo "等待服务生产者启动..."
sleep 10

echo "启动服务消费者 (端口: 8082)..."
cd service-consumer
nohup mvn spring-boot:run > ../logs/consumer.log 2>&1 &
CONSUMER_PID=$!
echo "服务消费者 PID: $CONSUMER_PID"
cd ..

echo "等待服务消费者启动..."
sleep 10

echo
echo "=== 服务启动完成 ==="
echo "服务生产者: http://localhost:8081"
echo "服务消费者: http://localhost:8082"
echo "Nacos 控制台: http://localhost:8848/nacos"
echo
echo "测试命令:"
echo "curl http://localhost:8081/provider/info"
echo "curl http://localhost:8082/consumer/info"
echo
echo "查看日志:"
echo "tail -f logs/provider.log"
echo "tail -f logs/consumer.log"
echo
echo "停止服务:"
echo "./stop-services.sh"
echo

# 保存 PID 到文件
mkdir -p logs
echo $PROVIDER_PID > logs/provider.pid
echo $CONSUMER_PID > logs/consumer.pid

echo "服务 PID 已保存到 logs/ 目录"