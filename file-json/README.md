# File JSON Application

这是一个Spring Boot应用程序，实现了一个POST接口，能够同时返回文件流和自定义JSON对象。

## 功能特性

- 提供POST接口同时返回文件流和JSON数据
- 支持两种响应方式：
  1. 多部分响应（MultiValueMap）
  2. Base64编码文件内容的JSON响应
- 自动创建示例文件用于测试
- 完整的错误处理机制

## 项目结构

```
file-json/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── filejson/
        │               ├── FileJsonApplication.java
        │               ├── controller/
        │               │   └── FileJsonController.java
        │               └── model/
        │                   └── ResponseData.java
        └── resources/
            └── application.properties
```

## API接口

### 1. 多部分响应接口

**POST** `/api/file-json`

**参数：**
- `filePath` (可选): 文件路径，默认为 "sample.txt"

**响应：**
- Content-Type: `multipart/form-data`
- 包含两部分：
  - `file`: 文件流
  - `json`: JSON字符串

**示例请求：**
```bash
curl -X POST "http://localhost:8080/api/file-json" \
     -H "Content-Type: application/json"
```

### 2. JSON响应接口（Base64编码文件）

**POST** `/api/file-json-alternative`

**参数：**
- `filePath` (可选): 文件路径，默认为 "sample.txt"

**响应：**
- Content-Type: `application/json`
- JSON对象包含：
  - `fileContent`: Base64编码的文件内容
  - `fileInfo`: 文件信息和自定义数据
  - `message`: 响应消息
  - `status`: 状态
  - `timestamp`: 时间戳

**示例请求：**
```bash
curl -X POST "http://localhost:8080/api/file-json-alternative" \
     -H "Content-Type: application/json"
```

**示例响应：**
```json
{
  "fileContent": "5L2g5aW955qE5paH5Lu25YaF5a65Li4u",
  "fileInfo": {
    "userId": 12345,
    "userName": "李四",
    "fileSize": 150,
    "fileName": "sample.txt"
  },
  "message": "文件和数据获取成功",
  "status": "success",
  "timestamp": 1703123456789
}
```

## 运行应用

### 前提条件
- Java 11 或更高版本
- Maven 3.6 或更高版本

### 启动步骤

1. 编译项目：
```bash
mvn clean compile
```

2. 运行应用：
```bash
mvn spring-boot:run
```

3. 应用将在 `http://localhost:8080` 启动

### 测试接口

应用启动后，会自动创建一个示例文件 `sample.txt`，你可以使用以下命令测试接口：

```bash
# 测试多部分响应接口
curl -X POST "http://localhost:8080/api/file-json"

# 测试JSON响应接口
curl -X POST "http://localhost:8080/api/file-json-alternative"
```

## 技术栈

- **Spring Boot 2.7.14**: 主框架
- **Spring Web**: Web层支持
- **Jackson**: JSON处理
- **Maven**: 依赖管理和构建工具
- **Java 11**: 编程语言

## 配置说明

应用配置位于 `src/main/resources/application.properties`：

- `server.port=8080`: 服务器端口
- `spring.servlet.multipart.max-file-size=10MB`: 最大文件大小
- `spring.servlet.multipart.max-request-size=10MB`: 最大请求大小

## 注意事项

1. 示例文件会在应用启动时自动创建在项目根目录
2. 两种接口提供了不同的文件传输方式，可根据客户端需求选择
3. 多部分响应适合需要直接处理文件流的场景
4. Base64编码响应适合纯JSON API的场景
5. 所有接口都包含完整的错误处理机制