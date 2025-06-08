#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
大规模CSV数据生成脚本
基于sample-data.csv格式生成100万行测试数据
"""

import csv
import random
from datetime import datetime, timedelta
import sys

# 配置参数
TOTAL_ROWS = 1000000  # 生成100万行数据
OUTPUT_FILE = "large_sample_data.csv"
BATCH_SIZE = 10000  # 每批写入1万行，减少内存占用

# 测试数据池
CHINESE_SURNAMES = ["张", "李", "王", "赵", "钱", "孙", "周", "吴", "郑", "冯", "陈", "褚", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤"]
CHINESE_NAMES = ["伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "军", "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀英", "华", "慧"]
DEPARTMENTS = ["技术部", "销售部", "市场部", "人事部", "财务部", "运营部", "产品部", "设计部", "客服部", "法务部"]
EMAIL_DOMAINS = ["example.com", "test.com", "demo.com", "company.com"]

def generate_chinese_name():
    """生成中文姓名"""
    surname = random.choice(CHINESE_SURNAMES)
    given_name = random.choice(CHINESE_NAMES)
    if random.random() < 0.3:  # 30%概率生成双字名
        given_name += random.choice(CHINESE_NAMES)
    return surname + given_name

def generate_email(name_pinyin, user_id):
    """生成邮箱地址"""
    domain = random.choice(EMAIL_DOMAINS)
    # 简化处理，使用用户ID作为邮箱前缀
    return f"user{user_id}@{domain}"

def generate_date():
    """生成随机日期（2020-2024年间）"""
    start_date = datetime(2020, 1, 1)
    end_date = datetime(2024, 12, 31)
    time_between = end_date - start_date
    days_between = time_between.days
    random_days = random.randrange(days_between)
    random_date = start_date + timedelta(days=random_days)
    return random_date.strftime("%Y-%m-%d")

def generate_row(row_id):
    """生成单行数据"""
    name = generate_chinese_name()
    age = random.randint(22, 65)
    salary = round(random.uniform(30000, 200000), 2)
    is_active = random.choice(["true", "false"])
    created_date = generate_date()
    email = generate_email(name, row_id)
    department = random.choice(DEPARTMENTS)
    
    return [
        row_id,
        name,
        age,
        salary,
        is_active,
        created_date,
        email,
        department
    ]

def main():
    """主函数"""
    print(f"开始生成{TOTAL_ROWS:,}行CSV数据...")
    print(f"输出文件: {OUTPUT_FILE}")
    print(f"批处理大小: {BATCH_SIZE:,}行")
    
    # CSV表头
    headers = ["id", "name", "age", "salary", "is_active", "created_date", "email", "department"]
    
    try:
        with open(OUTPUT_FILE, 'w', newline='', encoding='utf-8') as csvfile:
            writer = csv.writer(csvfile)
            
            # 写入表头
            writer.writerow(headers)
            
            # 分批生成和写入数据
            for batch_start in range(1, TOTAL_ROWS + 1, BATCH_SIZE):
                batch_end = min(batch_start + BATCH_SIZE - 1, TOTAL_ROWS)
                batch_data = []
                
                # 生成当前批次的数据
                for row_id in range(batch_start, batch_end + 1):
                    batch_data.append(generate_row(row_id))
                
                # 写入当前批次
                writer.writerows(batch_data)
                
                # 显示进度
                progress = (batch_end / TOTAL_ROWS) * 100
                print(f"\r进度: {progress:.1f}% ({batch_end:,}/{TOTAL_ROWS:,})", end='', flush=True)
        
        print(f"\n\n✅ 数据生成完成！")
        print(f"📁 文件路径: {OUTPUT_FILE}")
        print(f"📊 总行数: {TOTAL_ROWS:,}行 (包含表头)")
        
        # 显示文件大小
        import os
        file_size = os.path.getsize(OUTPUT_FILE)
        file_size_mb = file_size / (1024 * 1024)
        print(f"💾 文件大小: {file_size_mb:.2f} MB")
        
    except Exception as e:
        print(f"\n❌ 生成过程中出现错误: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()