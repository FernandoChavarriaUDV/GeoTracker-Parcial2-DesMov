# Validación del punto 2: Captura de ubicaciones

Revisión: 27 de octubre de 2026. Alcance: código y pruebas automatizadas.
El usuario confirmó por separado la validación visual e interactiva de la aplicación.

| Inciso | Resultado de revisión |
| --- | --- |
| a) Formulario desde Dashboard | MainActivity navega a capture desde Capturar ubicación. CaptureScreen contiene nombre y descripción. |
| b) Ubicación actual y envío | LocationProvider usa FusedLocationProviderClient.getCurrentLocation con alta precisión, antigüedad máxima cero y duración de 20 segundos. CaptureViewModel espera esa lectura y envía LocationRequest con name, description, latitude y longitude. ApiService utiliza POST api/v1/locations. No se utiliza lastLocation ni una coordenada fija en la app. |
| c) Indicador de carga | loading se activa antes de solicitar ubicación y se mantiene mientras espera GPS y respuesta del repositorio. La pantalla presenta LinearProgressIndicator y deshabilita el formulario/botón. Las llamadas repetidas durante la operación se ignoran. |
| d) Éxito o error | El éxito solo se establece después de recibir el registro guardado. Ante fallos de ubicación o backend, se detiene la carga, se conserva el formulario y se muestra error sin indicar éxito. Una respuesta del backend sin body también produce error. |
| e) MVVM | CaptureState contiene formulario, carga y mensajes. CaptureViewModel expone StateFlow de solo lectura y coordina LocationSource y LocationRepository. La vista no realiza llamadas HTTP. |
| f) Permisos en ejecución | El manifiesto declara FINE y COARSE. CaptureScreen comprueba los permisos y utiliza RequestMultiplePermissions si faltan. Al concederse alguno continúa; al denegarse todos presenta instrucciones para habilitarlo en Ajustes. Los fallos del proveedor se capturan y no se muestran como un guardado exitoso. |

## Ajustes de esta revisión

- Se maneja la cancelación de la tarea de Google Play Services para evitar una espera indefinida.
- permissionDenied limpia un posible mensaje de éxito anterior.
- LocationSource y Coordinates permiten probar el ViewModel sin un dispositivo ni dependencias
  de objetos Location de Android. En la aplicación real se sigue utilizando LocationProvider.

## Pruebas añadidas

CaptureViewModelTest comprueba:

Resultado: cinco pruebas de captura aprobadas; diez pruebas Android en total, sin fallos
ni errores. assembleDebug y lintDebug completados correctamente en esta revisión.

1. Carga durante la espera de GPS y durante el envío, cuatro campos correctos, prevención
   de doble envío y éxito únicamente después de responder el repositorio.
2. Campos vacíos o demasiado largos sin solicitar GPS ni guardar.
3. Fallo del GPS: error, formulario conservado y ninguna solicitud de guardado.
4. Fallo de conexión al guardar: error, carga finalizada y ausencia de éxito.
5. Permiso denegado: mensaje y ninguna petición de GPS o red.

Las ubicaciones y respuestas simuladas de estas pruebas están exclusivamente en src/test.
No sustituyen la lectura de GPS ni la API en la APK.

La precisión final depende del permiso concedido: Android puede entregar ubicación aproximada
si el usuario eligió ese permiso. El código respeta esa decisión del sistema.

El backend valida los campos obligatorios, sus longitudes, los rangos de coordenadas y el token;
asigna el dueño desde la autenticación. La prueba de integración existente de ubicaciones
comprueba persistencia, rechazo de coordenadas inválidas y separación por usuario.
