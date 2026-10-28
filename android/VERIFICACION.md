# Verificación local de GeoTracker

Fecha: 27 de octubre de 2026, Guatemala.

## Resultado

- Android: `assembleDebug`, `testDebugUnitTest` y `lintDebug` completados correctamente.
- Dos pruebas Android aprobadas: interceptor del token y carga/selección/error del ViewModel de ubicaciones.
- Backend: 42 pruebas aprobadas, sin fallos ni errores; incluye LocationApiIntegrationTest.
- APK de prueba: `../entregables/GeoTracker-1.0-debug.apk`, firmado con la clave de depuración.
- Código Android y backend empaquetados por separado, sin archivos locales de configuración ni claves de firma.

La primera ejecución de las pruebas Android falló al cargar clases desde la ruta con tilde del
proyecto. Se comprobó la ejecución correcta usando `-PbuildOutputDir=C:/Users/CG/AppData/Local/Temp/GeoTrackerBuild`.
Esta opción cambia únicamente la carpeta de compilación.

## Alcance pendiente

No había dispositivos ni emuladores conectados. No se realizó verificación visual de pantallas,
captura GPS real ni carga de mapas con una clave configurada. Los tests del backend usan H2;
no se ejecutó la migración V6 sobre MySQL ni se desplegó la actualización del servidor.

El APK debug apunta por defecto al backend del emulador (`http://10.0.2.2:8080/`) y no tiene una
clave de Google Maps incluida. Para usar un teléfono y el servidor real, configurar ambos valores
y recompilar. Este APK no representa la entrega release firmada ni una prueba de extremo a extremo.

Lint no presenta errores bloqueantes; quedan recomendaciones de versión de dependencia y uso
de las extensiones KTX de preferencias. Se conservan las versiones usadas en el proyecto del curso.

No se crearon commits ni repositorios remotos. La guía ENTREGA.md contiene los pasos de publicación.
