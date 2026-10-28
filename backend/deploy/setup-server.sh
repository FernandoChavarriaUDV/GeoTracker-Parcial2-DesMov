#!/usr/bin/env bash
set -euo pipefail

APP_DIR=/opt/mobile-backend
DOMAIN=202101815.aplicacionesweb2026.com

cd "$APP_DIR"

if [[ ! -f .env ]]; then
  umask 077
  DB_PASSWORD="$(openssl rand -hex 24)"
  ROOT_PASSWORD="$(openssl rand -hex 24)"
  JWT_SECRET="$(openssl rand -hex 48)"

  printf '%s\n' \
    'MYSQL_DATABASE=mobile_app' \
    'MYSQL_USER=mobile_user' \
    "MYSQL_PASSWORD=$DB_PASSWORD" \
    "MYSQL_ROOT_PASSWORD=$ROOT_PASSWORD" \
    "JWT_SECRET=$JWT_SECRET" \
    'JWT_EXPIRATION_SECONDS=86400' > .env
fi

chmod 600 .env
install -m 644 "$APP_DIR/$DOMAIN.conf" "/etc/nginx/sites-available/$DOMAIN"
ln -sfn "/etc/nginx/sites-available/$DOMAIN" "/etc/nginx/sites-enabled/$DOMAIN"
rm -f /etc/nginx/sites-enabled/default

nginx -t
systemctl reload nginx

docker compose --env-file .env -f docker-compose.prod.yml up -d --build
docker compose --env-file .env -f docker-compose.prod.yml ps
