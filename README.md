# GeoTracker - Desarrollo de Aplicaciones Móviles

Aplicación Android para registrar ubicaciones de interés. Permite iniciar sesión,
capturar la ubicación actual con nombre y descripción, consultar los lugares guardados
y visualizarlos en Google Maps. Cada usuario consulta únicamente sus ubicaciones.

## Organización

- `android/`: Kotlin, Jetpack Compose, Material 3, Navigation Compose, StateFlow, Retrofit,
  OkHttp, Maps Compose y FusedLocationProviderClient, con arquitectura MVVM.
- `backend/`: API REST con Java 21, Spring Boot, JWT, JPA, Flyway y MySQL 8.4.
  Reutiliza la autenticación del backend del curso y agrega los endpoints de ubicaciones.

## Android

Abrir `android/` en Android Studio con JDK 21 y Android SDK 36. Crear `local.properties`
usando `local.properties.example` e indicar la ruta del SDK, la URL de la API terminada
en `/` y una clave propia de Maps SDK for Android autorizada para `com.example.geotracker`.

```powershell
cd android
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

La variante release utiliza HTTPS. La clave de Maps y las claves de firma no se incluyen
en el repositorio. Consultar `android/GOOGLE-MAPS.md` para las restricciones de la clave.

## Backend

Con MySQL local disponible, configurar `DB_URL`, `DB_USER`, `DB_PASSWORD` y `JWT_SECRET`.
Las migraciones se aplican automáticamente al arrancar:

```powershell
cd backend
mvn spring-boot:run
```

Para probar sin MySQL, consultar `android/PRUEBA-LOCAL.md`. Ese perfil utiliza una base H2
exclusiva de pruebas y no representa la configuración de producción.

## API

| Método | Ruta | Función |
| --- | --- | --- |
| POST | `/api/v1/auth/login` | Autenticar y devolver el token |
| POST | `/api/v1/auth/register` | Registrar un usuario |
| POST | `/api/v1/locations` | Guardar nombre, descripción, latitud y longitud |
| GET | `/api/v1/locations` | Obtener todos los lugares del usuario autenticado |

Las rutas de ubicaciones requieren `Authorization: Bearer TOKEN`. El dueño de cada registro
se obtiene del token, no del cuerpo enviado por Android. Los registros incluyen fecha de creación.
El contrato completo está en `backend/GEOTRACKER.md`.

## Pruebas

```powershell
cd backend
mvn test
```

La integración de ubicaciones comprueba persistencia, validaciones, JWT y separación entre
usuarios. Las pruebas Android verifican login, interceptor, estados de captura, consulta y diálogo.
Los datos simulados pertenecen únicamente a los tests y no al flujo de la aplicación.

## Despliegue

GeoTracker utiliza una instancia EC2 independiente. Ver `backend/deploy/GEOTRACKER-AWS.md`.
La configuración de Docker está en `backend/docker-compose.geotracker.yml`.
MySQL no publica puertos y Java escucha en localhost; Nginx recibe las peticiones públicas.

Los módulos de censos y verificación facial provienen del backend del curso; GeoTracker no
los presenta en sus pantallas y la verificación facial permanece desactivada en este despliegue.
