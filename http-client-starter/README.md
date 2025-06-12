# TechZhi HTTP Client Starter

一个高性能、易用的HTTP客户端Spring Boot Starter，支持多种HTTP客户端实现（OkHttp、Apache HttpClient），提供丰富的配置选项和便捷的使用方式。

## 特性

- 🚀 **高性能**: 基于OkHttp和Apache HttpClient 5的高性能HTTP客户端
- 🔧 **易配置**: 丰富的配置选项，支持连接池、超时、重试等
- 🔐 **安全支持**: 支持SSL/TLS配置，可关闭证书校验
- 🌐 **代理支持**: 支持HTTP和SOCKS代理配置
- 📝 **日志集成**: 可配置的请求/响应日志记录
- 🔄 **重试机制**: 内置智能重试机制
- 💻 **异步支持**: 支持同步和异步HTTP请求
- 🎯 **类型安全**: 自动JSON序列化/反序列化
- 🛠️ **Spring Boot集成**: 无缝集成Spring Boot自动配置
- 📁 **文件操作**: 完整的文件上传、下载和流处理支持
- 📊 **进度跟踪**: 文件传输进度回调和监控
- 🔍 **智能检测**: 自动文件类型检测和Content-Type映射

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.techzhi.common</groupId>
    <artifactId>http-client-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置

```yaml
techzhi:
  http-client:
    enabled: true
    client-type: OK_HTTP
    connect-timeout: 10s
    read-timeout: 30s
    # 更多配置选项请参考配置文档
```

### 3. 使用

```java
@Service
public class MyService {
    
    @Autowired
    private HttpClient httpClient;
    
    @Autowired
    private HttpClientTemplate httpClientTemplate;
    
    public void example() {
        // 基本GET请求
        HttpResponse<String> response = httpClient.get("https://api.example.com/data");
        
        // 带类型转换的GET请求
        HttpResponse<User> userResponse = httpClient.get("https://api.example.com/user/1", User.class);
        
        // POST请求
        User newUser = new User("John", "john@example.com");
        HttpResponse<User> createResponse = httpClient.post("https://api.example.com/users", newUser, User.class);
        
        // 使用模板类
        HttpResponse<String> tokenResponse = httpClientTemplate.getWithToken(
            "https://api.example.com/protected", 
            "your-token"
        );
    }
}
```

## 主要功能

### 支持的HTTP方法
- GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
- 同步和异步请求
- 自动JSON序列化/反序列化
- 表单提交支持

### 文件操作功能
- **文件上传**: 支持单个和多个文件上传
- **多部分表单**: multipart/form-data支持，可混合文件和表单字段
- **文件下载**: 支持大文件流式下载，自动创建目录
- **进度回调**: 上传和下载进度实时监控
- **文件流**: 获取文件输入流和字节数组
- **类型检测**: 自动文件类型检测和Content-Type映射
- **异步操作**: 支持异步文件上传和下载

### 高级功能
- 连接池管理
- 代理配置（HTTP/SOCKS）
- SSL/TLS配置（支持关闭证书验证）
- 重试机制
- 请求/响应日志
- Bearer Token和Basic认证

### 配置示例

```yaml
techzhi:
  http-client:
    enabled: true
    client-type: OK_HTTP  # OK_HTTP 或 APACHE_HTTP_CLIENT
    connect-timeout: 10s
    read-timeout: 30s
    write-timeout: 30s
    
    # 连接池配置
    pool:
      max-total: 200
      default-max-per-route: 50
      time-to-live: 5m
    
    # 代理配置
    proxy:
      enabled: false
      type: HTTP
      host: proxy.example.com
      port: 8080
    
    # SSL配置
    ssl:
      verify-hostname: true
      verify-certificate-chain: true
    
    # 重试配置
    retry:
      enabled: true
      max-attempts: 3
      retry-interval: 1s
```

## API使用示例

### 基本请求

