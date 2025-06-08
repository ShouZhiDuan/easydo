# SQL to Excel Exporter

一个基于Spring Boot的高性能SQL查询结果导出Excel工具，支持批量SQL执行、数据脱敏、并行处理等功能。

## 功能特性

### 核心功能
- ✅ **批量SQL执行**: 支持一次性执行多个SQL查询语句
- ✅ **Excel导出**: 将查询结果导出到Excel文件，每个SQL结果对应一个Sheet
- ✅ **自定义Sheet名称**: 用户可以为每个Sheet指定名称
- ✅ **数据脱敏**: 支持手机号、身份证、邮箱等常见字段的脱敏处理
- ✅ **并行执行**: 支持并行执行SQL查询，提高处理效率
- ✅ **安全验证**: SQL安全性检查，防止危险操作

### 性能优化
- 🚀 **流式处理**: 使用SXSSFWorkbook支持大数据量导出
- 🚀 **连接池**: 配置数据库连接池，提高数据库访问效率
- 🚀 **缓存机制**: 脱敏规则正则表达式缓存，提高处理速度
- 🚀 **内存控制**: 可配置内存中保持的行数，避免内存溢出

### 安全特性
- 🔒 **SQL注入防护**: 基础SQL安全性验证
- 🔒 **数据脱敏**: 多种脱敏规则，保护敏感数据
- 🔒 **文件访问控制**: 限制文件下载路径，防止路径遍历攻击

## 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- 数据库（MySQL/PostgreSQL/H2等）

### 安装运行

1. **克隆项目**
```bash
git clone <repository-url>
cd sql-csv
```

2. **配置数据库**
编辑 `src/main/resources/application.yml` 文件，配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: your_username
    password: your_password
```

3. **编译运行**
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run
```

4. **访问应用**
- 应用地址: http://localhost:8080
- H2控制台: http://localhost:8080/h2-console (仅测试环境)
- 健康检查: http://localhost:8080/api/sql-export/health

## API 使用说明

### 1. 执行SQL并导出Excel

**接口**: `POST /api/sql-export/export`

**请求示例**:
```json
{
  "sqlList": [
    "SELECT id, username, email, phone FROM users LIMIT 100",
    "SELECT id, product_name, price FROM products WHERE price > 1000"
  ],
  "sheetNames": ["用户数据", "产品数据"],
  "fileName": "export_report.xlsx",
  "parallelExecution": false,
  "validateSqlSafety": true,
  "maskingRules": [
    {
      "fieldName": "phone",
      "maskingType": "PHONE",
      "enabled": true
    },
    {
      "fieldName": "email",
      "maskingType": "EMAIL",
      "enabled": true
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "导出成功",
  "success": true,
  "filePath": "/path/to/export_report.xlsx",
  "fileSize": 15360,
  "sqlCount": 2,
  "totalRecords": 150,
  "recordCounts": [100, 50],
  "processingTimeMs": 1250,
  "createdAt": "2024-01-15T10:30:00"
}
```

### 2. 下载导出文件

**接口**: `GET /api/sql-export/download?filePath=/path/to/file.xlsx`

### 3. 获取统计信息

**接口**: `GET /api/sql-export/statistics`

### 4. 清理过期文件

**接口**: `DELETE /api/sql-export/cleanup?daysToKeep=7`

## 数据脱敏配置

### 支持的脱敏类型

| 类型 | 说明 | 示例 |
|------|------|------|
| PHONE | 手机号脱敏 | 138****5678 |
| ID_CARD | 身份证脱敏 | 110101********1234 |
| EMAIL | 邮箱脱敏 | abc***@example.com |
| BANK_CARD | 银行卡脱敏 | 6222****7890 |
| NAME | 姓名脱敏 | 张*三 |
| CUSTOM | 自定义规则 | 用户自定义正则 |

### 脱敏规则配置示例

