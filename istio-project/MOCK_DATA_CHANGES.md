# Mock数据替换说明 - 移除数据库依赖

## 背景
为了简化开发调试和专注于Istio功能演示，将项目中的数据库操作全部改为内存Mock数据实现。这样可以：
- 避免数据库配置和连接问题
- 快速启动和测试
- 专注于Istio Service Mesh功能
- 简化部署流程

## 主要更改

### 1. Maven依赖清理

#### 移除的依赖
```xml
<!-- 已注释的数据库相关依赖 -->
<!-- 
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
-->
```

### 2. 模型类简化

#### Order.java 和 User.java
- 移除 `@Entity`、`@Table`、`@Id`、`@GeneratedValue` 等JPA注解
- 移除 `@Column` 注解
- 移除 `@PrePersist`、`@PreUpdate` 生命周期注解
- 保留验证注解 (`@NotNull`、`@Email`、`@NotBlank`)
- 改为纯POJO类

```java
// 修改前
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

// 修改后
// 移除JPA注解 - 现在是纯POJO类用于Mock数据
public class Order {
    // 移除JPA注解
    private Long id;
```

### 3. Repository重构

#### 从接口改为实现类
```java
// 修改前
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);
}

// 修改后
@Repository
public class OrderRepository {
    // 内存存储 - 使用ConcurrentHashMap保证线程安全
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public List<Order> findByUserId(Long userId) {
        return orders.values().stream()
                .filter(order -> Objects.equals(order.getUserId(), userId))
                .collect(Collectors.toList());
    }
}
```

#### 内存存储特性
- 使用 `ConcurrentHashMap` 保证线程安全
- 使用 `AtomicLong` 生成自增ID
- 实现所有原有的查询方法
- 自动初始化Mock数据

### 4. 初始化Mock数据

#### OrderRepository 示例数据
```java
private void initMockData() {
    save(new Order(1L, "MacBook Pro", 1, new BigDecimal("2499.99")));
    save(new Order(1L, "iPhone 15", 2, new BigDecimal("999.99")));
    save(new Order(2L, "iPad Air", 1, new BigDecimal("799.99")));
    save(new Order(2L, "AirPods Pro", 1, new BigDecimal("249.99")));
    save(new Order(3L, "MacBook Air", 1, new BigDecimal("1299.99")));
}
```

#### UserRepository 示例数据
```java
private void initMockData() {
    save(new User("john_doe", "john.doe@example.com", "John Doe", "+1-555-0101"));
    save(new User("jane_smith", "jane.smith@example.com", "Jane Smith", "+1-555-0102"));
    save(new User("bob_wilson", "bob.wilson@example.com", "Bob Wilson", "+1-555-0103"));
    save(new User("alice_brown", "alice.brown@example.com", "Alice Brown", "+1-555-0104"));
    save(new User("charlie_davis", "charlie.davis@example.com", "Charlie Davis", "+1-555-0105"));
}
```

### 5. 配置文件清理

#### application.yml 更改
```yaml
# 移除数据库配置 - 现在使用内存Mock数据
# datasource:
#   url: jdbc:mysql://localhost:33306/order_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
#   driver-class-name: com.mysql.cj.jdbc.Driver
#   username: root
#   password: 123456

# jpa:
#   database-platform: org.hibernate.dialect.MySQL8Dialect
#   hibernate:
#     ddl-auto: update
#   show-sql: false

# sql:
#   init:
#     mode: always
#     schema-locations: classpath:schema.sql
#     data-locations: classpath:data.sql
```

### 6. 删除的文件

#### SQL文件（不再需要）
- `order-service/src/main/resources/schema.sql`
- `order-service/src/main/resources/data.sql`
- `user-service/src/main/resources/schema.sql`
- `user-service/src/main/resources/data.sql`

## 实现的功能

### Repository方法实现
- `findAll()` - 返回所有记录
- `findById(Long id)` - 根据ID查找
- `save(T entity)` - 保存或更新
- `deleteById(Long id)` - 根据ID删除
- `existsById(Long id)` - 检查是否存在
- `count()` - 统计数量

### 自定义查询方法
- `findByUserId(Long userId)` - 根据用户ID查找订单
- `findByStatus(String status)` - 根据状态查找订单
- `findByUsername(String username)` - 根据用户名查找用户
- `findByEmail(String email)` - 根据邮箱查找用户

## 优势

### 1. 简化开发
- 无需配置数据库连接
- 无需创建数据库和表
- 立即启动，无等待时间

### 2. 专注功能
- 重点关注Istio功能演示
- 避免数据库相关问题干扰
- 简化故障排查

### 3. 易于测试
- 数据隔离，每次启动都是全新状态
- 预定义的测试数据
- 无外部依赖

### 4. 部署简单
- 不需要数据库容器
- 减少资源消耗
- 简化Docker编排

## 注意事项

### 1. 数据持久性
- **重启后数据丢失** - 这是内存存储的特性
- 适合演示和开发，不适合生产环境

### 2. 并发安全
- 使用 `ConcurrentHashMap` 保证基本的线程安全
- 适合单机演示，分布式场景需要考虑数据一致性

### 3. 性能特点
- 读写速度极快（内存操作）
- 内存占用随数据量增长
- 适合中小规模演示数据

## API兼容性

### Service层无变化
- 所有业务逻辑保持不变
- Controller层无需修改
- API接口完全兼容

### 测试方式
```bash
# 启动order-service
cd order-service
mvn spring-boot:run

# 启动user-service  
cd user-service
mvn spring-boot:run

# 测试API
curl http://localhost:8080/api/orders
curl http://localhost:8081/api/users
```

## 未来扩展

如果需要恢复数据库支持：
1. 取消注释相关依赖和配置
2. 恢复JPA注解
3. 将Repository改回接口形式
4. 重新创建SQL文件

当前的Mock实现可以作为数据库集成的参考和对比。 