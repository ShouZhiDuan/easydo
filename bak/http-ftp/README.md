# HTTP-FTP 通信系统

## 项目概述

本项目实现了一个基于 FTP 服务的 HTTP 通信系统，用于在两方网络环境无法直接进行 HTTP 通信的情况下，通过 FTP 服务作为中介来实现数据交换。

## 系统架构

```
┌─────────────────┐    FTP     ┌─────────────────┐    FTP     ┌─────────────────┐
│   Service A     │◄──────────►│  FTP Server 1   │◄──────────►│  FTP Server 2   │
│  (HTTP Client)  │            │ 192.168.60.69   │            │ 192.168.60.70   │
│   Port: 8080    │            │   Port: 10021   │            │   Port: 10021   │
└─────────────────┘            └─────────────────┘            └─────────────────┘
                                                                        ▲
                                                                        │
                                                                        ▼
                                                               ┌─────────────────┐
                                                               │   Service B     │
                                                               │ (HTTP Server)   │
                                                               │   Port: 8081    │
                                                               └─────────────────┘
```

## 项目结构

```
http-ftp/
├── service-a/                 # HTTP 客户端服务
│   ├── src/main/java/com/example/servicea/
│   │   ├── ServiceAApplication.java
│   │   ├── config/
│   │   │   └── FtpConfig.java
│   │   ├── controller/
│   │   │   └── HttpFtpController.java
│   │   └── service/
│   │       └── FtpClientService.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── service-b/                 # HTTP 服务端服务
│   ├── src/main/java/com/example/serviceb/
│   │   ├── ServiceBApplication.java
│   │   ├── config/
│   │   │   └── FtpConfig.java
│   │   ├── controller/
│   │   │   └── ServiceController.java
│   │   └── service/
│   │       ├── FtpClientService.java
│   │       ├── FtpListenerService.java
│   │       └── RequestProcessorService.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
└── README.md
```

## FTP 服务配置

### FTP Server 1 (192.168.60.69:10021)
- 用户名: ftpuser0
- 密码: cft6yhnbv@ga0351
- 模式: 被动模式
- 用途: Service A 的主要 FTP 服务器

### FTP Server 2 (192.168.60.70:10021)
- 用户名: ftpuser0
- 密码: cft6yhnbv@ga0351
- 模式: 被动模式
- 用途: Service B 的主要 FTP 服务器

## 通信流程

### GET 请求流程
1. 客户端向 Service A 发送 GET 请求
2. Service A 将请求信息封装为 JSON 文件
3. Service A 将请求文件上传到 FTP Server 1
4. Service B 定时监听 FTP Server 2，发现新的请求文件
5. Service B 下载并处理请求
6. Service B 生成响应并上传到 FTP Server 2
7. Service A 从 FTP Server 1 下载响应文件
8. Service A 将响应返回给客户端

### POST 请求流程
1. 客户端向 Service A 发送 POST 请求（包含请求体）
2. Service A 将请求信息（包括请求体）封装为 JSON 文件
3. 后续流程与 GET 请求相同

## 启动说明

### 启动 Service A
```bash
cd service-a
mvn spring-boot:run
```
Service A 将在端口 8080 启动

### 启动 Service B
```bash
cd service-b
mvn spring-boot:run
```
Service B 将在端口 8081 启动

## API 接口

### Service A (HTTP 客户端)

#### GET 请求
```bash
curl -X GET "http://localhost:8080/api/request?param1=value1&param2=value2"
```

#### POST 请求
```bash
curl -X POST "http://localhost:8080/api/request" \
  -H "Content-Type: application/json" \
  -d '{"key1":"value1","key2":"value2"}'
```

#### 健康检查
```bash
curl -X GET "http://localhost:8080/api/health"
```

### Service B (HTTP 服务端)

#### 健康检查
```bash
curl -X GET "http://localhost:8081/api/health"
```

#### 服务状态
```bash
curl -X GET "http://localhost:8081/api/status"
```

#### 启动/停止监听服务
```bash
# 启动监听
curl -X POST "http://localhost:8081/api/listener/start"

# 停止监听
curl -X POST "http://localhost:8081/api/listener/stop"

# 监听状态
curl -X GET "http://localhost:8081/api/listener/status"
```

## 文件格式

### 请求文件格式 (request_{requestId}.json)
```json
{
  "requestId": "uuid",
  "method": "GET|POST",
  "path": "/api/path",
  "queryParams": {
    "param1": "value1"
  },
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {},
  "timestamp": 1234567890
}
```

### 响应文件格式 (response_{requestId}.json)
```json
{
  "status": 200,
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "message": "success",
    "data": {}
  },
  "timestamp": 1234567890
}
```

## 监控和日志

- Service A 日志: `logs/service-a.log`
- Service B 日志: `logs/service-b.log`
- Actuator 端点: `/actuator/health`, `/actuator/info`, `/actuator/metrics`

## 注意事项

1. 确保 FTP 服务器可访问且配置正确
2. 确保 FTP 服务器上的目录结构存在：
   - `/data/requests` - 存放请求文件
   - `/data/responses` - 存放响应文件
3. Service B 会定时（每2秒）检查新的请求文件
4. 请求超时时间默认为 30 秒
5. 支持异步处理，提高并发性能

## 故障排除

1. **FTP 连接失败**: 检查网络连接和 FTP 服务器配置
2. **文件上传/下载失败**: 检查 FTP 用户权限和目录权限
3. **请求超时**: 检查网络延迟和 FTP 服务器性能
4. **Service B 未响应**: 检查 Service B 是否正常运行和监听服务状态