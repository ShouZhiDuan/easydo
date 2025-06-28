#!/bin/bash

# CSV生成脚本
# 用法: ./generate_csv.sh [表头个数] [数据行数]
# 默认: 5个表头，100行数据

# 设置默认值
DEFAULT_HEADERS=5
DEFAULT_ROWS=100

# 获取参数
HEADER_COUNT=${1:-$DEFAULT_HEADERS}
DATA_ROWS=${2:-$DEFAULT_ROWS}

# 检查参数是否为正整数
if ! [[ "$HEADER_COUNT" =~ ^[1-9][0-9]*$ ]]; then
    echo "错误: 表头个数必须是正整数"
    exit 1
fi

if ! [[ "$DATA_ROWS" =~ ^[1-9][0-9]*$ ]]; then
    echo "错误: 数据行数必须是正整数"
    exit 1
fi

# 定义可能的字段名称
FIELD_NAMES=(
    "id" "name" "email" "phone" "address" "city" "country" "age" "gender" "department"
    "salary" "position" "start_date" "end_date" "status" "category" "type" "description"
    "price" "quantity" "total" "discount" "tax" "score" "rating" "comment" "tag"
    "created_at" "updated_at" "deleted_at" "is_active" "is_verified" "priority"
    "company" "website" "industry" "revenue" "employees" "location" "zip_code"
    "first_name" "last_name" "middle_name" "title" "nickname" "birth_date"
    "product_id" "order_id" "customer_id" "user_id" "session_id" "transaction_id"
    "brand" "model" "version" "color" "size" "weight" "height" "width" "length"
)

# 生成随机表头
generate_headers() {
    local count=$1
    local headers=()
    local used_indices=()
    
    # 随机选择不重复的字段名
    while [ ${#headers[@]} -lt $count ]; do
        local index=$((RANDOM % ${#FIELD_NAMES[@]}))
        
        # 检查是否已使用过这个索引
        local already_used=false
        for used_index in "${used_indices[@]}"; do
            if [ $used_index -eq $index ]; then
                already_used=true
                break
            fi
        done
        
        if [ "$already_used" = false ]; then
            headers+=(${FIELD_NAMES[$index]})
            used_indices+=($index)
        fi
    done
    
    echo "${headers[@]}"
}

# 生成随机数据
generate_random_data() {
    local header=$1
    
    case $header in
        "id"|"user_id"|"customer_id"|"order_id"|"product_id"|"session_id"|"transaction_id")
            echo $((RANDOM % 100000 + 1))
            ;;
        "name"|"first_name"|"last_name")
            local names=("张三" "李四" "王五" "赵六" "孙七" "周八" "吴九" "郑十" "王小明" "李小红")
            echo "${names[$((RANDOM % ${#names[@]}))]}"
            ;;
        "email")
            local domains=("gmail.com" "163.com" "qq.com" "sina.com" "hotmail.com")
            local username="user$((RANDOM % 1000))"
            echo "${username}@${domains[$((RANDOM % ${#domains[@]}))]}"
            ;;
        "phone")
            echo "1$((RANDOM % 9 + 1))$(printf "%09d" $((RANDOM % 1000000000)))"
            ;;
        "age")
            echo $((RANDOM % 60 + 18))
            ;;
        "gender")
            local genders=("男" "女")
            echo "${genders[$((RANDOM % 2))]}"
            ;;
        "city")
            local cities=("北京" "上海" "广州" "深圳" "杭州" "南京" "成都" "武汉" "西安" "重庆")
            echo "${cities[$((RANDOM % ${#cities[@]}))]}"
            ;;
        "country")
            echo "中国"
            ;;
        "department")
            local depts=("技术部" "销售部" "市场部" "人力资源部" "财务部" "运营部" "产品部")
            echo "${depts[$((RANDOM % ${#depts[@]}))]}"
            ;;
        "salary")
            echo $((RANDOM % 50000 + 5000))
            ;;
        "position")
            local positions=("工程师" "经理" "总监" "专员" "主管" "顾问" "分析师")
            echo "${positions[$((RANDOM % ${#positions[@]}))]}"
            ;;
        "price")
            echo "$(echo "scale=2; $((RANDOM % 10000)) / 100" | bc)"
            ;;
        "quantity")
            echo $((RANDOM % 1000 + 1))
            ;;
        "score"|"rating")
            echo $((RANDOM % 100 + 1))
            ;;
        "status")
            local statuses=("active" "inactive" "pending" "completed" "cancelled")
            echo "${statuses[$((RANDOM % ${#statuses[@]}))]}"
            ;;
        "is_active"|"is_verified")
            local bools=("true" "false")
            echo "${bools[$((RANDOM % 2))]}"
            ;;
        *_date|*_at)
            local year=$((RANDOM % 5 + 2020))
            local month=$(printf "%02d" $((RANDOM % 12 + 1)))
            local day=$(printf "%02d" $((RANDOM % 28 + 1)))
            echo "${year}-${month}-${day}"
            ;;
        *)
            # 默认生成随机字符串
            local chars="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            local result=""
            for i in {1..8}; do
                result="${result}${chars:$((RANDOM % ${#chars})):1}"
            done
            echo "$result"
            ;;
    esac
}

# 生成表头
echo "正在生成 $HEADER_COUNT 个随机表头..."
HEADERS=($(generate_headers $HEADER_COUNT))

# 创建文件名（包含时间戳）
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
HEADER_FILE="headers_${TIMESTAMP}.csv"
DATA_FILE="data_${TIMESTAMP}.csv"

# 生成只有表头的CSV文件
echo "正在生成表头文件: $HEADER_FILE"
IFS=','
echo "${HEADERS[*]}" > "$HEADER_FILE"

# 生成含数据的CSV文件
echo "正在生成数据文件: $DATA_FILE (${DATA_ROWS}行数据)"
echo "${HEADERS[*]}" > "$DATA_FILE"

# 生成数据行
for ((i=1; i<=DATA_ROWS; i++)); do
    row_data=()
    for header in "${HEADERS[@]}"; do
        row_data+=($(generate_random_data "$header"))
    done
    echo "${row_data[*]}" >> "$DATA_FILE"
    
    # 显示进度
    if [ $((i % 10)) -eq 0 ] || [ $i -eq $DATA_ROWS ]; then
        echo "已生成 $i/$DATA_ROWS 行数据"
    fi
done

# 恢复IFS
unset IFS

echo ""
echo "✓ 文件生成完成！"
echo "  - 表头文件: $HEADER_FILE"
echo "  - 数据文件: $DATA_FILE"
echo ""
echo "文件信息:"
echo "  - 表头个数: $HEADER_COUNT"
echo "  - 数据行数: $DATA_ROWS"
echo "  - 表头: ${HEADERS[*]}"
echo ""
echo "使用方法:"
echo "  ./generate_csv.sh              # 使用默认参数（5个表头，100行数据）"
echo "  ./generate_csv.sh 10           # 10个表头，100行数据"
echo "  ./generate_csv.sh 8 200        # 8个表头，200行数据" 