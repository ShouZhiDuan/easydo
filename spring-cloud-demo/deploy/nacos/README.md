# Nacos Docker 部署

这个目录包含了使用 Docker Compose 部署 Nacos 服务的配置文件和脚本。

## 文件说明

- `docker-compose.yml` - Docker Compose 配置文件
- `start-nacos.sh` - Nacos 启动脚本
- `stop-nacos.sh` - Nacos 停止脚本
- `data/` - Nacos 数据目录（自动创建）
- `logs/` - Nacos 日志目录（自动创建）

## 快速开始

### 前置要求

- Docker
- Docker Compose

### 启动 Nacos

```bash
# 方式1: 使用启动脚本（推荐）
./start-nacos.sh

# 方式2: 直接使用 docker-compose
docker-compose up -d
```

### 停止 Nacos

```bash
# 方式1: 使用停止脚本（推荐）
./stop-nacos.sh

# 方式2: 直接使用 docker-compose
docker-compose down
```

## 访问信息

- **控制台地址**: http://localhost:8848/nacos
- **用户名**: nacos
- **密码**: nacos

## 端口说明

- `8848` - Nacos 主端口（HTTP）
- `9848` - 客户端 gRPC 请求服务端端口
- `9849` - 服务端 gRPC 请求服务端端口

## 配置说明

### 运行模式
- 使用 `standalone` 单机模式，适合开发和测试环境
- 生产环境建议使用集群模式

### 数据存储
- 使用内置数据库（embedded），数据持久化到 `./data` 目录
- 生产环境建议使用外部 MySQL 数据库

### 认证配置
- 启用了认证功能，提高安全性
- 默认用户名/密码: nacos/nacos

### JVM 参数
- 设置了较小的内存参数，适合开发环境
- 生产环境需要根据实际情况调整

## 常用命令

```bash
# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f nacos

# 重启服务
docker-compose restart nacos

# 进入容器
docker-compose exec nacos bash

# 清理所有数据（谨慎操作）
docker-compose down -v
rm -rf data logs
```

## 健康检查

容器配置了健康检查，可以通过以下方式查看：

```bash
# 查看健康状态
docker-compose ps

# 手动检查健康接口
curl http://localhost:8848/nacos/v1/console/health/readiness
```

## 故障排除

### 服务启动失败
1. 检查 Docker 服务是否运行
2. 检查端口 8848 是否被占用
3. 查看容器日志：`docker-compose logs nacos`

### 无法访问控制台
1. 等待服务完全启动（约30-60秒）
2. 检查防火墙设置
3. 确认端口映射正确

### 数据丢失
- 数据持久化到 `./data` 目录
- 删除容器不会丢失数据
- 只有删除 `data` 目录才会丢失数据

## 生产环境建议

1. **使用外部数据库**
   ```yaml
   environment:
     - SPRING_DATASOURCE_PLATFORM=mysql
     - MYSQL_SERVICE_HOST=mysql-host
     - MYSQL_SERVICE_DB_NAME=nacos
     - MYSQL_SERVICE_USER=nacos
     - MYSQL_SERVICE_PASSWORD=password
   ```

2. **调整 JVM 参数**
   ```yaml
   environment:
     - JVM_XMS=1g
     - JVM_XMX=1g
     - JVM_XMN=512m
   ```

3. **配置集群模式**
   ```yaml
   environment:
     - MODE=cluster
     - NACOS_SERVERS=nacos1:8848 nacos2:8848 nacos3:8848
   ```

4. **使用更强的认证密钥**
   ```yaml
   environment:
     - NACOS_AUTH_TOKEN=your-very-long-secret-key-here
   ```