# CSV Import Tool

一个高性能的CSV文件导入MySQL数据库的Spring Boot工具，支持自动类型推断、动态建表、流式处理和批量导入。

## 主要特性

- 🚀 **高性能**: 使用HikariCP连接池和批量操作，支持大文件快速导入
- 🧠 **智能类型推断**: 自动分析CSV数据并推断最适合的MySQL字段类型
- 📊 **动态建表**: 根据CSV文件结构自动创建对应的MySQL表
- 🔄 **流式处理**: 边读取CSV边插入数据库，内存占用低
- 📈 **批量操作**: 支持自定义批次大小的批量插入
- 🔍 **自动索引**: 支持指定字段自动创建索引
- ⚙️ **灵活配置**: 支持动态数据库连接配置
- 🎯 **解耦设计**: CSV处理和数据库操作完全分离

## 技术栈

- Spring Boot 2.7.14
- Spring JDBC Template
- HikariCP 连接池
- Apache Commons CSV
- MySQL 8.0+
- Maven

## 快速开始

### 1. 环境要求

- Java 11+
- Maven 3.6+
- MySQL 8.0+

### 2. 构建项目

```bash
mvn clean package
```

### 3. 使用方式

#### 交互式模式

```bash
java -jar target/csv-import-1.0.0.jar --interactive
```

按提示输入CSV文件路径、数据库连接信息等参数。

#### 命令行模式

```bash
java -jar target/csv-import-1.0.0.jar \
  /path/to/data.csv \
  "jdbc:mysql://localhost:3306/testdb" \
  root \
  password \
  "id,user_id,created_date" \
  2000 \
  1000
```

参数说明：
- 参数1: CSV文件路径
- 参数2: MySQL数据库URL
- 参数3: 数据库用户名
- 参数4: 数据库密码
- 参数5: 索引列名（可选，多个用逗号分隔）
- 参数6: 批次大小（可选，默认1000）
- 参数7: 样本大小（可选，默认1000）

#### 编程方式使用

```java
@Autowired
private CsvImportService csvImportService;

public void importData() {
    // 配置数据库连接
    DatabaseConfig config = new DatabaseConfig(
        "jdbc:mysql://localhost:3306/testdb",
        "root",
        "password"
    );
    
    // 指定需要创建索引的列
    List<String> indexColumns = Arrays.asList("id", "user_id");
    
    // 执行导入
    csvImportService.importCsv(
        "/path/to/data.csv", 
        config, 
        indexColumns, 
        2000,  // 批次大小
        1000   // 样本大小
    );
    
    // 关闭连接
    csvImportService.closeConnection();
}
```

## 核心组件

### 1. CsvService
负责CSV文件处理：
- 解析CSV文件头部
- 数据类型推断
- 流式数据读取
- 表名提取

### 2. DatabaseService
负责数据库操作：
- 动态创建数据库连接
- 创建表结构
- 批量数据插入
- 创建索引

### 3. CsvImportService
主要业务逻辑：
- 整合CSV处理和数据库操作
- 控制导入流程
- 错误处理和日志记录

## 数据类型映射

| CSV数据特征 | 推断的Java类型 | MySQL字段类型 |
|------------|---------------|---------------|
| 纯整数 | INTEGER | INT |
| 长整数(10位+) | LONG | BIGINT |
| 小数 | DOUBLE | DOUBLE |
| true/false/yes/no/1/0 | BOOLEAN | BOOLEAN |
| 日期格式 | DATE | DATE |
| 日期时间格式 | TIMESTAMP | TIMESTAMP |
| 其他 | STRING | VARCHAR/TEXT |

## 性能优化

### 1. 连接池优化
```java
DatabaseConfig config = new DatabaseConfig(url, username, password);
config.setMaximumPoolSize(50);     // 最大连接数
config.setMinimumIdle(20);         // 最小空闲连接
config.setConnectionTimeout(30000); // 连接超时
```

### 2. 批量操作优化
- 使用`rewriteBatchedStatements=true`参数
- 适当增加批次大小（建议1000-10000）
- 启用预编译语句缓存

### 3. MySQL配置优化
```sql
-- 临时禁用约束检查（导入时）
SET foreign_key_checks = 0;
SET unique_checks = 0;
SET autocommit = 0;

-- 调整缓冲区大小
SET innodb_buffer_pool_size = 1G;
SET bulk_insert_buffer_size = 256M;
```

## 配置说明

### application.yml配置

```yaml
csv-import:
  default:
    batch-size: 1000        # 默认批次大小
    sample-size: 1000       # 默认样本大小
    connection-timeout: 30000 # 连接超时时间
    max-pool-size: 20       # 最大连接池大小
    min-idle: 5             # 最小空闲连接数
  
  performance:
    enable-batch-rewrite: true      # 启用批量重写
    enable-prep-stmt-cache: true    # 启用预编译缓存
    prep-stmt-cache-size: 250       # 缓存大小
```

## 错误处理

- **文件不存在**: 检查CSV文件路径是否正确
- **数据库连接失败**: 检查数据库URL、用户名、密码
- **类型转换错误**: 工具会自动降级为字符串类型
- **内存不足**: 减少批次大小或增加JVM堆内存

## 最佳实践

1. **大文件处理**: 使用较大的批次大小（5000-10000）
2. **类型推断**: 增加样本大小提高准确性
3. **索引策略**: 在数据导入完成后创建索引
4. **连接池**: 根据并发需求调整连接池大小
5. **监控**: 关注日志输出，监控导入进度

## 示例数据

创建测试CSV文件 `users.csv`：
```csv
id,name,age,salary,is_active,created_date
1,John Doe,25,50000.50,true,2023-01-01
2,Jane Smith,30,60000.75,false,2023-01-02
3,Bob Johnson,35,70000.00,true,2023-01-03
```

导入命令：
```bash
java -jar csv-import-1.0.0.jar \
  users.csv \
  "jdbc:mysql://localhost:3306/testdb" \
  root \
  password \
  "id,name"
```

生成的MySQL表结构：
```sql
CREATE TABLE `users` (
  `id` INT,
  `name` VARCHAR(255),
  `age` INT,
  `salary` DOUBLE,
  `is_active` BOOLEAN,
  `created_date` DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX `idx_users_id` ON `users` (`id`);
CREATE INDEX `idx_users_name` ON `users` (`name`);
```

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！