```json
{
  "maskingRules": [
    {
      "fieldName": "phone",
      "maskingType": "PHONE",
      "enabled": true
    },
    {
      "fieldName": "custom_field",
      "maskingType": "CUSTOM",
      "customRegex": "(\\d{4})\\d*(\\d{4})",
      "customReplacement": "$1****$2",
      "enabled": true
    }
  ]
}
```

## 配置说明

### 应用配置

```yaml
app:
  export:
    output-directory: ./exports          # 导出文件目录
    max-sql-count: 50                   # 最大SQL数量
    max-records-per-query: 100000       # 单个查询最大记录数
    enable-parallel-execution: true      # 是否启用并行执行
    default-file-retention-days: 7       # 默认文件保留天数
  
  security:
    enable-sql-validation: true          # 是否启用SQL安全验证
    allowed-download-paths:              # 允许下载的路径
      - ./exports
  
  performance:
    excel-rows-in-memory: 1000          # Excel内存中保持的行数
    sql-timeout-seconds: 300            # SQL执行超时时间
    thread-pool-size: 4                 # 线程池大小
```

### 数据库配置

支持多种数据库：

**MySQL**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/database?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: username
    password: password
```

**PostgreSQL**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/database
    driver-class-name: org.postgresql.Driver
    username: username
    password: password
```

## 测试

### 运行单元测试
```bash
mvn test
```

### 运行集成测试
```bash
mvn verify
```

### 测试覆盖率
```bash
mvn jacoco:report
```

## 使用示例

### 1. 基础导出

```bash
curl -X POST http://localhost:8080/api/sql-export/export \
  -H "Content-Type: application/json" \
  -d '{
    "sqlList": ["SELECT * FROM users LIMIT 10"],
    "sheetNames": ["用户列表"]
  }'
```

### 2. 带脱敏的导出

```bash
curl -X POST http://localhost:8080/api/sql-export/export \
  -H "Content-Type: application/json" \
  -d '{
    "sqlList": ["SELECT username, phone, email FROM users"],
    "sheetNames": ["用户信息"],
    "maskingRules": [
      {"fieldName": "phone", "maskingType": "PHONE", "enabled": true},
      {"fieldName": "email", "maskingType": "EMAIL", "enabled": true}
    ]
  }'
```

### 3. 并行执行多个查询

```bash
curl -X POST http://localhost:8080/api/sql-export/export \
  -H "Content-Type: application/json" \
  -d '{
    "sqlList": [
      "SELECT * FROM users",
      "SELECT * FROM orders",
      "SELECT * FROM products"
    ],
    "sheetNames": ["用户", "订单", "产品"],
    "parallelExecution": true
  }'
```

## 性能建议

1. **大数据量处理**:
   - 使用LIMIT限制查询结果数量
   - 启用并行执行提高效率
   - 适当调整`excel-rows-in-memory`参数

2. **内存优化**:
   - 避免一次性查询过多数据
   - 及时清理过期文件
   - 监控JVM内存使用情况

3. **数据库优化**:
   - 确保查询SQL有适当的索引
   - 配置合适的连接池大小
   - 避免长时间运行的查询

## 故障排除

### 常见问题

1. **内存溢出**
   - 减少`max-records-per-query`配置
   - 增加JVM堆内存: `-Xmx2g`
   - 减少`excel-rows-in-memory`配置

2. **SQL执行超时**
   - 增加`sql-timeout-seconds`配置
   - 优化SQL查询性能
   - 检查数据库连接状态

3. **文件生成失败**
   - 检查输出目录权限
   - 确保磁盘空间充足
   - 查看应用日志错误信息

### 日志配置

```yaml
logging:
  level:
    com.example.sqlcsv: DEBUG
  file:
    name: ./logs/sql-csv-exporter.log
```

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 更新日志

### v1.0.0
- 初始版本发布
- 支持批量SQL查询和Excel导出
- 实现数据脱敏功能
- 添加并行执行支持
- 完善安全验证机制