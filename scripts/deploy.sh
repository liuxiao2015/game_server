#!/bin/bash

# 部署脚本
# @author lx
# @date 2025/06/08

set -e

echo "=== 开始部署游戏服务器 ==="

# 参数检查
ENVIRONMENT=${1:-development}
echo "部署环境: $ENVIRONMENT"

# 检查环境
echo "检查部署环境..."
./scripts/check-env.sh

if [ $? -ne 0 ]; then
    echo "环境检查失败，部署中止"
    exit 1
fi

# 备份当前版本
if [ "$ENVIRONMENT" = "production" ]; then
    echo "备份当前版本..."
    docker-compose -f docker-compose.prod.yml stop
    docker tag game-server:latest game-server:backup-$(date +%Y%m%d-%H%M%S) || true
fi

# 部署服务
echo "部署服务..."
if [ "$ENVIRONMENT" = "production" ]; then
    docker-compose -f docker-compose.prod.yml up -d
else
    docker-compose -f docker-compose.dev.yml up -d
fi

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 健康检查
echo "执行健康检查..."
./scripts/health-check.sh

if [ $? -eq 0 ]; then
    echo "=== 部署成功 ==="
else
    echo "=== 部署失败，开始回滚 ==="
    
    if [ "$ENVIRONMENT" = "production" ]; then
        # 回滚到备份版本
        docker-compose -f docker-compose.prod.yml down
        docker tag game-server:backup-$(date +%Y%m%d)* game-server:latest || true
        docker-compose -f docker-compose.prod.yml up -d
    fi
    
    exit 1
fi

echo "部署完成时间: $(date)"