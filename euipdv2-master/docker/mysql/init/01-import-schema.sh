#!/bin/bash
# 首次初始化数据卷时由 MySQL entrypoint 执行：
#   1. 导入表结构 doc/db/euipd-schema.sql
#   2. 容器环境变量 SUPER_ADMIN_PHONE、SUPER_ADMIN_PASSWORD 均已设置时，执行 doc/db/init-super-admin.sql
# 依赖 compose 挂载：./doc/db -> /euipd-db
set -euo pipefail

MYSQL=(mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" --default-character-set=utf8mb4)

echo "[init] 导入表结构"
"${MYSQL[@]}" < /euipd-db/euipd-schema.sql

if [ -n "${SUPER_ADMIN_PHONE:-}" ] && [ -n "${SUPER_ADMIN_PASSWORD:-}" ]; then
  echo "[init] 写入平台超级管理员"
  sql_quote() { printf "%s" "$1" | sed "s/\\\\/\\\\\\\\/g; s/'/''/g"; }
  "${MYSQL[@]}" mycourse_bk <<SQL
SET @phone='$(sql_quote "$SUPER_ADMIN_PHONE")';
SET @admin_name='$(sql_quote "${SUPER_ADMIN_NAME:-}")';
SET @plain_password='$(sql_quote "$SUPER_ADMIN_PASSWORD")';
source /euipd-db/init-super-admin.sql
SQL
else
  echo "[init] 未设置 SUPER_ADMIN_PHONE 或 SUPER_ADMIN_PASSWORD，跳过超级管理员写入；之后可执行 docker/init-super-admin.sh" >&2
fi

echo "[init] 数据库初始化完成"
