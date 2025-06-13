#!/bin/bash

# CSV导入工具快速启动脚本

echo "=== CSV导入工具快速启动脚本 ==="
echo

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装Java 11+"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请先安装Maven 3.6+"
    exit 1
fi

# 构建项目
echo "正在构建项目..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "错误: 项目构建失败"
    exit 1
fi

echo "项目构建成功！"
echo

# 检查是否存在示例数据文件
if [ ! -f "sample-data.csv" ]; then
    echo "错误: 未找到示例数据文件 sample-data.csv"
    exit 1
fi

# 提示用户选择运行模式
echo "请选择运行模式:"
echo "1. 交互式模式 (推荐新手使用)"
echo "2. 命令行模式 (使用示例数据)"
echo "3. 查看使用帮助"
echo
read -p "请输入选择 (1-3): " choice

case $choice in
    1)
        echo "启动交互式模式..."
        java -jar target/csv-import-1.0.0.jar --interactive
        ;;
    2)
        echo "请输入数据库连接信息:"
        read -p "数据库URL (例: jdbc:mysql://localhost:3306/testdb): " db_url
        read -p "用户名: " db_user
        read -s -p "密码: " db_password
        echo
        read -p "索引列 (可选，多个用逗号分隔): " index_cols
        
        echo "开始导入示例数据..."
        java -jar target/csv-import-1.0.0.jar \
            "sample-data.csv" \
            "$db_url" \
            "$db_user" \
            "$db_password" \
            "$index_cols" \
            1000 \
            100
        ;;
    3)
        echo "显示使用帮助..."
        java -jar target/csv-import-1.0.0.jar
        ;;
    *)
        echo "无效选择，退出"
        exit 1
        ;;
esac

echo
echo "脚本执行完成！"