```java
// GET请求
HttpResponse<String> response = httpClient.get("https://httpbin.org/get");

// POST请求
Map<String, Object> data = Map.of("key", "value");
HttpResponse<String> postResponse = httpClient.post("https://httpbin.org/post", data);
```

### 自定义请求

```java
HttpRequest request = HttpRequest.post("https://httpbin.org/post")
    .header("Authorization", "Bearer token")
    .header("Content-Type", "application/json")
    .queryParam("page", "1")
    .jsonBody(requestData);

HttpResponse<ResponseData> response = httpClient.execute(request, ResponseData.class);
```

### 异步请求

```java
CompletableFuture<HttpResponse<String>> future = httpClient.getAsync("https://httpbin.org/get");
future.thenAccept(response -> {
    System.out.println("Response: " + response.getBody());
});
```

### 文件操作示例

```java
// 1. 单个文件上传
File file = new File("document.pdf");
HttpResponse<String> uploadResponse = httpClient.uploadFile(
    "https://api.example.com/upload", 
    "file", 
    file
);

// 2. 文件上传带进度回调
FileProgressCallback progressCallback = new FileProgressCallback() {
    @Override
    public void onProgress(long bytesTransferred, long totalBytes, double percentage) {
        System.out.printf("上传进度: %.2f%%\n", percentage);
    }
    
    @Override
    public void onComplete(long totalBytes) {
        System.out.println("上传完成: " + totalBytes + " bytes");
    }
};

httpClient.uploadFile("https://api.example.com/upload", "file", file, progressCallback);

// 3. 多文件上传
List<MultipartFile> files = Arrays.asList(
    MultipartFile.of("file1", new File("doc1.pdf")),
    MultipartFile.of("file2", new File("doc2.pdf")),
    MultipartFile.of("data", "info.json", jsonData.getBytes(), "application/json")
);

HttpResponse<String> multiResponse = httpClient.uploadFiles("https://api.example.com/upload", files);

// 4. 文件和表单数据混合上传
Map<String, String> formFields = new HashMap<>();
formFields.put("title", "文档标题");
formFields.put("category", "技术文档");

httpClient.uploadFilesWithForm("https://api.example.com/upload", files, formFields);

// 5. 文件下载
boolean downloadSuccess = httpClient.downloadFile(
    "https://api.example.com/download/report.pdf", 
    "/path/to/save/report.pdf"
);

// 6. 文件下载带进度回调
httpClient.downloadFile(
    "https://api.example.com/download/large-file.zip", 
    "/path/to/save/large-file.zip", 
    progressCallback
);

// 7. 获取文件字节数组
byte[] fileBytes = httpClient.getFileBytes("https://api.example.com/download/small-file.txt");

// 8. 获取文件流
try (InputStream fileStream = httpClient.getFileStream("https://api.example.com/download/data.csv")) {
    // 处理文件流
    BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
    String line;
    while ((line = reader.readLine()) != null) {
        // 处理每一行
    }
}

// 9. 异步文件操作
CompletableFuture<HttpResponse<String>> asyncUpload = httpClient.uploadFileAsync(
    "https://api.example.com/upload", 
    "file", 
    file
);

CompletableFuture<Boolean> asyncDownload = httpClient.downloadFileAsync(
    "https://api.example.com/download/file.zip", 
    "/path/to/save/file.zip"
);
```

### 错误处理

```java
HttpResponse<String> response = httpClient.get("https://httpbin.org/status/404");

if (response.isSuccessful()) {
    String data = response.getBody();
} else if (response.isClientError()) {
    System.out.println("Client error: " + response.getStatusCode());
} else if (response.isServerError()) {
    System.out.println("Server error: " + response.getStatusCode());
}
```

## 构建和测试

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package

# 安装到本地仓库
mvn install
```

## 依赖说明

本项目支持以下HTTP客户端实现：

- **OkHttp**: 默认实现，高性能、轻量级
- **Apache HttpClient 5**: 功能丰富，企业级特性

根据项目需求选择合适的实现，通过配置`techzhi.http-client.client-type`来切换。

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！ 