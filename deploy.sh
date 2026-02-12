#!/bin/bash

# Asset Manager 一键部署脚本

echo "🚀 开始部署 Asset Manager..."

# 检查Docker
if ! command -v docker &> /dev/null; then
    echo "❌ 请先安装 Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

# 构建并启动
echo "📦 构建镜像..."
docker compose build

echo "▶️ 启动服务..."
docker compose up -d

echo "✅ 部署完成！"
echo ""
echo "📝 访问地址: http://你的服务器IP:8000"
echo ""
echo "📋 管理命令:"
echo "   查看日志: docker compose logs -f"
echo "   停止服务: docker compose down"
echo "   重启服务: docker compose restart"
