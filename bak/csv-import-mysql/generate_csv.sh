#!/bin/bash

# CSV大数据生成脚本
# 使用方法: ./generate_csv.sh [行数] [输出文件名]

set -e  # 遇到错误立即退出

# 默认参数
DEFAULT_ROWS=1000000
DEFAULT_OUTPUT="large_sample_data.csv"

# 获取参数
ROWS=${1:-$DEFAULT_ROWS}
OUTPUT_FILE=${2:-$DEFAULT_OUTPUT}

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== CSV大数据生成工具 ===${NC}"
echo -e "${YELLOW}目标行数: ${ROWS}${NC}"
echo -e "${YELLOW}输出文件: ${OUTPUT_FILE}${NC}"
echo ""

# 检查Python是否安装
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}错误: 未找到python3，请先安装Python 3${NC}"
    exit 1
fi

# 检查磁盘空间（粗略估算：每行约100字节）
ESTIMATED_SIZE_MB=$((ROWS * 100 / 1024 / 1024))
echo -e "${YELLOW}预估文件大小: ~${ESTIMATED_SIZE_MB} MB${NC}"

# 检查可用磁盘空间
AVAILABLE_SPACE=$(df . | tail -1 | awk '{print $4}')
AVAILABLE_SPACE_MB=$((AVAILABLE_SPACE / 1024))

if [ $ESTIMATED_SIZE_MB -gt $AVAILABLE_SPACE_MB ]; then
    echo -e "${RED}警告: 磁盘空间可能不足！${NC}"
    echo -e "${RED}需要: ~${ESTIMATED_SIZE_MB} MB, 可用: ${AVAILABLE_SPACE_MB} MB${NC}"
    read -p "是否继续？(y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "已取消操作"
        exit 1
    fi
fi

# 创建临时Python脚本
cat > temp_generator.py << 'EOF'
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import csv
import random
import sys
import os
from datetime import datetime, timedelta

# 从命令行参数获取配置
TOTAL_ROWS = int(sys.argv[1]) if len(sys.argv) > 1 else 1000000
OUTPUT_FILE = sys.argv[2] if len(sys.argv) > 2 else "large_sample_data.csv"
BATCH_SIZE = 10000

# 测试数据池
CHINESE_SURNAMES = ["张", "李", "王", "赵", "钱", "孙", "周", "吴", "郑", "冯", "陈", "褚", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金"]
CHINESE_NAMES = ["伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "军", "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀英", "华", "慧", "建华", "建国", "建军", "志强", "志明", "秀兰", "秀珍", "秀云", "桂英", "桂花"]
DEPARTMENTS = ["技术部", "销售部", "市场部", "人事部", "财务部", "运营部", "产品部", "设计部", "客服部", "法务部", "研发部", "质量部", "采购部", "物流部", "行政部"]
EMAIL_DOMAINS = ["example.com", "test.com", "demo.com", "company.com", "corp.com", "enterprise.com"]

def generate_chinese_name():
    surname = random.choice(CHINESE_SURNAMES)
    given_name = random.choice(CHINESE_NAMES)
    if random.random() < 0.3:
        given_name += random.choice(CHINESE_NAMES)
    return surname + given_name

def generate_email(user_id):
    domain = random.choice(EMAIL_DOMAINS)
    prefixes = [f"user{user_id}", f"emp{user_id}", f"staff{user_id}", f"member{user_id}"]
    return f"{random.choice(prefixes)}@{domain}"

def generate_date():
    start_date = datetime(2020, 1, 1)
    end_date = datetime(2024, 12, 31)
    time_between = end_date - start_date
    days_between = time_between.days
    random_days = random.randrange(days_between)
    random_date = start_date + timedelta(days=random_days)
    return random_date.strftime("%Y-%m-%d")

def generate_row(row_id):
    name = generate_chinese_name()
    age = random.randint(22, 65)
    # 薪资分布更真实：大部分在5-15万，少数高薪
    if random.random() < 0.8:
        salary = round(random.uniform(50000, 150000), 2)
    else:
        salary = round(random.uniform(150000, 500000), 2)
    
    is_active = random.choices(["true", "false"], weights=[0.8, 0.2])[0]  # 80%激活
    created_date = generate_date()
    email = generate_email(row_id)
    department = random.choice(DEPARTMENTS)
    
    return [row_id, name, age, salary, is_active, created_date, email, department]

def main():
    print(f"开始生成 {TOTAL_ROWS:,} 行CSV数据...")
    
    headers = ["id", "name", "age", "salary", "is_active", "created_date", "email", "department"]
    
    try:
        with open(OUTPUT_FILE, 'w', newline='', encoding='utf-8') as csvfile:
            writer = csv.writer(csvfile)
            writer.writerow(headers)
            
            for batch_start in range(1, TOTAL_ROWS + 1, BATCH_SIZE):
                batch_end = min(batch_start + BATCH_SIZE - 1, TOTAL_ROWS)
                batch_data = [generate_row(row_id) for row_id in range(batch_start, batch_end + 1)]
                writer.writerows(batch_data)
                
                progress = (batch_end / TOTAL_ROWS) * 100
                print(f"\r进度: {progress:.1f}% ({batch_end:,}/{TOTAL_ROWS:,})", end='', flush=True)
        
        print(f"\n\n✅ 数据生成完成！")
        file_size = os.path.getsize(OUTPUT_FILE) / (1024 * 1024)
        print(f"📁 文件: {OUTPUT_FILE}")
        print(f"📊 行数: {TOTAL_ROWS:,}")
        print(f"💾 大小: {file_size:.2f} MB")
        
    except Exception as e:
        print(f"\n❌ 错误: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
EOF

echo -e "${GREEN}开始生成数据...${NC}"
echo ""

# 记录开始时间
START_TIME=$(date +%s)

# 运行Python脚本
python3 temp_generator.py "$ROWS" "$OUTPUT_FILE"

# 计算耗时
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo -e "${GREEN}=== 生成完成 ===${NC}"
echo -e "${BLUE}总耗时: ${DURATION} 秒${NC}"
echo -e "${BLUE}平均速度: $((ROWS / DURATION)) 行/秒${NC}"

# 清理临时文件
rm -f temp_generator.py

# 显示文件信息
if [ -f "$OUTPUT_FILE" ]; then
    echo ""
    echo -e "${YELLOW}文件信息:${NC}"
    ls -lh "$OUTPUT_FILE"
    echo ""
    echo -e "${YELLOW}前5行预览:${NC}"
    head -5 "$OUTPUT_FILE"
fi

echo ""
echo -e "${GREEN}🎉 CSV文件生成成功！${NC}"
echo -e "${BLUE}可以使用以下命令导入到MySQL:${NC}"
echo -e "${YELLOW}java -jar target/csv-import-1.0.0.jar $OUTPUT_FILE <数据库URL> <用户名> <密码>${NC}"