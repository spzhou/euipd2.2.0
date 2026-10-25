#!/usr/bin/env bash
# 生成 EUIPD 令牌签名密钥对（RS256：PKCS#8 私钥 + X.509 公钥 PEM），写入 euipdv2-master/keys/。
# system 以私钥签发令牌，gateway 与其他服务以公钥验签；keys/ 已被 .gitignore 忽略，不得提交。
# 已有私钥时默认不覆盖；换钥会使已签发的令牌全部失效，须加 --force。
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/keys"
mkdir -p "$OUT"
chmod 700 "$OUT"
if [ -f "$OUT/jwt-private.pem" ] && [ "${1:-}" != "--force" ]; then
  echo "$OUT/jwt-private.pem 已存在；确需换钥时加 --force。"
  exit 0
fi
openssl genrsa -out "$OUT/jwt-private-rsa.pem" 2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in "$OUT/jwt-private-rsa.pem" -out "$OUT/jwt-private.pem"
openssl rsa -in "$OUT/jwt-private-rsa.pem" -pubout -out "$OUT/jwt-public.pem"
rm -f "$OUT/jwt-private-rsa.pem"
chmod 600 "$OUT/jwt-private.pem"
chmod 644 "$OUT/jwt-public.pem"
echo "已生成 $OUT/jwt-private.pem 与 $OUT/jwt-public.pem"
