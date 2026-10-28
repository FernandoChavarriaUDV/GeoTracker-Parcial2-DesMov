#!/usr/bin/env bash
set -euo pipefail
cd /opt/geotracker

if [[ ! -f .env ]]; then
    umask 077
    printf 'DB_PASSWORD=%s\nMYSQL_ROOT_PASSWORD=%s\nJWT_SECRET=%s\n' \
        "$(openssl rand -hex 24)" "$(openssl rand -hex 24)" "$(openssl rand -hex 48)" > .env
fi
chmod 600 .env

docker compose -p geotracker --env-file .env -f docker-compose.geotracker.yml up -d --build

cat > /etc/nginx/sites-available/geotracker <<'NGINX'
server {
    listen 80;
    server_name 32.193.21.113;
    client_max_body_size 2m;
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
NGINX
ln -sfn /etc/nginx/sites-available/geotracker /etc/nginx/sites-enabled/geotracker
nginx -t
systemctl reload nginx
docker compose -p geotracker --env-file .env -f docker-compose.geotracker.yml ps
