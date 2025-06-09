#!/bin/bash

# 健康检查脚本
# @author lx
# @date 2025/06/08

set -e

echo "=== 执行健康检查 ==="

# 服务端点配置
SERVICES=(
    "gateway:8080"
    "logic:8081"
    "chat:8082"
    "adm:8083"
)

HEALTH_CHECK_TIMEOUT=30
SUCCESS_COUNT=0
TOTAL_SERVICES=${#SERVICES[@]}

# 检查每个服务
for service in "${SERVICES[@]}"; do
    IFS=':' read -r name port <<< "$service"
    
    echo "检查服务: $name (端口: $port)"
    
    # 等待服务启动
    wait_time=0
    while [ $wait_time -lt $HEALTH_CHECK_TIMEOUT ]; do
        if curl -f -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo "✓ $name 服务健康"
            SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
            break
        fi
        
        echo "等待 $name 服务启动... ($wait_time/$HEALTH_CHECK_TIMEOUT)"
        sleep 2
        wait_time=$((wait_time + 2))
    done
    
    if [ $wait_time -ge $HEALTH_CHECK_TIMEOUT ]; then
        echo "✗ $name 服务健康检查失败"
    fi
done

# 检查结果
echo "=== 健康检查结果 ==="
echo "成功: $SUCCESS_COUNT/$TOTAL_SERVICES"

if [ $SUCCESS_COUNT -eq $TOTAL_SERVICES ]; then
    echo "所有服务健康检查通过"
    exit 0
else
    echo "部分服务健康检查失败"
    exit 1
fi