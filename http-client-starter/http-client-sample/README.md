# HTTP Client Starter 示例项目

这是一个使用 TechZhi HTTP Client Starter 的示例 Spring Boot 应用程序，展示了HTTP客户端的各种使用场景。

## 项目结构

```
http-client-sample/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/techzhi/sample/
│       │       ├── HttpClientSampleApplication.java     # 主应用类
│       │       ├── controller/
│       │       │   └── DemoController.java              # REST API控制器
│       │       ├── service/
│       │       │   ├── HttpClientDemoService.java       # HTTP客户端演示服务
│       │       │   └── FileOperationDemoService.java    # 文件操作演示服务
│       │       ├── model/
│       │       │   ├── User.java                        # 用户模型
│       │       │   └── Post.java                        # 文章模型
│       │       └── runner/
│       │           └── DemoRunner.java                  # 启动时运行的示例
│       └── resources/
│           └── application.yml                          # 应用配置
├── pom.xml                                              # Maven配置
└── README.md                                            # 项目说明
```

## 功能演示

### HTTP客户端示例

1. **基本GET请求** - 简单的HTTP GET请求
2. **类型转换GET请求** - 自动将响应转换为Java对象
3. **POST请求** - 发送JSON数据的POST请求
4. **自定义请求** - 带自定义请求头和查询参数的请求
5. **异步请求** - 异步HTTP请求
6. **表单提交** - 表单数据提交
7. **HttpClientTemplate** - 使用模板类进行认证请求
8. **错误处理** - 错误处理和异常演示

### 文件操作示例

1. **单个文件上传** - 上传单个文件
2. **带进度回调的文件上传** - 带上传进度监控
3. **多文件上传** - 同时上传多个文件
4. **文件与表单数据结合上传** - 文件上传同时提交表单数据
5. **异步文件上传** - 异步上传文件
6. **文件下载** - 下载文件到本地
7. **带进度回调的文件下载** - 带下载进度监控
8. **异步文件下载** - 异步下载文件

## 快速开始

### 1. 构建starter项目

首先需要构建并安装HTTP Client Starter到本地Maven仓库：

```bash
# 在http-client-starter目录下
mvn clean install
```

### 2. 运行示例项目

```bash
# 切换到示例项目目录
cd http-client-sample

# 运行示例应用
mvn spring-boot:run
```

### 3. 查看运行结果

应用启动后会自动运行一些基本示例，你可以在控制台看到输出结果。

应用启动后监听 `http://localhost:8080`

## API接口

### 获取所有可用接口

```bash
GET http://localhost:8080/api/demo/endpoints
```

### HTTP客户端示例接口

```bash
# 运行所有HTTP客户端示例
POST http://localhost:8080/api/demo/http-client/all

# 基本GET请求示例
POST http://localhost:8080/api/demo/http-client/basic-get

# 类型转换GET请求示例
POST http://localhost:8080/api/demo/http-client/type-conversion

# POST请求示例
POST http://localhost:8080/api/demo/http-client/post

# 自定义请求示例
POST http://localhost:8080/api/demo/http-client/custom-request

# 异步请求示例
POST http://localhost:8080/api/demo/http-client/async

# 表单提交示例
POST http://localhost:8080/api/demo/http-client/form-submit

# HttpClientTemplate示例
POST http://localhost:8080/api/demo/http-client/template

# 错误处理示例
POST http://localhost:8080/api/demo/http-client/error-handling
```

### 文件操作示例接口

```bash
# 运行所有文件操作示例
POST http://localhost:8080/api/demo/file-operations/all

# 单个文件上传示例
POST http://localhost:8080/api/demo/file-operations/single-upload

# 带进度回调的文件上传示例
POST http://localhost:8080/api/demo/file-operations/upload-with-progress

# 多文件上传示例
POST http://localhost:8080/api/demo/file-operations/multiple-upload

# 文件上传与表单数据结合示例
POST http://localhost:8080/api/demo/file-operations/upload-with-form

# 异步文件上传示例
POST http://localhost:8080/api/demo/file-operations/async-upload

# 文件下载示例
POST http://localhost:8080/api/demo/file-operations/download

# 带进度回调的文件下载示例
POST http://localhost:8080/api/demo/file-operations/download-with-progress

# 异步文件下载示例
POST http://localhost:8080/api/demo/file-operations/async-download
```

## 配置说明

项目使用以下HTTP客户端配置（见 `application.yml`）：

```yaml
techzhi:
  http-client:
    enabled: true
    client-type: OK_HTTP              # 使用OkHttp客户端
    connect-timeout: 10s              # 连接超时
    read-timeout: 30s                 # 读取超时
    write-timeout: 30s                # 写入超时
    
    # 连接池配置
    pool:
      max-total: 200                  # 最大连接数
      default-max-per-route: 50       # 每个路由的最大连接数
      time-to-live: 5m                # 连接存活时间
    
    # 重试配置
    retry:
      enabled: true                   # 启用重试
      max-attempts: 3                 # 最大重试次数
      retry-interval: 1s              # 重试间隔
    
    # 日志配置
    logging:
      enabled: true                   # 启用日志
      log-request: true               # 记录请求
      log-response: true              # 记录响应
      log-headers: true               # 记录请求头
```

## 使用示例

### 在代码中使用HTTP客户端

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

## 测试建议

你可以通过以下方式测试示例：

1. **使用curl命令**：
   ```bash
   curl -X POST http://localhost:8080/api/demo/http-client/basic-get
   ```

2. **使用Postman或其他HTTP客户端工具**

3. **查看应用日志**：
   所有HTTP请求和响应都会记录在日志中，方便调试和学习

## 注意事项

1. 示例中使用的是公共测试API（如jsonplaceholder.typicode.com、httpbin.org），请确保网络连接正常，这两个地址需要开启🪜才能访问。
2. 文件操作示例会在项目根目录创建临时文件，运行完成后会自动删除
3. 可以修改 `application.yml` 中的配置来测试不同的客户端行为
4. 日志级别设置为DEBUG，可以看到详细的请求响应信息

## 扩展示例

你可以基于这个示例项目：

1. 添加更多的HTTP请求场景
2. 测试不同的配置组合
3. 集成到你自己的业务逻辑中
4. 添加单元测试和集成测试

---

欢迎使用 TechZhi HTTP Client Starter！如有问题，请查看starter项目的文档或提交issue。 