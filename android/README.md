# GeoTracker

Prueba con teléfono por USB y cuenta local: ver [PRUEBA-LOCAL.md](PRUEBA-LOCAL.md).
El despliegue de GeoTracker se hará en una instancia nueva de AWS, separada del servidor anterior.

Aplicación Android para registrar lugares de interés con la ubicación actual del teléfono.
Proyecto nuevo del ejercicio de Desarrollo de Aplicaciones Móviles.

## Tecnologías

Kotlin, Jetpack Compose, Material 3, Navigation Compose, StateFlow, Retrofit con OkHttp,
Google Maps Compose y FusedLocationProviderClient. Se utiliza MVVM: las pantallas
representan el estado, los ViewModels atienden las acciones y los repositorios consultan la API.

## Abrir y ejecutar

1. Abrir esta carpeta en Android Studio. Usar JDK 21 y tener instalado Android SDK 36.
2. Crear `local.properties` con la ruta del SDK y las propiedades de `local.properties.example`.
3. Para el emulador, configurar `API_BASE_URL=http://10.0.2.2:8080/`.
   Para un teléfono físico, usar la dirección accesible del equipo o el servidor HTTPS.
4. Ejecutar el backend actualizado siguiendo `../backend/GEOTRACKER.md`.
5. Configurar `MAPS_API_KEY` con una clave propia para Maps SDK for Android.
   Restringirla al paquete `com.example.geotracker` y a la huella SHA-1 de la firma utilizada.
   La clave de la aplicación anterior requiere una restricción adicional para este paquete nuevo.
6. Ejecutar la variante debug. Iniciar sesión con una cuenta del backend.
   El registro se realiza mediante el endpoint existente; no hay cuentas incorporadas en la app.

Si no se configura la URL se utiliza el backend local del emulador. La variante debug permite HTTP
para las prácticas locales; la variante release requiere un servidor HTTPS porque su manifiesto bloquea HTTP.
Sin clave de Maps se informa la configuración pendiente y se puede seguir usando el listado.

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
.\gradlew.bat signingReport
```

El APK debug se genera en `app/build/outputs/apk/debug/app-debug.apk`.
En Windows, si las pruebas no encuentran sus clases al trabajar en una ruta con tildes,
se puede añadir `-PbuildOutputDir=C:/ruta/sin/tildes/GeoTrackerBuild` al comando Gradle.
En ese caso el APK y los reportes se generan dentro de esa ruta, en la carpeta `app`.
Para la entrega firmada, configurar la URL HTTPS y generar un APK mediante
Build > Generate Signed App Bundle or APK en Android Studio. Guardar la clave de firma fuera del repositorio.

## Organización

| Carpeta | Responsabilidad |
| --- | --- |
| `model` | Solicitudes, respuestas y estados de las pantallas |
| `data/remote` | ApiService y el interceptor del token |
| `data` | Sesión, repositorios, configuración de Retrofit y proveedor de GPS |
| `ui/login` | Inicio de sesión y su ViewModel |
| `ui/capture` | Formulario, permisos y captura de ubicación |
| `ui/locations` | Listado y ViewModel de consulta/selección |
| `ui/map` | Mapa y diálogo de información del marcador |
| `MainActivity.kt` | Tema, Dashboard y navegación |

## Flujo de la aplicación

El login guarda el token en preferencias privadas de la aplicación. El interceptor lee el token
actual y lo agrega a las peticiones posteriores. Al cerrar sesión se elimina el token y se limpia
la navegación. Ante una sesión vencida se muestra el error; usar Salir e iniciar sesión nuevamente.

Capturar y guardar solicita permisos de ubicación, obtiene una lectura actual y envía nombre,
descripción, latitud y longitud. No se usa una ubicación anterior como sustituto. Si el GPS está
desactivado o no responde, se muestra un error y se permite reintentar. Android permite conceder
ubicación aproximada; en ese caso la precisión depende del permiso del dispositivo.

El listado y el mapa consultan el backend al entrar. El botón Actualizar repite la consulta.
El mapa encuadra todos los marcadores; con un único punto utiliza zoom de detalle.
Al tocar un marcador, el ViewModel conserva la ubicación seleccionada en StateFlow y la pantalla
abre un AlertDialog Material 3. El clic se consume para suprimir el InfoWindow de Google Maps.

## Relación con las clases

Se consultaron las capturas de las grabaciones del 31 de agosto y 28 de septiembre, y capturas
y fragmentos de transcripción del 29 de septiembre. En esta última se observan MapState,
MapViewModel, MarkerItem y el componente de mapa con callbacks. La estructura de GeoTracker sigue
esa separación. La integración de datos reales, el formulario y el diálogo desarrollan lo pedido
por el ejercicio. Esta revisión no equivale a haber escuchado todas las grabaciones completas.

La app se limita a las funciones del enunciado. Los módulos anteriores de censos, perfil y
verificación facial permanecen en el backend existente y no forman parte de las pantallas nuevas.

## Verificación manual

1. Intentar entrar con credenciales incorrectas y luego con una cuenta válida.
2. Denegar el permiso de ubicación y comprobar el mensaje; habilitarlo y reintentar.
3. Guardar un lugar con nombre y descripción, con GPS activo.
4. Abrir el listado y comprobar los datos almacenados después de reiniciar la app.
5. Abrir el mapa, tocar un marcador y cerrar el diálogo; probar uno y varios puntos.
6. Entrar con otra cuenta y verificar que no aparecen lugares de la cuenta anterior.
7. Desactivar Internet y comprobar los errores de conexión y el botón Actualizar.

## GitHub

Publicar esta carpeta como repositorio de Android y `mobile-backend` como repositorio del backend.
Cada carpeta incluye `.gitignore`. No publicar `local.properties`, archivos `.env`, credenciales,
tokens, claves de firma ni carpetas de compilación. `local.properties.example` contiene ejemplos
para que otra persona pueda configurar el proyecto.

Los commits deben describir los cambios realizados y las verificaciones reales. Ejemplos de
mensajes para los cambios correspondientes: `Crear proyecto Android GeoTracker con MVVM`,
`Implementar captura y consulta de ubicaciones`, `Agregar mapa y diálogo de ubicación`.
