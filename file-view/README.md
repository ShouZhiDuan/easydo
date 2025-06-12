# Excel文件预览器

基于SpringBoot的Excel文件在线预览系统，支持上传Excel文件并在Web页面中预览内容。

## 功能特点

- 📊 支持.xlsx和.xls格式的Excel文件
- 🚀 现代化的Web界面设计
- 📁 支持拖拽上传和点击上传
- 📈 多工作表预览支持
- 📱 响应式设计，支持移动端
- 🔧 完整的单元测试覆盖
- 🌐 前后端分离架构

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.0
- Apache POI 5.2.4
- Maven
- JUnit 5

### 前端
- 原生HTML5 + CSS3 + JavaScript
- 现代化CSS设计
- Fetch API

## 项目结构

```
excel-viewer/
├── src/
│   ├── main/
│   │   ├── java/com/example/excelviewer/
│   │   │   ├── ExcelViewerApplication.java    # 主启动类
│   │   │   ├── controller/
│   │   │   │   └── ExcelController.java       # REST API控制器
│   │   │   ├── service/
│   │   │   │   └── ExcelService.java          # Excel处理服务
│   │   │   └── model/
│   │   │       └── ExcelData.java             # 数据模型
│   │   └── resources/
│   │       ├── application.yml                # 应用配置
│   │       └── static/
│   │           └── index.html                 # 前端页面
│   └── test/                                  # 单元测试
├── pom.xml                                    # Maven依赖配置
└── README.md                                  # 项目说明
```

## API接口

### 1. 上传并预览Excel文件
- **URL**: `POST /api/excel/preview`
- **参数**: `file` (multipart/form-data)
- **响应**: JSON格式的Excel数据

### 2. 预览服务器Excel文件
- **URL**: `GET /api/excel/preview/{filePath}`
- **参数**: `filePath` - 文件路径
- **响应**: JSON格式的Excel数据

### 3. 健康检查
- **URL**: `GET /api/excel/health`
- **响应**: 服务状态信息

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+

### 2. 运行项目

```bash
# 克隆项目
git clone <repository-url>
cd excel-viewer

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run
```

### 3. 访问应用
打开浏览器访问：http://localhost:8080

## 使用说明

1. **上传文件**：点击"选择文件"按钮或直接拖拽Excel文件到上传区域
2. **预览内容**：文件上传后会自动解析并显示Excel内容
3. **切换工作表**：如果Excel文件包含多个工作表，可以点击标签页切换
4. **查看统计**：页面顶部显示文件的基本统计信息

## API使用示例

### cURL示例

```bash
# 上传文件预览
curl -X POST "http://localhost:8080/api/excel/preview" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/your/excel.xlsx"

# 预览服务器文件
curl -X GET "http://localhost:8080/api/excel/preview/sample.xlsx"

# 健康检查
curl -X GET "http://localhost:8080/api/excel/health"
```

### JavaScript示例

```javascript
// 上传文件
const formData = new FormData();
formData.append('file', fileInput.files[0]);

fetch('/api/excel/preview', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(data => {
    if (data.success) {
        console.log('Excel数据:', data.data);
    }
});
```

## 配置说明

### application.yml配置项

```yaml
server:
  port: 8080                    # 服务端口

spring:
  servlet:
    multipart:
      max-file-size: 10MB       # 最大文件大小
      max-request-size: 10MB    # 最大请求大小
```

## 开发指南

### 添加新功能
1. 在`ExcelService`中添加业务逻辑
2. 在`ExcelController`中添加REST接口
3. 编写相应的单元测试

### 单元测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=ExcelServiceTest

# 生成测试报告
mvn test jacoco:report
```

## 性能优化

- 限制每个工作表最大读取1000行数据
- 支持大文件分批处理
- 前端使用虚拟滚动优化大数据表格显示

## 安全考虑

- 文件类型验证
- 文件大小限制
- 输入参数校验
- CORS跨域配置

## 常见问题

### Q: 支持哪些Excel文件格式？
A: 支持.xlsx（Excel 2007+）和.xls（Excel 97-2003）格式。

### Q: 文件大小有限制吗？
A: 默认限制为10MB，可在application.yml中调整。

### Q: 如何处理大文件？
A: 系统会限制每个工作表读取的行数，避免内存溢出。

### Q: 是否支持公式计算？
A: 支持基本的公式计算，复杂公式可能需要Excel环境。

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交Issue或PR。 