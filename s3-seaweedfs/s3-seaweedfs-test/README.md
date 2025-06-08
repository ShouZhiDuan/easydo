# S3 SeaweedFS Test Application

这是一个用于测试和验证 `s3-seaweedfs-spring-boot-starter` 功能的Spring Boot应用程序。

## 功能特性

- 🚀 完整的文件操作REST API
- 📝 Swagger API文档
- 🔧 文件上传、下载、删除功能
- 📊 文件列表和元数据查询
- 🔗 预签名URL生成
- 📋 文件复制功能
- 💾 系统信息查询

## 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.6+
- SeaweedFS服务（已启动S3网关）

### 2. 配置SeaweedFS

确保SeaweedFS服务正在运行，并且S3网关已启动。默认配置：

```yaml
seaweedfs:
  s3:
    access-key: nvx1
    secret-key: nvx1
    bucket-name: gongan
    endpoint: http://192.168.60.70:38333
```

### 3. 构建和运行

首先构建主工程：

```bash
# 在主工程目录下
cd /Users/shouzhi/techzhi/project/common/s3-seaweedfs
mvn clean install
```

然后运行测试应用：

```bash
# 在测试工程目录下
cd s3-seaweedfs-test
mvn spring-boot:run
```

### 4. 访问应用

应用启动后，可以通过以下地址访问：

- **应用首页**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API文档**: http://localhost:8080/v2/api-docs
- **健康检查**: http://localhost:8080/actuator/health

## API接口说明

### 文件操作API

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/files/upload` | POST | 上传文件 |
| `/api/files/download/{key}` | GET | 下载文件 |
| `/api/files/delete/{key}` | DELETE | 删除文件 |
| `/api/files/exists/{key}` | GET | 检查文件是否存在 |
| `/api/files/list` | GET | 列出文件 |
| `/api/files/presigned-url/{key}` | GET | 生成预签名URL |
| `/api/files/copy` | POST | 复制文件 |
| `/api/files/info` | GET | 获取系统信息 |

### 使用示例

#### 1. 上传文件

```bash
curl -X POST "http://localhost:8080/api/files/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/your/file.txt" \
  -F "prefix=test"
```

#### 2. 下载文件

```bash
curl -X GET "http://localhost:8080/api/files/download/test/1234567890_file.txt" \
  -o downloaded_file.txt
```

#### 3. 列出文件

```bash
curl -X GET "http://localhost:8080/api/files/list?prefix=test&maxKeys=10"
```

#### 4. 检查文件存在

```bash
curl -X GET "http://localhost:8080/api/files/exists/test/1234567890_file.txt"
```

#### 5. 生成预签名URL

```bash
curl -X GET "http://localhost:8080/api/files/presigned-url/test/1234567890_file.txt?expireHours=2"
```

#### 6. 复制文件

```bash
curl -X POST "http://localhost:8080/api/files/copy" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "sourceKey=test/1234567890_file.txt&destinationKey=backup/1234567890_file.txt"
```

#### 7. 删除文件

```bash
curl -X DELETE "http://localhost:8080/api/files/delete/test/1234567890_file.txt"
```

## 测试场景

### 基础功能测试

1. **文件上传测试**
   - 上传不同类型的文件（文本、图片、文档等）
   - 测试大文件上传
   - 测试中文文件名

2. **文件下载测试**
   - 下载已上传的文件
   - 验证文件完整性
   - 测试不存在文件的下载

3. **文件管理测试**
   - 列出文件列表
   - 检查文件存在性
   - 获取文件元数据

### 高级功能测试

1. **预签名URL测试**
   - 生成预签名URL
   - 通过预签名URL访问文件
   - 测试URL过期机制

2. **文件复制测试**
   - 复制文件到不同路径
   - 验证复制后的文件内容

3. **错误处理测试**
   - 测试各种错误场景
   - 验证错误响应格式

## 配置说明

### 应用配置

```yaml
# 服务器配置
server:
  port: 8080
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

# SeaweedFS配置
seaweedfs:
  s3:
    enabled: true
    access-key: nvx1
    secret-key: nvx1
    bucket-name: gongan
    endpoint: http://192.168.60.70:38333
    region: us-east-1
    path-style-access-enabled: true
    connection-timeout: 10000
    request-timeout: 30000
    max-connections: 50
```

### 日志配置

应用日志会输出到控制台和文件：
- 控制台：实时查看应用运行状态
- 文件：`logs/s3-seaweedfs-test.log`

## 故障排除

### 常见问题

1. **连接SeaweedFS失败**
   - 检查SeaweedFS服务是否正常运行
   - 验证S3网关是否已启动
   - 确认endpoint配置是否正确

2. **文件上传失败**
   - 检查文件大小限制
   - 验证存储桶权限
   - 查看应用日志获取详细错误信息

3. **API访问异常**
   - 确认应用已正常启动
   - 检查端口是否被占用
   - 验证请求格式是否正确

### 调试技巧

1. **启用详细日志**
   ```yaml
   logging:
     level:
       com.techzhi: DEBUG
       com.amazonaws: DEBUG
   ```

2. **使用Swagger UI**
   - 访问 http://localhost:8080/swagger-ui.html
   - 直接在浏览器中测试API

3. **查看健康检查**
   - 访问 http://localhost:8080/actuator/health
   - 检查应用和依赖服务状态

## 许可证

MIT License