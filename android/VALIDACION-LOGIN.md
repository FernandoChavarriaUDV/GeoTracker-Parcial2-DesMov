# Validación del punto 1: Login

Revisión: 27 de octubre de 2026. Entorno: backend local y APK debug de GeoTracker.

## Requisitos

| Inciso | Resultado | Evidencia |
| --- | --- | --- |
| a) Campos usuario/email y contraseña | Implementado | LoginScreen incluye usuario y contraseña oculta con PasswordVisualTransformation. Las cuentas de esta prueba utilizan el correo como usuario. |
| b) Autenticación real contra el backend | Implementado y probado | ApiService declara POST api/v1/auth/login; AuthRepository envía las credenciales ingresadas. La API local acepta la cuenta válida y rechaza cuentas o claves incorrectas. |
| c) Guardar token y enviarlo automáticamente | Implementado y probado | AuthRepository exige login verdadero y token no vacío; SessionManager lo conserva en preferencias privadas. AppContainer instala AuthInterceptor, que consulta el token actual y agrega Authorization: Bearer a las llamadas posteriores. |
| d) Mensajes de credenciales y conexión | Implementado y probado en pruebas automatizadas | LoginViewModel captura el error y actualiza LoginState.error; LoginScreen lo muestra. LoginApiTest comprueba HTTP 401 y el mensaje de conexión para IOException. |
| e) Abrir Dashboard tras el login | Implementado | MainActivity observa loggedIn y navega a dashboard, quitando login del historial. Durante la revisión se observó el Dashboard real en el teléfono antes de solicitar la prueba manual. El usuario ya había confirmado el login correcto en esta sesión. |
| f) MVVM con StateFlow, Repository y ApiService separados | Implementado | LoginScreen representa LoginState; LoginViewModel expone un StateFlow de solo lectura. AuthRepository realiza la operación de acceso y ApiService define los endpoints. |

## Pruebas ejecutadas

Cinco pruebas unitarias de Android aprobadas, sin fallos ni errores. Incluyen tres casos
del login, la prueba del interceptor y la prueba del ViewModel de ubicaciones.
El servidor de prueba de LoginApiTest solo pertenece a src/test; no participa en la autenticación
de la APK. No se encontraron usuarios, contraseñas de prueba ni login simulado en src/main.

Comprobaciones contra el backend local activo:

| Solicitud | Resultado |
| --- | --- |
| Login con credenciales válidas | HTTP 200 y token presente |
| Contraseña incorrecta | HTTP 401 |
| Usuario inexistente | HTTP 401 |
| Campos vacíos | HTTP 400 |
| Consulta de ubicaciones con token válido | HTTP 200 |
| Consulta sin token | HTTP 401 |
| Consulta con token inválido | HTTP 401 |

No se crearon ni eliminaron usuarios o ubicaciones durante estas comprobaciones.
No se modificó el comportamiento del login; únicamente se añadieron pruebas de validación.

## Alcance visual

Se inspeccionaron el Dashboard y posteriormente la pantalla de login en el teléfono.
El teléfono rechaza la inyección de clics por adb; no se alteraron sus ajustes de seguridad.
El usuario confirmó posteriormente que la validación visual e interactiva ya fue realizada
y solicitó continuar las revisiones a nivel de código.
El mensaje de falta de conexión había sido observado por el usuario anteriormente y su
correspondencia con IOException se comprobó nuevamente mediante una prueba unitaria.

Esta revisión verifica la implementación y las respuestas locales. La prueba del mismo flujo
contra la nueva instancia AWS se realizará después del despliegue.
