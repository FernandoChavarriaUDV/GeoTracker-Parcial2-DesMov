# Actualización segura a 1.5

Cuenta FC-ens2026, 657498838364; región us-east-1; instancia i-01234f8889958550d.
Requisitos: SSM Online, Docker/MySQL/API existentes, IMDSv2 obligatorio con hop limit 2 y el rol Backend-DesMov-SSM.

## Preparación local

```powershell
cd mobile-backend
mvn.cmd package
python deploy/package-update.py
```

El generador incluye solo una lista explícita de código, JAR probado y configuración AWS pública.
No incluye `.env`, claves, credenciales de firma, fotos ni copias de la base de datos.

## Aplicación desde CloudShell

Subir `entregables/backend-1.5-cloudshell.zip` con Acciones > Cargar archivo. Ejecutar:

```bash
cd ~
unzip -o backend-1.5-cloudshell.zip -d backend-1.5
cd backend-1.5
python3 deploy-via-ssm.py
```

El script comprueba la cuenta, integridad y estado SSM. Utiliza temporalmente el bucket privado que SAM ya creó;
sube el paquete cifrado con SSE-S3 y genera un enlace firmado de 15 minutos que no imprime. No concede acceso S3 al rol EC2.
El objeto temporal se retira al concluir la ejecución del script. El paquete sin secretos queda en CloudShell.
No cerrar CloudShell durante la operación. Si se interrumpe, consultar el comando SSM antes de repetirla.

En EC2 crea un respaldo root-only de fuentes/configuración y SQL en
`/opt/mobile-backend/backups/update-1.5-FECHA/`, y conserva una etiqueta Docker `mobile-backend-api:rollback-FECHA`.
Construye la imagen de ejecución con el JAR ya compilado, preservando el servicio anterior durante la construcción.
Actualiza solo el backend (breve interrupción); no reinicia ni recrea MySQL, no cambia DNS y no toca DigitalOcean.
Se aplicará V5, que añade una columna nullable de resumen facial, compatible con la versión anterior.

Comprobaciones: arranque HTTP, V5, autenticación/roles y llamadas crear/consultar sesión Lambda usando el rol EC2 desde Docker.
La sesión diagnóstica no contiene captura ni imagen; no prueba detección real ni similitudes.
La salida debe incluir `lambda_create_and_results=OK`, `smoke=OK` y `deployment_1.5=OK`.
Ante fallo de validación, intenta restaurar fuentes/configuración e imagen anteriores. No restaura automáticamente el SQL,
porque sobrescribiría cambios legítimos hechos por usuarios. No reintentar a ciegas si SSM informa un timeout.

## Activación Android

Instalar la APK 1.5 firmada con la clave existente una vez confirmado el despliegue.
Mantiene login, dominio y cuentas. El Identity Pool no es secreto y no sustituye la autenticación de la aplicación.
Con fotos consentidas en los perfiles: abrir prueba facial, aceptar consentimiento, permitir cámara y completar la captura.
Comprobar resultado de vida, selfie temporal, número de fotos comparadas y coincidencias o mensaje de búsqueda incompleta.

## No hacer

- No pegar claves AWS ni contraseñas en CloudShell, código o chat.
- No abrir MySQL/8080 a Internet ni ampliar SSH a 0.0.0.0/0 para este despliegue.
- No ejecutar los scripts históricos de migración DigitalOcean.
- No borrar respaldos, volúmenes, el stack o la instancia para resolver un fallo de actualización.
