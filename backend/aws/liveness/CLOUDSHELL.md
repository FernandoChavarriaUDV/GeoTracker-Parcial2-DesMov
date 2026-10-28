# Despliegue facial de la clase

Paquete sin credenciales. Ejecutar en CloudShell de FC-ens2026 (657498838364), us-east-1.

1. Subir `liveness-1.5-cloudshell.zip` usando Acciones > Cargar archivo.
2. Ejecutar:

```bash
unzip -o liveness-1.5-cloudshell.zip -d liveness-1.5
cd liveness-1.5
bash deploy-cloudshell.sh
```

Revisar y confirmar el changeset. No requiere claves AWS ni permisos de despliegue en EC2. Crea tres funciones privadas Node.js 24, roles de minimo privilegio, un Identity Pool para streaming y logs de 14 dias. El rol existente del backend recibe permiso para invocar exclusivamente estas funciones. No cambia el login de la app ni habilita URLs publicas de Lambda.

Compartir la tabla final Outputs (ARN de las tres funciones, IdentityPoolId y Region); esos identificadores no son secretos. No compartir credenciales, tokens ni claves.

Este despliegue no activa por si solo el flujo en la app: faltan configurar el backend, verificar acceso desde Docker y compilar la APK con el Identity Pool real. La comparacion usa todas las fotos validas registradas, con limite explicito de 20 candidatos para esta practica; si se supera, informa el limite sin dar una respuesta parcial como definitiva. No concede acceso ni reemplaza el login. Se requiere consentimiento de las personas cuyas fotos se utilicen.

Si falla, compartir el error de CloudFormation sin secretos. No borrar el stack para intentar de nuevo: el comando permite actualizarlo.
