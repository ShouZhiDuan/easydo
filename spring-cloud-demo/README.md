# Spring Cloud 微服务演示项目

这是一个基于 Spring Cloud 2025.0.0 和 Nacos 注册中心的微服务演示项目。

## 项目结构

```
spring-cloud-demo/
├── pom.xml                    # 父POM文件
├── service-provider/          # 服务生产者
│   ├── pom.xml
│   └── src/main/java/com/example/provider/
│       ├── ServiceProviderApplication.java
│       └── controller/ProviderController.java
└── service-consumer/          # 服务消费者
    ├── pom.xml
    └── src/main/java/com/example/consumer/
        ├── ServiceConsumerApplication.java
        ├── controller/ConsumerController.java
        └── feign/
            ├── ProviderFeignClient.java
            └── ProviderFeignClientFallback.java
```

## 技术栈

- **Spring Boot**: 3.4.0
- **Spring Cloud**: 2025.0.0
- **Spring Cloud Alibaba**: 2023.0.1.2
- **Nacos**: 服务注册与发现
- **OpenFeign**: 服务间调用
- **LoadBalancer**: 负载均衡
- **Java**: 17

## 快速开始

### 1. 启动 Nacos

下载并启动 Nacos Server：

```bash
# 下载 Nacos
wget https://github.com/alibaba/nacos/releases/download/2.3.0/nacos-server-2.3.0.tar.gz
tar -xzf nacos-server-2.3.0.tar.gz
cd nacos/bin

# 启动 Nacos (单机模式)
./startup.sh -m standalone
```

Nacos 控制台访问地址：http://localhost:8848/nacos
- 用户名：nacos
- 密码：nacos

### 2. 编译项目

```bash
cd spring-cloud-demo
mvn clean compile
```

### 3. 启动服务

#### 启动服务生产者

```bash
cd service-provider
mvn spring-boot:run
```

服务将在 8081 端口启动。

#### 启动服务消费者

```bash
cd service-consumer
mvn spring-boot:run
```

服务将在 8082 端口启动。

## API 接口

### 服务生产者 (service-provider:8081)

- `GET /provider/info` - 获取服务信息
- `GET /provider/user/{id}` - 根据ID获取用户信息
- `GET /provider/health` - 健康检查

### 服务消费者 (service-consumer:8082)

- `GET /consumer/info` - 通过Feign调用生产者获取信息
- `GET /consumer/user/{id}` - 通过Feign调用生产者获取用户信息
- `GET /consumer/instances` - 获取服务实例列表
- `GET /consumer/services` - 获取所有服务列表
- `GET /consumer/health` - 健康检查

## 测试示例

```bash
# 测试服务生产者
curl http://localhost:8081/provider/info
curl http://localhost:8081/provider/user/123

# 测试服务消费者
curl http://localhost:8082/consumer/info
curl http://localhost:8082/consumer/user/456
curl http://localhost:8082/consumer/instances
curl http://localhost:8082/consumer/services
```

## 功能特性

1. **服务注册与发现**：使用 Nacos 作为注册中心
2. **负载均衡**：Spring Cloud LoadBalancer
3. **服务调用**：OpenFeign 声明式服务调用
4. **容错处理**：Feign 降级处理
5. **健康检查**：Spring Boot Actuator
6. **配置管理**：YAML 配置文件

## 监控端点

- 服务生产者监控：http://localhost:8081/actuator
- 服务消费者监控：http://localhost:8082/actuator

## 注意事项

1. 确保 Nacos Server 已启动并运行在 localhost:8848
2. 服务启动顺序：先启动 Nacos，再启动各个微服务
3. 如需修改端口，请同时更新 application.yml 配置文件
4. 生产环境建议配置 Nacos 集群模式

## 扩展功能

可以进一步添加以下功能：
- 配置中心 (Nacos Config)
- 服务网关 (Spring Cloud Gateway)
- 分布式链路追踪 (Sleuth + Zipkin)
- 熔断器 (Sentinel)
- 分布式事务 (Seata)