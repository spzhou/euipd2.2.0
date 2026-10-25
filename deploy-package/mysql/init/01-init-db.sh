#!/bin/bash
# =====================================================================
# 首次初始化数据库（MySQL 数据目录为空时由容器自动执行，之后不再执行）：
#   1. 导入表结构 mysql/db/euipd-schema.sql
#   2. 按 .env 中的 SUPER_ADMIN_* 执行 mysql/db/init-super-admin.sql，创建平台超级管理员
# mysql/db/ 下的两个脚本由 scripts/build-artifacts.sh 从 euipdv2-master/doc/db/ 复制。
# 参数格式已由 euipd.sh 校验；缺少参数时初始化失败，避免建成没有管理员的数据库。
# =====================================================================
set -euo pipefail

MYSQL=(mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" --default-character-set=utf8mb4)

if [ -z "${SUPER_ADMIN_PHONE:-}" ] || [ -z "${SUPER_ADMIN_PASSWORD:-}" ]; then
  echo "[euipd] 未提供 SUPER_ADMIN_PHONE 或 SUPER_ADMIN_PASSWORD，停止初始化" >&2
  exit 1
fi

echo "[euipd] 导入表结构"
"${MYSQL[@]}" < /euipd-db/euipd-schema.sql

echo "[euipd] 写入平台超级管理员"
sql_quote() { printf "%s" "$1" | sed "s/\\\\/\\\\\\\\/g; s/'/''/g"; }
"${MYSQL[@]}" mycourse_bk <<SQL
SET @phone='$(sql_quote "$SUPER_ADMIN_PHONE")';
SET @admin_name='$(sql_quote "${SUPER_ADMIN_NAME:-}")';
SET @plain_password='$(sql_quote "$SUPER_ADMIN_PASSWORD")';
source /euipd-db/init-super-admin.sql
SQL

COUNT="$("${MYSQL[@]}" -N -B mycourse_bk -e "SELECT COUNT(*) FROM rdms_user WHERE login_name='$(sql_quote "$SUPER_ADMIN_PHONE")'")"
if [ "$COUNT" != "1" ]; then
  echo "[euipd] 超级管理员写入失败：请核对 SUPER_ADMIN_PHONE（11 位数字）与 SUPER_ADMIN_PASSWORD（8 至 32 位并同时含字母与数字）" >&2
  exit 1
fi
echo "[euipd] 数据库初始化完成，超级管理员登录手机号 ${SUPER_ADMIN_PHONE}"
