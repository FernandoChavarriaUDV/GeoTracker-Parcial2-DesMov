# Prueba local por USB

GeoTracker utilizará una instancia nueva de AWS y una base MySQL independiente.
El servidor anterior de Censo Desmov no se utiliza para esta prueba ni como destino de despliegue.

La cuenta local es `prueba@geotracker.com`, con contraseña `123456` (seis caracteres).
Está registrada mediante la API, con contraseña cifrada; no es un login incorporado en Android.
El login emite un JWT que la app envía en el encabezado Authorization.

## Backend en esta PC

Desde `mobile-backend`:

```powershell
mvn spring-boot:run '-Dspring-boot.run.useTestClasspath=true' '-Dspring-boot.run.profiles=local'
```

El perfil local usa la dependencia H2 de pruebas y una base persistente exclusiva en `.local/geotracker`.
Escucha solamente en `127.0.0.1:8080`. No contacta a AWS ni requiere Docker.
Este perfil desactiva Flyway para la prueba; producción continúa usando MySQL con migraciones.
La carpeta `.local` está excluida de Git y de los paquetes de código.

## Teléfono

1. Conectar el teléfono por USB con depuración USB autorizada.
2. Configurar `API_BASE_URL=http://127.0.0.1:8080/` en `local.properties` y compilar debug.
3. Ejecutar con adb (desde platform-tools, o con adb disponible en PATH):

```powershell
adb reverse tcp:8080 tcp:8080
```

4. Instalar la APK de USB e iniciar sesión con la cuenta indicada.

El backend debe seguir ejecutándose y el cable USB debe permanecer conectado.
Al reconectar el teléfono puede ser necesario repetir `adb reverse`.
No confundir esta APK con la anterior configurada para la dirección del emulador `10.0.2.2`.

La clave de Maps quedó configurada en `local.properties`; el usuario confirmó que el mapa aparece.
Ver `GOOGLE-MAPS.md` para la firma debug y la configuración de la futura APK release.

## Comprobación realizada

Registro HTTP 201, login HTTP 200 con token y consulta de ubicaciones HTTP 200 con lista vacía.
La APK USB se instaló correctamente en el teléfono conectado y se abrió su MainActivity.
La interacción de login y captura GPS en el teléfono queda a cargo del usuario en esta prueba.
