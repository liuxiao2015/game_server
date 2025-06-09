#!/bin/bash

# 构建脚本
# @author lx
# @date 2025/06/08

set -e

echo "=== 开始构建游戏服务器 ==="

# 检查环境
echo "检查构建环境..."
if ! command -v mvn &> /dev/null; then
    echo "错误: Maven 未安装"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装"
    exit 1
fi

echo "环境检查通过"

# 清理旧构建
echo "清理旧构建文件..."
mvn clean

# 编译项目
echo "编译项目..."
mvn compile

# 运行测试
echo "运行测试..."
mvn test

# 打包
echo "打包应用..."
mvn package -DskipTests

# 构建Docker镜像
echo "构建Docker镜像..."
docker-compose build

echo "=== 构建完成 ==="

# 显示构建结果
echo "构建产物:"
find . -name "*.jar" -path "*/target/*" | grep -v "/test-" | head -10

echo "Docker镜像:"
docker images | grep game-server | head -5

echo "构建完成时间: $(date)"