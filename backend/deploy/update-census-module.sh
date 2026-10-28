#!/usr/bin/env bash
set -euo pipefail

APP_DIR=/opt/mobile-backend
ARCHIVE=/tmp/mobile-backend-censos-update.tar.gz
cd "$APP_DIR"
test -f docker-compose.prod.yml
test -f .env
test -f "$ARCHIVE"

BACKUP_DIR="$APP_DIR/backups/censos-$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$BACKUP_DIR"
chmod 700 "$BACKUP_DIR"
docker exec mobile-backend-mysql-1 sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysqldump --single-transaction --no-tablespaces --set-gtid-purged=OFF -u"$MYSQL_USER" "$MYSQL_DATABASE"' > "$BACKUP_DIR/database.sql"
chmod 600 "$BACKUP_DIR/database.sql"
tar -czf "$BACKUP_DIR/source.tar.gz" src pom.xml Dockerfile
echo "Respaldo guardado: $BACKUP_DIR"

tar -xzf "$ARCHIVE" -C "$APP_DIR"
docker compose --env-file .env -f docker-compose.prod.yml up -d --build api
docker compose --env-file .env -f docker-compose.prod.yml ps
