# Preparación de la entrega

## Rúbrica

| Criterio | Implementación |
| --- | --- |
| Login, 15 puntos | LoginScreen, LoginViewModel, AuthRepository, SessionManager y AuthInterceptor |
| Captura GPS, 25 puntos | CaptureScreen, CaptureViewModel y LocationProvider; POST de ubicación |
| Listado, 20 puntos | LocationsScreen con LazyColumn y Cards; GET de ubicaciones propias |
| Mapa, 20 puntos | MapScreen, GoogleMap, Marker y encuadre de cámara |
| Diálogo, 10 puntos | AlertDialog; selección en LocationsViewModel y StateFlow; clic consumido |
| Backend, 10 puntos | LocationController, LocationRepository, Location y migración V6 |

La tabla relaciona el código con los criterios. La nota depende de comprobar su funcionamiento
y de la evaluación del docente; no representa una calificación obtenida.

## Antes de entregar

- Configurar la URL del backend actualizado y la clave de Google Maps.
- Ejecutar las pruebas manuales indicadas en README, especialmente GPS y mapa en un dispositivo.
- Para release, utilizar HTTPS y generar el APK firmado con una clave propia.
- Revisar los archivos antes de publicarlos. Los archivos privados de configuración están ignorados.

## Publicar el código

Crear dos repositorios vacíos en GitHub. En una carpeta independiente con el código Android:

```powershell
git init -b main
git add .
git status
git diff --cached --stat
git commit -m "Implementar GeoTracker con autenticación y registro de ubicaciones"
git remote add origin https://github.com/TU_USUARIO/TU_REPOSITORIO_ANDROID.git
git push -u origin main
```

Repetir desde una carpeta independiente con el código del backend, usando su URL:

```powershell
git init -b main
git add .
git status
git diff --cached --stat
git commit -m "Agregar endpoints autenticados de ubicaciones por usuario"
git remote add origin https://github.com/TU_USUARIO/TU_REPOSITORIO_BACKEND.git
git push -u origin main
```

Si el backend ya tiene repositorio propio, añadir y confirmar los cambios allí en lugar de inicializar otro.
Los comandos son instrucciones de publicación: no se han ejecutado ni se han creado repositorios remotos.
