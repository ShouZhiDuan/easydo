#!/bin/bash

# 部署到Surge.sh的脚本
# 使用方法: ./deploy.sh

echo "🚀 开始部署到Surge.sh..."

# 检查是否安装了surge
if ! command -v surge &> /dev/null; then
    echo "❌ Surge未安装，正在安装..."
    npm install -g surge
fi

# 检查是否存在index.html
if [ ! -f "index.html" ]; then
    echo "❌ 未找到index.html文件"
    exit 1
fi

echo "📁 当前目录文件:"
ls -la *.html *.md 2>/dev/null || echo "没有找到相关文件"

echo ""
echo "🌐 开始部署..."
echo "提示: 首次使用需要注册Surge账号"
echo "建议域名格式: your-project-name.surge.sh"
echo ""

# 运行surge部署
surge

echo ""
echo "✅ 部署完成！"
echo "💡 提示: 记住您的域名，下次可以直接访问"