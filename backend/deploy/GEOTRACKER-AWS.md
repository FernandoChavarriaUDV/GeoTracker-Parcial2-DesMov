# GeoTracker en AWS EC2

Instancia independiente: `i-0f43d0acf535a9e28`, región `us-east-1`.
IPv4 elástica: `32.193.21.113`. Directorio de aplicación: `/opt/geotracker`.
Dominio previsto: `202101815.aplicacionesweb2026.com`.

## Configuración

Ubuntu 24.04, Docker Compose, MySQL 8.4, Java 21 y Nginx.
Se utiliza `docker-compose.geotracker.yml` con proyecto Docker `geotracker` y un volumen MySQL
independiente. `.env` contiene contraseñas generadas en el servidor y tiene permisos 600.
Los secretos no se incluyen en Git. La verificación facial permanece desactivada.

MySQL no publica puertos; Java publica únicamente `127.0.0.1:8080`. El grupo de seguridad
debe permitir 80/443 públicos y SSH desde la IP del propietario. No abrir 3306 ni 8080.

## Construcción y despliegue

Ejecutar las pruebas y construir el JAR en el equipo local:

```powershell
mvn package
```

Transferir a `/opt/geotracker` el JAR `target/mobile-backend-0.0.1-SNAPSHOT.jar`,
`Dockerfile.runtime`, `Dockerfile.runtime.dockerignore` y `docker-compose.geotracker.yml`.
En la instancia, desde `/opt/geotracker`:

```bash
sudo docker compose -p geotracker --env-file .env -f docker-compose.geotracker.yml up -d --build
sudo docker compose -p geotracker --env-file .env -f docker-compose.geotracker.yml ps
```

Flyway aplica las migraciones V1 a V6. En el primer arranque de esta instancia se comprobó
su aplicación correcta sobre MySQL y la respuesta HTTP 401 al consultar ubicaciones sin token.
No reutilizar los scripts antiguos de despliegue de Censo Desmov en el servidor anterior.

Nginx dirige el dominio a `http://127.0.0.1:8080`. El certificado HTTPS se instala y renueva
con Certbot una vez que DNS y las reglas de acceso 80/443 estén configurados.

## Datos

La base es nueva. Las cuentas y ubicaciones del entorno local no se transfieren automáticamente.
Antes de una actualización posterior, realizar respaldo de MySQL. No ejecutar `down -v`,
porque elimina el volumen persistente de la base de datos.
