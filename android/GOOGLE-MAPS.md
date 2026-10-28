# Google Maps en GeoTracker

Comprobación del 27 de octubre de 2026: se configuró la clave local, se recompiló e instaló
la APK USB en el teléfono y el usuario confirmó que el mapa aparece. No se modificaron
restricciones ni credenciales en Google Cloud durante esta configuración.

La clave se configura con `MAPS_API_KEY` en `local.properties`, archivo excluido de Git.
No agregar la clave al manifiesto, al código Kotlin ni a los documentos de entrega.
El manifiesto recibe el valor mediante el placeholder que configura Gradle.

## Restricción Android para la APK debug de esta PC

- Paquete: `com.example.geotracker`
- SHA-1: `AB:5A:DE:76:2F:AA:A3:5A:E9:93:0A:E8:AC:02:1F:CD:20:15:96:23`

En el proyecto propietario de la clave de Google Cloud se debe habilitar Maps SDK for Android
y contar con facturación habilitada. Si la clave está restringida a aplicaciones Android,
debe autorizar este par de paquete y SHA-1. No retirar las restricciones de otras aplicaciones.
Restringir la API a Maps SDK for Android si esa es la única API que utiliza la clave.

Para la APK release, obtener su SHA-1 con la clave de firma release y registrar también ese par.
Una clave que solo permite el paquete de la app anterior no autoriza automáticamente GeoTracker.

## Comprobación

Sincronizar y recompilar después de cambiar `local.properties`. Instalar la APK actualizada,
iniciar sesión y abrir Ver mapa con al menos una ubicación guardada. Se debe ver el mapa base,
el marcador y, al tocarlo, el diálogo con nombre, descripción y coordenadas.

Si aparece un mapa vacío, comprobar Logcat. `Authorization failure` indica revisar clave,
habilitación del SDK y restricciones de paquete/SHA-1; no basta con que la clave tenga un valor.

Referencia: https://developers.google.com/maps/documentation/android-sdk/get-api-key
