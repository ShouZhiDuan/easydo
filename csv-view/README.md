# CSV文件预览与Excel下载工具

这是一个基于SpringBoot开发的Web应用，用于上传CSV文件、在线预览数据并下载为Excel格式。应用特别注重保持原CSV文件的列顺序一致性。

## 功能特性

- **CSV文件上传**: 支持拖拽或点击上传CSV文件
- **数据预览**: 实时预览CSV文件内容，支持可选分页显示
- **Excel导出**: 将CSV数据转换为Excel格式并下载
- **列顺序保持**: 确保导出的Excel文件保持原CSV文件的列顺序
- **大文件处理**: 支持大型CSV文件的分页处理
- **前后端分离**: 提供完整的REST API接口
- **文件管理**: 支持文件信息查询和删除操作
- **错误处理**: 完善的错误提示和异常处理
- **响应式设计**: 适配不同屏幕尺寸的设备

## 技术栈

- **后端**: Spring Boot 2.7.14
- **前端**: Thymeleaf + Bootstrap 5 + Font Awesome
- **CSV处理**: Apache Commons CSV
- **Excel处理**: Apache POI
- **构建工具**: Maven
- **Java版本**: 11+

## 快速开始

### 前置要求

- Java 8 或更高版本
- Maven 3.6 或更高版本

### 运行步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd csv-view
   ```

2. **编译并运行**
   ```bash
   mvn spring-boot:run
   ```

3. **访问应用**
   
   - 传统页面: `http://localhost:8080/csv/`
   - API测试页面: `http://localhost:8080/csv/api-test`
   - 多数据源测试页面: `http://localhost:8080/csv/multi-source-test`

### 使用示例

#### 传统Web界面
1. **上传CSV文件**: 点击"选择文件"按钮，选择一个CSV文件
2. **预览数据**: 上传成功后，系统会自动显示CSV文件的内容预览
3. **分页浏览**: 使用页面底部的分页控件浏览大文件
4. **下载Excel**: 点击"下载Excel"按钮，将CSV数据导出为Excel文件

#### API接口使用
1. **访问API测试页面**: `http://localhost:8080/csv/api-test`
2. **上传文件**: 选择CSV文件并上传，获取文件ID
3. **选择预览方式**: 可选择是否启用分页预览
4. **预览数据**: 查看CSV文件内容
5. **下载Excel**: 使用文件ID下载Excel格式文件
6. **文件管理**: 查询文件信息或删除文件

#### 多数据源测试
1. **访问多数据源测试页面**: `http://localhost:8080/csv/multi-source-test`
2. **测试MultipartFile**: 选择CSV文件并上传，获取文件ID
3. **测试File路径**: 输入本地文件路径进行上传
4. **测试InputStream**: 通过数据流方式上传CSV数据
5. **预览和下载**: 对不同数据源的文件进行预览和Excel下载

## 项目结构

```
csv-view/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/csvview/
│   │   │       ├── CsvViewApplication.java     # 主启动类
│   │   │       ├── controller/
│   │   │       │   └── CsvController.java      # 控制器(包含REST API)
│   │   │       ├── service/
│   │   │       │   └── CsvService.java         # 业务逻辑
│   │   │       └── model/
│   │   │           └── CsvData.java            # 数据模型
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── index.html                  # 传统前端页面
│   │       │   └── api-test.html               # API测试页面
│   │       └── application.yml                 # 配置文件
├── pom.xml                                     # Maven配置
├── sample-data.csv                             # 示例数据
└── README.md                                   # 项目说明
```

## 核心特性

### 1. 列顺序保持机制

项目采用 `LinkedHashMap` 和有序列表来确保CSV文件的列顺序在整个处理过程中保持不变：

- **CSV解析**: 使用Apache Commons CSV按顺序读取列头
- **数据存储**: 使用有序数据结构存储列信息
- **Excel生成**: 按原始顺序创建Excel列

### 2. 前后端分离架构

系统提供完整的REST API接口，支持前后端分离开发：

- **文件上传API**: `POST /csv/api/upload`
- **数据预览API**: `GET /csv/api/preview/{fileId}`
- **Excel下载API**: `GET /csv/api/download/{fileId}`
- **文件管理API**: `GET/DELETE /csv/api/files/{fileId}`

### 3. 灵活的分页机制

支持用户选择是否启用分页预览：

- **可选分页**: 用户可选择全量预览或分页预览
- **内存优化**: 分页模式下只加载当前页面数据
- **灵活配置**: 支持自定义页码和每页显示行数

### 4. 多数据源支持

- **MultipartFile**: 标准文件上传
- **File**: 本地文件路径读取
- **InputStream**: 数据流方式处理
- **重载方法**: 提供独立的预览和下载方法，无需先保存到内存

### 5. 独立处理能力

- **直接预览**: 从File或InputStream直接预览CSV数据
- **直接转换**: 从File或InputStream直接转换为Excel
- **无状态处理**: 不依赖文件存储，适合一次性处理场景

### 6. 文件存储管理

采用内存存储方案（可扩展为Redis或数据库）：

- **UUID标识**: 每个上传文件分配唯一ID
- **临时存储**: 文件数据临时存储在内存中
- **生命周期管理**: 支持文件删除和清理

### 7. 核心依赖

- **Apache Commons CSV**: CSV文件解析
- **Apache POI**: Excel文件生成
- **Spring Boot**: Web框架和依赖注入
- **Thymeleaf**: 模板引擎

## 配置说明

### 文件上传限制

