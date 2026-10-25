#!/bin/bash
# 在已运行的开发数据库中写入平台超级管理员（首次初始化时未设置 SUPER_ADMIN_* 的情况下使用）。
# 手机号、口令、姓名取自 docker/.env.local 中的 SUPER_ADMIN_PHONE、SUPER_ADMIN_PASSWORD、SUPER_ADMIN_NAME。
# 用法：docker/init-super-admin.sh
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$PROJECT_ROOT/docker/.env.local"
[ -f "$ENV_FILE" ] || { echo "缺少 docker/.env.local，请按 docker/env.example 创建" >&2; exit 1; }
set -a
. "$ENV_FILE"
set +a

[ -n "${SUPER_ADMIN_PHONE:-}" ] && [ -n "${SUPER_ADMIN_PASSWORD:-}" ] \
  || { echo "请在 docker/.env.local 中填写 SUPER_ADMIN_PHONE 与 SUPER_ADMIN_PASSWORD" >&2; exit 1; }

sql_quote() { printf "%s" "$1" | sed "s/\\\\/\\\\\\\\/g; s/'/''/g"; }

docker exec -i -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" euipd-mysql \
  mysql -uroot --default-character-set=utf8mb4 mycourse_bk <<SQL
SET @phone='$(sql_quote "$SUPER_ADMIN_PHONE")';
SET @admin_name='$(sql_quote "${SUPER_ADMIN_NAME:-}")';
SET @plain_password='$(sql_quote "$SUPER_ADMIN_PASSWORD")';
source /euipd-db/init-super-admin.sql
SQL
