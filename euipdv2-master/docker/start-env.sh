#!/bin/bash
# EUIPD 项目环境快速启动脚本
# Author: 周朔鹏
# Date: 2026-08-01

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$PROJECT_ROOT/docker/.env.local"

cd "$PROJECT_ROOT"

if [ ! -f "$ENV_FILE" ]; then
    echo "❌ 缺少 docker/.env.local，请先执行：cp docker/env.example docker/.env.local 并填写口令"
    exit 1
fi
set -a
. "$ENV_FILE"
set +a
for v in MYSQL_ROOT_PASSWORD REDIS_PASSWORD MINIO_ROOT_USER MINIO_ROOT_PASSWORD RABBITMQ_DEFAULT_USER RABBITMQ_DEFAULT_PASS; do
    if [ -z "${!v:-}" ]; then
        echo "❌ docker/.env.local 中 $v 未填写"
        exit 1
    fi
done

echo "=========================================="
echo "EUIPD 项目 Docker 环境启动脚本"
echo "=========================================="
echo ""

if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi
echo "✅ Docker运行正常"
echo ""

check_port() {
    local port=$1
    local service=$2
    if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
        echo "⚠️  端口 $port ($service) 已被占用"
        return 1
    fi
    echo "✅ 端口 $port ($service) 可用"
    return 0
}

echo "检查端口占用情况..."
check_port 3306 "MySQL" || exit 1
check_port 6379 "Redis" || exit 1
check_port 5672 "RabbitMQ" || exit 1
check_port 15672 "RabbitMQ管理界面" || exit 1
check_port 18100 "MinIO API" || exit 1
check_port 18101 "MinIO Console" || exit 1
echo ""

COMPOSE="docker compose --env-file $ENV_FILE"
if ! docker compose version >/dev/null 2>&1; then
    COMPOSE="docker-compose --env-file $ENV_FILE"
fi

echo "启动全部基础服务（MySQL 首次启动会自动建库/建表）..."
$COMPOSE up -d

echo ""
echo "等待 MySQL healthy..."
for i in $(seq 1 60); do
    if docker exec -e MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" euipd-mysql mysqladmin ping -h localhost -uroot --silent 2>/dev/null; then
        echo "✅ MySQL 可用"
        break
    fi
    if [ "$i" -eq 60 ]; then
        echo "❌ MySQL 启动超时"
        $COMPOSE logs --tail=80 mysql
        exit 1
    fi
    sleep 2
done

echo "等待 Redis..."
for i in $(seq 1 30); do
    if docker exec -e REDISCLI_AUTH="${REDIS_PASSWORD}" euipd-redis redis-cli ping 2>/dev/null | grep -q PONG; then
        echo "✅ Redis 可用"
        break
    fi
    if [ "$i" -eq 30 ]; then
        echo "❌ Redis 启动超时"
        exit 1
    fi
    sleep 1
done

echo "等待 MinIO..."
for i in $(seq 1 30); do
    if curl -sf http://127.0.0.1:18100/minio/health/live >/dev/null 2>&1; then
        echo "✅ MinIO 可用"
        break
    fi
    if [ "$i" -eq 30 ]; then
        echo "❌ MinIO 启动超时"
        $COMPOSE logs --tail=80 minio
        exit 1
    fi
    sleep 2
done

echo ""
echo "=========================================="
echo "服务状态"
echo "=========================================="
$COMPOSE ps

echo ""
echo "=========================================="
echo "✅ 环境启动完成"
echo "=========================================="
echo "  MySQL:     localhost:3306"
echo "  Redis:     localhost:6379"
echo "  RabbitMQ:  http://localhost:15672"
echo "  MinIO API: http://localhost:18100"
echo "  MinIO UI:  http://localhost:18101"
echo "  账号与口令见 docker/.env.local"
echo "=========================================="