在`application.yml`中可以调整文件上传限制：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB      # 单个文件最大大小
      max-request-size: 50MB   # 请求最大大小
```

### 分页设置

默认每页显示50行数据，可以通过URL参数调整：
- `page`: 页码（从0开始）
- `size`: 每页行数

例如：`http://localhost:8080/csv/preview?page=0&size=100`

## API 接口文档

### 1. 文件上传接口

#### 1.1 MultipartFile上传
```
POST /csv/api/upload
Content-Type: multipart/form-data

参数:
- file: CSV文件

响应:
{
  "success": true,
  "message": "CSV文件上传成功",
  "fileId": "uuid",
  "fileName": "example.csv",
  "totalRows": 100,
  "totalColumns": 5,
  "headers": ["列1", "列2", "列3", "列4", "列5"]
}
```

#### 1.2 本地文件路径
```
POST /csv/api/upload-file
Content-Type: application/x-www-form-urlencoded
参数: 
  - filePath (String, 必需): 文件完整路径
  - fileName (String, 可选): 自定义文件名
```

#### 1.3 InputStream数据流
```
POST /csv/api/upload-stream?fileName=data.csv
Content-Type: application/octet-stream
参数: fileName (String, 必需): 文件名
请求体: CSV数据流
```

### 2. 数据预览
```
GET /csv/api/preview/{fileId}?enablePaging=true&page=0&size=20

参数:
- fileId: 文件ID (路径参数)
- enablePaging: 是否启用分页 (可选，默认true)
- page: 页码 (可选，启用分页时有效)
- size: 每页行数 (可选，启用分页时有效)

响应:
{
  "success": true,
  "fileName": "example.csv",
  "headers": ["列1", "列2"],
  "rows": [["值1", "值2"], ["值3", "值4"]],
  "totalRows": 100,
  "displayedRows": 20,
  "isPaged": true,
  "currentPage": 0,
  "pageSize": 20,
  "totalPages": 5
}
```

### 3. Excel下载
```
GET /csv/api/download/{fileId}

参数:
- fileId: 文件ID (路径参数)

响应: Excel文件流
```

### 4. 文件信息
```
GET /csv/api/files/{fileId}/info

响应:
{
  "success": true,
  "fileName": "example.csv",
  "totalRows": 100,
  "totalColumns": 5,
  "headers": ["列1", "列2", "列3", "列4", "列5"]
}
```

### 5. 删除文件
```
DELETE /csv/api/files/{fileId}

响应:
{
  "success": true,
  "message": "文件已删除"
}
```

## 重载方法API（独立处理）

### CsvService重载方法

#### 1. 直接预览方法
```java
// 从File预览（全量）
CsvData previewCsvFromFile(File file, String fileName)

// 从File预览（分页）
CsvData previewCsvFromFile(File file, String fileName, int page, int size)

// 从InputStream预览（全量）
CsvData previewCsvFromInputStream(InputStream inputStream, String fileName)

// 从InputStream预览（分页）
CsvData previewCsvFromInputStream(InputStream inputStream, String fileName, int page, int size)
```

#### 2. 直接转换方法
```java
// 从File转换为Excel
byte[] convertFileToExcel(File file, String fileName)

// 从InputStream转换为Excel
byte[] convertInputStreamToExcel(InputStream inputStream, String fileName)
```

#### 3. 使用示例
```java
@Autowired
private CsvService csvService;

// 示例1：处理本地文件
File csvFile = new File("/path/to/data.csv");
CsvData data = csvService.previewCsvFromFile(csvFile, "data.csv");
byte[] excel = csvService.convertFileToExcel(csvFile, "data.csv");

// 示例2：处理数据流
InputStream stream = new ByteArrayInputStream(csvContent.getBytes());
CsvData data = csvService.previewCsvFromInputStream(stream, "data.csv");
byte[] excel = csvService.convertInputStreamToExcel(stream, "data.csv");

// 示例3：分页预览
CsvData pagedData = csvService.previewCsvFromFile(csvFile, "data.csv", 0, 10);
```

## 开发说明

### 扩展功能

1. **持久化存储**: 将内存存储替换为Redis或数据库存储
2. **文件格式支持**: 扩展支持其他格式如TSV、Excel等
3. **数据验证**: 添加CSV数据的格式验证和清洗功能
4. **用户管理**: 添加用户认证和权限管理
5. **批量处理**: 支持批量上传和处理多个文件
6. **文件过期**: 添加文件自动过期和清理机制

### 性能优化

1. **流式处理**: 对于超大文件，可以采用流式读取和处理
2. **缓存机制**: 添加Redis缓存提高响应速度
3. **异步处理**: 大文件处理采用异步方式
4. **CDN加速**: 静态资源使用CDN加速
5. **连接池**: 数据库连接池优化
6. **压缩传输**: 启用Gzip压缩减少传输大小

## 常见问题

**Q: 支持哪些CSV编码格式？**
A: 目前主要支持UTF-8编码，如需支持其他编码可以修改`CsvService`中的编码设置。

**Q: Excel文件格式是什么？**
A: 生成的是Excel 2007+格式(.xlsx)，使用Apache POI的XSSFWorkbook。

**Q: 如何处理包含特殊字符的CSV？**
A: 应用使用Apache Commons CSV库，能够正确处理包含逗号、引号等特殊字符的CSV文件。

**Q: 可以同时处理多个文件吗？**
A: 当前版本一次只能处理一个文件，如需批量处理功能需要扩展开发。

## 许可证

本项目采用MIT许可证，详情请参阅LICENSE文件。