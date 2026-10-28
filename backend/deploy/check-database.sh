#!/usr/bin/env sh
set -eu

CONTAINER=mobile-backend-mysql-1

echo "CONTENEDOR"
docker inspect --format 'estado={{.State.Status}} iniciado={{.State.StartedAt}}' "$CONTAINER"

echo "VOLUMEN"
docker inspect --format '{{range .Mounts}}{{.Name}} -> {{.Destination}}{{println}}{{end}}' "$CONTAINER"

echo "BASE_DE_DATOS"
docker exec "$CONTAINER" sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SELECT DATABASE() AS base_actual; SHOW TABLES; SELECT COUNT(*) AS usuarios_registrados FROM users; SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"'
