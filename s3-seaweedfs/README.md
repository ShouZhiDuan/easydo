# SeaweedFS S3 Spring Boot Starter

一个用于Spring Boot项目集成SeaweedFS S3协议的通用库，提供文件上传、下载、删除等操作。

## 特性

- 🚀 开箱即用的Spring Boot自动配置
- 📁 完整的S3协议支持
- 🛠️ 便捷的工具类和服务类
- 🔧 灵活的配置选项
- 📝 完善的单元测试
- 🎯 高性能文件操作

## 环境要求

- Java 17+
- Maven 3.6+
- Spring Boot 2.7+

## 快速开始

### 1. 添加依赖

在你的Spring Boot项目中添加以下依赖：

```xml
<dependency>
    <groupId>com.techzhi.common</groupId>
    <artifactId>s3-seaweedfs-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加SeaweedFS配置：

```yaml
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

### 3. 使用方式

#### 方式一：注入服务类

```java
@Service
public class FileService {
    
    @Autowired
    private SeaweedFsS3Service seaweedFsS3Service;
    
    public void uploadFile(String key, byte[] data) {
        seaweedFsS3Service.uploadFile(key, data, "application/octet-stream");
    }
    
    public byte[] downloadFile(String key) {
        return seaweedFsS3Service.getFileBytes(key);
    }
}
```

#### 方式二：使用工具类

```java
@RestController
public class FileController {
    
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String key = "uploads/" + file.getOriginalFilename();
        SeaweedFsS3Util.upload(key, file.getBytes(), file.getContentType());
        return "File uploaded successfully: " + key;
    }
    
    @GetMapping("/download/{key}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String key) {
        if (!SeaweedFsS3Util.exists(key)) {
            return ResponseEntity.notFound().build();
        }
        
        byte[] data = SeaweedFsS3Util.getBytes(key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .body(data);
    }
}
```

## API 文档

### SeaweedFsS3Service 主要方法

| 方法 | 描述 |
|------|------|
| `uploadFile(key, inputStream, contentLength, contentType)` | 上传文件流 |
| `uploadFile(key, data, contentType)` | 上传字节数组 |
| `downloadFile(key)` | 下载文件对象 |
| `getFileInputStream(key)` | 获取文件输入流 |
| `getFileBytes(key)` | 获取文件字节数组 |
| `deleteFile(key)` | 删除文件 |
| `doesFileExist(key)` | 检查文件是否存在 |
| `getFileMetadata(key)` | 获取文件元数据 |
| `listFiles(prefix)` | 列出指定前缀的文件 |
| `listAllFiles()` | 列出所有文件 |
| `generatePresignedUrl(key, expiration)` | 生成预签名URL |
| `copyFile(sourceKey, destinationKey)` | 复制文件 |

### SeaweedFsS3Util 工具类方法

| 方法 | 描述 |
|------|------|
| `upload(key, data, contentType)` | 上传文件 |
| `download(key)` | 下载文件 |
| `getBytes(key)` | 获取文件字节数组 |
| `delete(key)` | 删除文件 |
| `exists(key)` | 检查文件是否存在 |
| `list(prefix)` | 列出文件 |
| `generatePresignedUrl(key)` | 生成预签名URL（1小时过期） |
| `copy(sourceKey, destinationKey)` | 复制文件 |

## 配置参数

| 参数 | 默认值 | 描述 |
|------|--------|------|
| `seaweedfs.s3.enabled` | `true` | 是否启用SeaweedFS S3功能 |
| `seaweedfs.s3.access-key` | `nvx1` | S3访问密钥 |
| `seaweedfs.s3.secret-key` | `nvx1` | S3秘密密钥 |
| `seaweedfs.s3.bucket-name` | `gongan` | 存储桶名称 |
| `seaweedfs.s3.endpoint` | `http://192.168.60.70:38333` | SeaweedFS S3端点 |
| `seaweedfs.s3.region` | `us-east-1` | AWS区域 |
| `seaweedfs.s3.path-style-access-enabled` | `true` | 是否启用路径样式访问 |
| `seaweedfs.s3.connection-timeout` | `10000` | 连接超时时间（毫秒） |
| `seaweedfs.s3.request-timeout` | `30000` | 请求超时时间（毫秒） |
| `seaweedfs.s3.max-connections` | `50` | 最大连接数 |

## 运行测试

确保SeaweedFS服务正在运行，然后执行：

```bash
mvn test
```

## 构建项目

```bash
mvn clean install
```

## 注意事项

1. 确保SeaweedFS服务已正确配置并启动S3网关
2. 根据实际环境修改配置文件中的连接信息
3. 在生产环境中，建议将敏感信息（如访问密钥）配置在环境变量中
4. 大文件上传时，建议使用分片上传功能

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。