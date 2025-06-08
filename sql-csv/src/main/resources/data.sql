-- 测试数据初始化脚本
-- 用于H2数据库的示例数据

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    id_card VARCHAR(18),
    real_name VARCHAR(50),
    age INT,
    gender VARCHAR(10),
    address VARCHAR(200),
    salary DECIMAL(10,2),
    department VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    user_id BIGINT,
    product_name VARCHAR(100),
    quantity INT,
    unit_price DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    order_status VARCHAR(20),
    order_date DATE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建产品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(32) NOT NULL,
    product_name VARCHAR(100),
    category VARCHAR(50),
    price DECIMAL(10,2),
    stock_quantity INT,
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入用户测试数据
INSERT INTO users (username, email, phone, id_card, real_name, age, gender, address, salary, department) VALUES
('zhangsan', 'zhangsan@example.com', '13812345678', '110101199001011234', '张三', 25, '男', '北京市朝阳区xxx街道', 8000.00, '技术部'),
('lisi', 'lisi@example.com', '13987654321', '110101199002022345', '李四', 28, '女', '上海市浦东新区xxx路', 9500.00, '产品部'),
('wangwu', 'wangwu@example.com', '13765432109', '110101199003033456', '王五', 30, '男', '广州市天河区xxx大道', 12000.00, '销售部'),
('zhaoliu', 'zhaoliu@example.com', '13654321098', '110101199004044567', '赵六', 26, '女', '深圳市南山区xxx街', 7500.00, '人事部'),
('sunqi', 'sunqi@example.com', '13543210987', '110101199005055678', '孙七', 32, '男', '杭州市西湖区xxx路', 15000.00, '技术部'),
('zhouba', 'zhouba@example.com', '13432109876', '110101199006066789', '周八', 29, '女', '南京市鼓楼区xxx街道', 8800.00, '财务部'),
('wujiu', 'wujiu@example.com', '13321098765', '110101199007077890', '吴九', 27, '男', '武汉市洪山区xxx路', 9200.00, '技术部'),
('zhengshi', 'zhengshi@example.com', '13210987654', '110101199008088901', '郑十', 31, '女', '成都市锦江区xxx大道', 11000.00, '产品部'),
('liuyi', 'liuyi@example.com', '13109876543', '110101199009099012', '刘一', 24, '男', '重庆市渝北区xxx街', 6800.00, '销售部'),
('chener', 'chener@example.com', '13098765432', '110101199010101123', '陈二', 33, '女', '西安市雁塔区xxx路', 13500.00, '技术部');

-- 插入产品测试数据
INSERT INTO products (product_code, product_name, category, price, stock_quantity, description) VALUES
('P001', 'iPhone 15 Pro', '手机', 7999.00, 100, '苹果最新旗舰手机'),
('P002', 'MacBook Pro 16', '笔记本电脑', 19999.00, 50, '苹果专业级笔记本电脑'),
('P003', 'iPad Air', '平板电脑', 4399.00, 80, '轻薄便携平板电脑'),
('P004', 'AirPods Pro', '耳机', 1899.00, 200, '主动降噪无线耳机'),
('P005', 'Apple Watch Series 9', '智能手表', 2999.00, 120, '健康监测智能手表'),
('P006', 'Magic Keyboard', '键盘', 899.00, 150, '无线蓝牙键盘'),
('P007', 'Magic Mouse', '鼠标', 599.00, 180, '无线蓝牙鼠标'),
('P008', 'Studio Display', '显示器', 11499.00, 30, '27英寸5K显示器'),
('P009', 'Mac Studio', '台式机', 14999.00, 25, '专业级台式电脑'),
('P010', 'HomePod mini', '智能音箱', 749.00, 100, '智能家居音箱');

-- 插入订单测试数据
INSERT INTO orders (order_no, user_id, product_name, quantity, unit_price, total_amount, order_status, order_date) VALUES
('ORD20240101001', 1, 'iPhone 15 Pro', 1, 7999.00, 7999.00, '已完成', '2024-01-01'),
('ORD20240101002', 2, 'MacBook Pro 16', 1, 19999.00, 19999.00, '已完成', '2024-01-02'),
('ORD20240101003', 3, 'iPad Air', 2, 4399.00, 8798.00, '已发货', '2024-01-03'),
('ORD20240101004', 4, 'AirPods Pro', 1, 1899.00, 1899.00, '已完成', '2024-01-04'),
('ORD20240101005', 5, 'Apple Watch Series 9', 1, 2999.00, 2999.00, '处理中', '2024-01-05'),
('ORD20240101006', 6, 'Magic Keyboard', 2, 899.00, 1798.00, '已完成', '2024-01-06'),
('ORD20240101007', 7, 'Magic Mouse', 1, 599.00, 599.00, '已发货', '2024-01-07'),
('ORD20240101008', 8, 'Studio Display', 1, 11499.00, 11499.00, '已完成', '2024-01-08'),
('ORD20240101009', 9, 'Mac Studio', 1, 14999.00, 14999.00, '处理中', '2024-01-09'),
('ORD20240101010', 10, 'HomePod mini', 3, 749.00, 2247.00, '已完成', '2024-01-10'),
('ORD20240101011', 1, 'AirPods Pro', 2, 1899.00, 3798.00, '已完成', '2024-01-11'),
('ORD20240101012', 2, 'iPad Air', 1, 4399.00, 4399.00, '已发货', '2024-01-12'),
('ORD20240101013', 3, 'iPhone 15 Pro', 1, 7999.00, 7999.00, '已完成', '2024-01-13'),
('ORD20240101014', 4, 'Magic Keyboard', 1, 899.00, 899.00, '处理中', '2024-01-14'),
('ORD20240101015', 5, 'Apple Watch Series 9', 2, 2999.00, 5998.00, '已完成', '2024-01-15');

-- 创建视图用于复杂查询测试
CREATE VIEW user_order_summary AS
SELECT 
    u.id as user_id,
    u.username,
    u.real_name,
    u.department,
    COUNT(o.id) as order_count,
    COALESCE(SUM(o.total_amount), 0) as total_spent,
    COALESCE(AVG(o.total_amount), 0) as avg_order_amount,
    MAX(o.order_date) as last_order_date
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
GROUP BY u.id, u.username, u.real_name, u.department;

-- 创建部门统计视图
CREATE VIEW department_stats AS
SELECT 
    department,
    COUNT(*) as employee_count,
    AVG(age) as avg_age,
    AVG(salary) as avg_salary,
    MIN(salary) as min_salary,
    MAX(salary) as max_salary
FROM users
GROUP BY department;