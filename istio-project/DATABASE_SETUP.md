# MySQL 数据库设置指南

## 数据库连接信息

项目已配置为使用MySQL数据库，每个服务使用独立的数据库：

### Order Service
- **主机**: localhost
- **端口**: 33306
- **数据库**: order_db
- **用户名**: root
- **密码**: 123456

### User Service
- **主机**: localhost
- **端口**: 33306
- **数据库**: user_db
- **用户名**: root
- **密码**: 123456

## 预置要求

1. 确保MySQL服务器已启动并运行在端口33306
2. 确保数据库`order_db`和`user_db`已创建
3. 确保用户`root`有权限访问这两个数据库
4. 确保root用户密码设置为`123456`

## 数据库表结构

### 用户服务表 (users)
```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100),
    full_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE'
);
```

### 订单服务表 (orders)
```sql
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 自动初始化

应用启动时会自动：

1. **创建表结构**: 通过`schema.sql`文件
2. **插入初始数据**: 通过`data.sql`文件
3. **更新表结构**: 通过JPA的`ddl-auto: update`配置

## 手动初始化（可选）

如果需要手动创建数据库和表：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户表（在user_db中）
USE user_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100),
    full_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- 创建订单表（在order_db中）
USE order_db;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
```

## 验证连接

启动应用后，检查日志确认数据库连接成功：

```bash
# 查看应用日志
tail -f logs/user-service.log
tail -f logs/order-service.log
```

成功连接后应该看到类似的日志：
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

## 故障排除

### 1. 连接被拒绝
```
java.sql.SQLNonTransientConnectionException: Could not connect to address=(host=localhost)(port=33306)
```

**解决方案**:
- 检查MySQL服务是否运行
- 确认端口33306是否正确
- 检查防火墙设置

### 2. 数据库不存在
```
java.sql.SQLSyntaxErrorException: Unknown database 'order_db' or 'user_db'
```

**解决方案**:
```sql
CREATE DATABASE order_db;
CREATE DATABASE user_db;
```

### 3. 用户权限问题
```
java.sql.SQLException: Access denied for user 'root'@'localhost'
```

**解决方案**:
```sql
GRANT ALL PRIVILEGES ON order_db.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON user_db.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

## Docker方式启动MySQL（可选）

如果没有本地MySQL，可以使用Docker快速启动：

```bash
docker run --name mysql-istio-demo \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=order_db \
  -p 33306:3306 \
  -d mysql:8.0

# 等待MySQL启动完成
sleep 30

# 验证连接
docker exec mysql-istio-demo mysql -uroot -p123456 -e "SHOW DATABASES;"

# 创建第二个数据库
docker exec mysql-istio-demo mysql -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS user_db;"
``` 

## 快速数据库设置脚本

使用以下SQL脚本快速创建所需的数据库：

```sql
-- 连接到MySQL服务器（端口33306）
-- mysql -uroot -p123456 -h localhost -P 33306

-- 创建数据库
CREATE DATABASE IF NOT EXISTS order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 授权（如果需要）
GRANT ALL PRIVILEGES ON order_db.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON user_db.* TO 'root'@'localhost';
FLUSH PRIVILEGES;

-- 验证
SHOW DATABASES;
```

保存以上内容为 `setup_databases.sql`，然后执行：

```bash
mysql -uroot -p123456 -h localhost -P 33306 < setup_databases.sql
```