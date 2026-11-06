# pruebaAccenture - API de Franquicias

API REST reactiva (Spring Boot 4.1.2, WebFlux, Spring Data R2DBC, MySQL, Java 21, Maven) para administrar franquicias, sucursales y productos.

## Requisitos

- JDK 21 (verificar con `java -version`; debe ser 21.x)
- Docker Desktop con Docker Compose
- Maven Wrapper incluido (`mvnw.cmd`)

## 1. Crear el archivo `.env`

Copie el contenido siguiente en un archivo llamado `.env` en la raíz del proyecto y cambie las contraseñas:

```env
MYSQL_ROOT_PASSWORD=cambia-esta-clave-root
MYSQL_FRANQUICIAS_APP_PASSWORD=cambia-esta-clave-app

DB_URL=r2dbc:mysql://localhost:3306/franquicias_db
DB_USERNAME=franquicias_app
DB_PASSWORD=cambia-esta-clave-app
```

`DB_PASSWORD` debe coincidir con `MYSQL_FRANQUICIAS_APP_PASSWORD`: el usuario `franquicias_app` se crea automáticamente al inicializar el contenedor con privilegios sobre la base `franquicias_db`. Nunca utilice root desde la aplicación.

El archivo `.env` está en `.gitignore`; nunca debe commitearse.

## 2. Levantar MySQL

```powershell
docker compose up -d
docker compose ps
```

Docker Compose lee automáticamente el `.env` de la raíz del proyecto. Espere a que el estado del contenedor sea `healthy`.

## 3. Cargar las variables de `.env` en PowerShell

Spring Boot **no lee el archivo `.env`** cuando se ejecuta con Maven; hay que exportarlas a la sesión de PowerShell antes de arrancar o probar:

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*?)\s*=\s*(.*?)\s*$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}
```

Compruebe que quedaron cargadas (evita errores de conexión por variables ausentes):

```powershell
Get-ChildItem Env:DB_*, Env:MYSQL_*
```

Estas variables existen solo en la sesión actual; al abrir una terminal nueva hay que cargarlas otra vez.

## 4. Arrancar la aplicación

```powershell
.\mvnw.cmd spring-boot:run
```

Al arrancar, Spring ejecuta `schema.sql` contra MySQL (`spring.sql.init.mode=always`), que crea las tablas de forma idempotente con `CREATE TABLE IF NOT EXISTS`. El log debe mostrar el arranque con Netty y ningún error de conexión R2DBC.

## 5. Verificar la conexión y el esquema

```powershell
docker compose exec mysql mysql -ufranquicias_app -p -e "SHOW TABLES;" franquicias_db
```

Debe listar: `franquicia`, `producto`, `sucursal`.

Debe listar: `franquicia`, `producto`, `sucursal`.

## Pruebas

- **Unitarias**: cualquier test sin acceso a MySQL. Se ejecutan con `mvnw test`.
- **De integración**: anotadas con `@Tag("integracion")`. Necesitan MySQL levantado y las variables de entorno cargadas. Se ejecutan con `mvnw verify`.

## Datos

El volumen `mysql_data` conserva los datos entre reinicios (`docker compose down` no los borra). Para reinicializar la base recuerde que esto elimina los datos:

```powershell
docker compose down -v
```
