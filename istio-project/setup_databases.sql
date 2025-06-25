-- Istio Demo 项目数据库设置脚本
-- 使用方法: mysql -uroot -p123456 -h localhost -P 33306 < setup_databases.sql

-- 创建数据库
CREATE DATABASE IF NOT EXISTS order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 显示创建结果
SHOW DATABASES;

-- 选择数据库并验证
USE order_db;
SELECT DATABASE() AS 'Current Database', 'Order database selected' AS Status;

USE user_db;
SELECT DATABASE() AS 'Current Database', 'User database selected' AS Status;

-- 显示配置信息
SELECT 
    'Database setup completed successfully!' AS Message,
    'order_db and user_db created' AS Status,
    'Ready for application startup' AS NextStep; 