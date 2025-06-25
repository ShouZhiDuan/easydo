# Docker镜像构建指南

## 📦 概述

本项目提供了三种方式来构建Docker镜像：
1. **单个服务构建** - 在各服务目录中独立构建
2. **批量构建** - 在项目根目录一次性构建所有服务
3. **手动构建** - 使用Maven和Docker命令手动构建

## 🚀 快速开始

### 方式1: 批量构建所有服务（推荐）

```bash
# 在项目根目录执行
./build-all-images.sh
```

### 方式2: 单独构建某个服务

```bash
# 构建order-service
cd order-service
./build-image.sh

# 构建user-service  
cd user-service
./build-image.sh
```

## 📁 脚本文件结构

```
istio-project/
├── build-all-images.sh          # 主构建脚本（构建所有服务）
├── order-service/
│   ├── Dockerfile
│   └── build-image.sh           # order-service构建脚本
└── user-service/
    ├── Dockerfile
    └── build-image.sh           # user-service构建脚本
```

## 🔧 构建过程

每个构建脚本都会执行以下步骤：

1. **清理旧构建** - `mvn clean`
2. **编译打包** - `mvn package -DskipTests`
3. **验证JAR文件** - 检查生成的jar文件
4. **构建Docker镜像** - `docker build -t service-name:latest .`
5. **验证镜像** - 检查镜像是否成功创建

## 📊 生成的镜像

| 服务 | 镜像名称 | 标签 | 端口 |
|------|----------|------|------|
| Order Service | `order-service` | `latest` | 8080 |
| User Service | `user-service` | `latest` | 8080 |

## 🐳 使用Docker镜像

### 启动单个服务

```bash
# 启动order-service（映射到主机8080端口）
docker run -d -p 8080:8080 --name order-service order-service:latest

# 启动user-service（映射到主机8081端口） 
docker run -d -p 8081:8080 --name user-service user-service:latest
```

### 检查服务状态

```bash
# 查看运行中的容器
docker ps

# 查看服务健康状态
curl http://localhost:8080/api/orders/health  # order-service
curl http://localhost:8081/api/users/health   # user-service
```

### 查看日志

```bash
# 查看order-service日志
docker logs order-service

# 查看user-service日志
docker logs user-service
```

## 🛠 手动构建方式

如果需要手动构建，可以按以下步骤操作：

```bash
# 1. 构建order-service
cd order-service
mvn clean package -DskipTests
docker build -t order-service:latest .
cd ..

# 2. 构建user-service
cd user-service  
mvn clean package -DskipTests
docker build -t user-service:latest .
cd ..
```

## 🔍 故障排查

### 常见问题

1. **Maven构建失败**
   ```bash
   # 检查Java版本（需要Java 17）
   java -version
   
   # 检查Maven版本
   mvn -version
   ```

2. **Docker构建失败**
   ```bash
   # 检查Docker是否运行
   docker version
   
   # 检查Dockerfile语法
   docker build --no-cache -t test .
   ```

3. **端口冲突**
   ```bash
   # 查看端口占用
   lsof -ti:8080
   
   # 停止占用端口的进程
   lsof -ti:8080 | xargs kill -9
   ```

### 清理镜像

```bash
# 删除所有相关镜像
docker rmi order-service:latest user-service:latest

# 清理未使用的镜像
docker image prune
```

## 💡 最佳实践

1. **构建前检查**
   - 确保Docker守护进程运行
   - 确保有足够的磁盘空间
   - 关闭可能占用端口的服务

2. **优化构建速度**
   - 使用Maven的离线模式：`mvn -o package`
   - 使用Docker多阶段构建缓存

3. **生产环境建议**
   - 使用具体版本标签而非`latest`
   - 定期清理未使用的镜像
   - 配置健康检查和资源限制

## 🔗 相关文档

- [MOCK_DATA_CHANGES.md](./MOCK_DATA_CHANGES.md) - Mock数据实现说明
- [CLEAN_CODE_CHANGES.md](./CLEAN_CODE_CHANGES.md) - 代码清理说明
- [README.md](./README.md) - 项目总体说明 