# Nacos 配置管理集成说明

## 概述
本项目已集成 Nacos 配置管理功能，可以从 Nacos 配置中心动态加载 `base.yaml` 和 `service-provider.yaml` 两个配置文件。

## 配置文件结构

### 1. 依赖配置
在 `pom.xml` 中已添加以下依赖：
- `spring-cloud-starter-alibaba-nacos-config`: Nacos配置管理
- `spring-cloud-starter-bootstrap`: Bootstrap配置支持

### 2. Bootstrap 配置
`src/main/resources/bootstrap.yml` 文件配置了 Nacos 配置中心连接信息：
- 服务器地址：`127.0.0.1:8848`
- 命名空间：`dev`
- 配置组：`DEFAULT_GROUP`
- 配置文件格式：`yaml`

### 3. 加载的配置文件
系统会自动从 Nacos 加载以下配置文件：
- `base.yaml`: 基础配置（共享配置）
- `service-provider.yaml`: 服务提供者特定配置

## 在 Nacos 中创建配置

### 1. base.yaml 配置示例
```yaml
# 应用基础配置
app:
  name: "Spring Cloud Demo"
  version: "2.1.0"
  description: "基于Spring Cloud的微服务演示项目"
  
# 数据库配置
database:
  url: "jdbc:mysql://localhost:3306/spring_cloud_demo"
  username: "root"
  password: "123456"
  driver-class-name: "com.mysql.cj.jdbc.Driver"
  
# Redis配置
redis:
  host: "localhost"
  port: 6379
  database: 0
  timeout: 3000
```

### 2. service-provider.yaml 配置示例
```yaml
# 服务提供者特定配置
provider:
  max-connections: 100
  timeout: 5000
  retry-count: 3
  
# 业务配置
business:
  cache-enabled: true
  log-level: "DEBUG"
  feature-flags:
    new-algorithm: true
    beta-feature: false
```

## 配置创建步骤

1. 访问 Nacos 控制台：http://localhost:8848/nacos
2. 使用账号密码登录：nacos/nacos
3. 进入"配置管理" -> "配置列表"
4. 选择命名空间：`dev`
5. 点击"+"按钮创建配置：
   - Data ID: `base.yaml`
   - Group: `DEFAULT_GROUP`
   - 配置格式: `YAML`
   - 配置内容: 粘贴上述 base.yaml 内容
6. 同样方式创建 `service-provider.yaml` 配置

## 测试配置加载

### 启动应用
```bash
cd service-provider
mvn spring-boot:run
```

### 测试接口
1. **获取配置信息**：
   ```
   GET http://localhost:18081/provider/config
   ```
   返回从 Nacos 加载的所有配置信息

2. **动态配置刷新**：
   ```
   POST http://localhost:18081/actuator/refresh
   ```
   刷新配置后再次调用 `/provider/config` 查看变化

### 验证配置动态刷新
1. 在 Nacos 控制台修改配置文件内容
2. 发布配置
3. 调用刷新接口或等待自动刷新
4. 再次获取配置信息验证是否更新

## 注意事项

1. **配置优先级**：Nacos配置 > application.yml > bootstrap.yml
2. **动态刷新**：使用 `@RefreshScope` 注解支持配置动态刷新
3. **配置安全**：生产环境中应使用加密配置和访问控制
4. **命名空间**：不同环境使用不同的命名空间隔离配置
5. **配置备份**：重要配置应定期备份

## 监控和日志

- 健康检查：http://localhost:18081/actuator/health
- 配置刷新端点：http://localhost:18081/actuator/refresh
- 查看配置日志：应用启动时会打印配置加载日志

## 故障排查

1. **配置加载失败**：检查 Nacos 服务是否启动，网络连接是否正常
2. **配置不生效**：检查配置文件名称、组名、命名空间是否正确
3. **动态刷新不工作**：确认使用了 `@RefreshScope` 注解
4. **权限问题**：检查 Nacos 的用户名密码配置 