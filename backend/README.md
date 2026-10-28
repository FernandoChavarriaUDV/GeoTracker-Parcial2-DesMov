# Mobile Backend

GeoTracker: ver [endpoints de ubicaciones y ejecución](GEOTRACKER.md). El módulo nuevo
reutiliza la autenticación existente y guarda lugares por usuario mediante la migración V6.

Estado y despliegue unificado 1.5: consultar [AWS-1.5](deploy/AWS-1.5.md) y [mapa del proyecto](../README-PROYECTO.md).
La integración vigente utiliza tres Lambdas Node.js y el rol EC2; ya no utiliza el handler Python único.

API REST para la aplicación Android de Desarrollo Móvil. Está construida con Java 21,
Spring Boot, Spring Security, JPA, Flyway y MySQL.

## Funcionalidad

- Registro con nombre, apellido, usuario, correo, contraseña, género, fecha de nacimiento y teléfono opcional.
- Contraseñas cifradas con BCrypt.
- Login por usuario y emisión de JWT válido por 24 horas.
- Consulta y actualización del perfil autenticado.
- Campos de perfil para teléfono y avatar Base64.
- Respuestas y errores JSON estandarizados.

## Ejecutar localmente

Requisitos: Java 21, Maven y Docker Desktop.

```bash
docker compose up -d
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Desde el emulador Android se accede
mediante `http://10.0.2.2:8080`.

El MySQL del proyecto se publica en `localhost:3307` para no interferir con otras
instalaciones de MySQL que utilicen el puerto estándar `3306`.

En un entorno real define `JWT_SECRET` con un secreto privado de al menos 32 caracteres.
El valor incluido en `application.yml` es únicamente para desarrollo local.

## Endpoints

### Registro

`POST /api/v1/auth/register`

```json
{
  "name": "Ana",
  "lastName": "López",
  "username": "ana",
  "email": "ana@example.com",
  "password": "123456",
  "gender": "Femenino",
  "birthDate": "2000-01-10",
  "phone": "+502 5555-1234"
}
```

### Login

`POST /api/v1/auth/login`

```json
{"username":"ana","password":"123456"}
```

La respuesta contiene `body.token` y `body.user`.

### Consultar perfil

`GET /api/v1/profile`

### Actualizar perfil

`PUT /api/v1/profile`

```json
{
  "name": "Ana",
  "lastName": "López",
  "email": "ana@example.com",
  "gender": "Femenino",
  "birthDate": "2000-01-10",
  "phone": "5555-5555",
  "avatarBase64": null
}
```

Los dos endpoints de perfil requieren el encabezado:

```text
Authorization: Bearer <token>
```

## Pruebas

```bash
mvn test
```
