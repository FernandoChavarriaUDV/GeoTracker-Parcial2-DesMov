# Endpoints de GeoTracker

GeoTracker se desplegará en una instancia nueva de AWS y con base de datos independiente.
No utilizar el servidor anterior como destino de despliegue. Para la prueba en esta PC,
ver `../android/PRUEBA-LOCAL.md` y el perfil `application-local.yml`.

Este módulo amplía el backend existente, tal como indica el ejercicio. Reutiliza el registro,
el login y la validación JWT. No exige rol de censista o administrador: cualquier usuario
autenticado puede guardar y consultar sus propios lugares.

## Ejecución local

Requisitos: Java 21, Maven y Docker Desktop con MySQL.

```powershell
docker compose up -d
mvn spring-boot:run
```

Flyway aplica `V6__create_locations.sql` automáticamente. La tabla nueva almacena el usuario,
nombre, descripción, coordenadas y fecha de creación. Las migraciones anteriores se conservan.
No ejecutar esta migración manualmente si el servidor la administra con Flyway.

Para producción, respaldar la base y desplegar el backend actualizado con la configuración
existente. Estos cambios locales no actualizan por sí solos el servidor AWS.
Las credenciales y `JWT_SECRET` se configuran mediante variables de entorno.

## Contrato de API

El login existente es `POST /api/v1/auth/login` con `username` y `password`.
La respuesta contiene `body.token`. Todas las solicitudes a ubicaciones requieren:

```text
Authorization: Bearer TOKEN_DEL_LOGIN
Content-Type: application/json
```

### Guardar

`POST /api/v1/locations`

```json
{
  "name": "Universidad",
  "description": "Entrada principal",
  "latitude": 14.6349,
  "longitude": -90.5069
}
```

Devuelve HTTP 201 y el registro dentro de `body`. Nombre y descripción son obligatorios,
con máximos de 120 y 1000 caracteres; latitud entre -90 y 90 y longitud entre -180 y 180.
El usuario y la fecha los determina el servidor. Enviar un `userId` no permite elegir otro dueño.

### Consultar

`GET /api/v1/locations`

Devuelve HTTP 200 con un arreglo dentro de `body`, ordenado del registro más reciente al más antiguo.
Cada elemento contiene `id`, `userId`, `name`, `description`, `latitude`, `longitude` y `createdAt`.
El usuario solo recibe sus registros; una cuenta sin ubicaciones recibe `[]`.
No se exige paginación en este ejercicio y la consulta devuelve todos los lugares del usuario.

Los errores mantienen la estructura del backend: `httpCode`, `message`, `body`, `timestamp`.
Se responde 400 ante datos inválidos y 401 si el token falta, no es válido o ha vencido.

## Prueba de integración

```powershell
mvn test
```

`LocationApiIntegrationTest` usa una base H2 aislada y peticiones HTTP reales. Comprueba login,
rechazo de tokens inválidos, validaciones, persistencia y separación de ubicaciones entre dos usuarios.
Esta prueba no valida el despliegue ni la migración sobre MySQL real.
