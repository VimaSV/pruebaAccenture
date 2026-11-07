# pruebaAccenture - API de Franquicias

API REST reactiva para administrar franquicias, sucursales y productos. Una **franquicia** contiene varias **sucursales**; cada **sucursal** vende varios **productos**, cada uno con su propio **stock**. Sobre este modelo la API permite crear franquicias y sucursales, agregar, renombrar, eliminar productos, y consultar el producto con mayor stock de cada sucursal de una franquicia.

La aplicación está desarrollada con **programación reactiva** (Spring WebFlux + Reactor): los flujos HTTP y de base de datos usan `Mono` y `Flux` sin llamadas bloqueantes.

## 1. Descripción del proyecto

- Una franquicia tiene un nombre y varias sucursales.
- Una sucursal pertenece a una franquicia, tiene un nombre y varios productos.
- Un producto pertenece a una sucursal, tiene un nombre y una cantidad de stock.

Operaciones principales: crear franquicias, crear sucursales dentro de una franquicia, agregar productos a una sucursal, eliminar productos, actualizar stock y nombre, consultar el producto con mayor stock de cada sucursal (desempatando por el ID más bajo) y listar todos los productos de una franquicia junto con la sucursal a la que pertenecen.

## 2. Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.2
- Spring WebFlux (Netty)
- Spring Data R2DBC con driver `r2dbc-mysql` (io.asyncer)
- MySQL 8.4 (en Docker)
- Docker y Docker Compose
- Maven Wrapper (`mvnw.cmd` / `mvnw`)
- JUnit 5, Mockito y Reactor Test (StepVerifier)

La aplicación funciona bajo **programación reactiva** (Spring WebFlux + Reactor): los controladores, servicios y repositorios componen flujos no bloqueantes con `Mono` y `Flux`, sin llamadas bloqueantes.

## 3. Requisitos previos

Obligatorios:

- **JDK 21**: verificar con `java -version` (debe indicar versión 21.x).
- **Docker Desktop** con Docker Compose: para ejecutar MySQL.

Opcionales:

- Un cliente HTTP (Postman, curl o similar) para probar los endpoints.
- IDE: IntelliJ IDEA, VS Code o Eclipse con soporte de Java 21.

No es necesario instalar Maven: el proyecto incluye Maven Wrapper.

## 4. Configuración de variables de entorno

Cree un archivo `.env` en la raíz del proyecto a partir de `.env.example`:

```powershell
Copy-Item .env.example .env
```

Luego edítelo y reemplace las contraseñas por sus propios valores. Variables:

| Variable | Uso |
|---|---|
| `MYSQL_ROOT_PASSWORD` | Contraseña del usuario root de MySQL dentro del contenedor Docker (no la usa la aplicación). |
| `MYSQL_FRANQUICIAS_APP_PASSWORD` | Contraseña del usuario de aplicación `franquicias_app` que Docker crea al inicializar el contenedor. |
| `DB_URL` | URL R2DBC de conexión: `r2dbc:mysql://localhost:3306/franquicias_db` (la aplicación NO usa root; usa `franquicias_app`). |
| `DB_USERNAME` | Usuario de la aplicación: `franquicias_app`. |
| `DB_PASSWORD` | Contraseña del usuario de aplicación. Debe coincidir con `MYSQL_FRANQUICIAS_APP_PASSWORD`. |

`DB_PASSWORD` debe coincidir con `MYSQL_FRANQUICIAS_APP_PASSWORD`; ambos deben setearse a la misma contraseña. El usuario `franquicias_app` se crea automáticamente la primera vez que se inicializa el contenedor. Nunca se use el usuario root desde la aplicación.

El archivo `.env` está excluido en `.gitignore` y no debe commitearse. `.env.example` sí se sube al repositorio solo como referencia sin valores reales.

## 5. Ejecución de MySQL con Docker Compose

Desde la raíz del proyecto:

```powershell
# iniciar el contenedor (lee el .env de la raíz automáticamente)
docker compose up -d

# verificar que el estado del contenedor sea "healthy"
docker compose ps
```

Para detener el contenedor conservando los datos:

```powershell
docker compose down
```

Los datos se conservan en el volumen nombrado `mysql_data`, por lo que `docker compose down` NO borra la información. Solo si quiere eliminarla por completo (borra todos los datos):

```powershell
docker compose down -v
```

## 6. Ejecución de la aplicación

Spring Boot **no lee el archivo `.env`** cuando se ejecuta con Maven. Antes de arrancar o ejecutar pruebas, cargue las variables en la sesión de PowerShell:

```powershell
# cargar las variables del .env en la sesión de PowerShell actual
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+?)\s*=\s*(.*)\s*$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}

# comprobar que quedaron cargadas
Get-ChildItem Env:DB_*, Env:MYSQL_*
```

Estas variables existen solo en la sesión actual; al abrir una terminal nueva hay que volver a cargarlas.

Con las variables ya cargadas, arranque la aplicación:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación escucha en el puerto **8080**. Al arrancar, Spring ejecuta `schema.sql` contra MySQL (`spring.sql.init.mode=always`), que crea las tablas de forma idempotente con `CREATE TABLE IF NOT EXISTS`.

## 7. Endpoints

### Endpoints obligatorios (criterios de aceptación)

| Método | Ruta | Descripción | Parámetros | Body JSON | Código esperado |
|---|---|---|---|---|---|
| POST | `/api/franquicias` | Crear franquicia | — | `{"nombre": "Farmacias XYZ"}` | 201 |
| POST | `/api/franquicias/{idFranquicia}/sucursales` | Crear sucursal en una franquicia | `idFranquicia` en la ruta | `{"nombre": "Sucursal Norte"}` | 201 |
| POST | `/api/sucursales/{idSucursal}/productos` | Agregar producto a una sucursal | `idSucursal` en la ruta | `{"nombre": "Paracetamol 500", "stock": 100}` | 201 |
| DELETE | `/api/productos/{id}` | Eliminar un producto | `id` en la ruta | — | 204 |
| PATCH | `/api/productos/{id}/stock` | Actualizar stock de un producto | `id` en la ruta | `{"stock": 50}` | 200 |
| GET | `/api/franquicias/{idFranquicia}/top-productos` | Producto con mayor stock de cada sucursal de una franquicia | `idFranquicia` en la ruta | — | 200 |
| GET | `/api/franquicias/{idFranquicia}/productos` | Listar productos de una franquicia con su sucursal | `idFranquicia` en la ruta | — | 200 |

### Endpoints adicionales (puntos extra)

| Método | Ruta | Descripción | Parámetros | Body JSON | Código esperado |
|---|---|---|---|---|---|
| PATCH | `/api/franquicias/{id}/nombre` | Actualizar nombre de una franquicia | `id` en la ruta | `{"nombre": "Nuevo nombre"}` | 200 |
| PATCH | `/api/sucursales/{id}/nombre` | Actualizar nombre de una sucursal | `id` en la ruta | `{"nombre": "Nuevo nombre"}` | 200 |
| PATCH | `/api/productos/{id}/nombre` | Actualizar nombre de un producto | `id` en la ruta | `{"nombre": "Nuevo nombre"}` | 200 |

Respuesta de error ante datos inválidos (400) o recurso inexistente (404):

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "nombre: El campo es obligatorio y no puede estar vacío",
  "timestamp": "2026-11-06T10:15:32.123456"
}
```

## 8. Ejemplos de uso

### Crear franquicia (criterio #2)

```powershell
curl.exe -X POST http://localhost:8080/api/franquicias -H "Content-Type: application/json" -d "{\"nombre\":\"Farmacias XYZ\"}"
```

Respuesta 201:

```json
{"id":1,"nombre":"Farmacias XYZ"}
```

### Crear sucursal en una franquicia (criterio #3)

```powershell
curl.exe -X POST http://localhost:8080/api/franquicias/1/sucursales -H "Content-Type: application/json" -d "{\"nombre\":\"Sucursal Norte\"}"
```

Respuesta 201:

```json
{"id":1,"nombre":"Sucursal Norte","idFranquicia":1}
```

### Agregar producto a una sucursal (criterio #4)

```powershell
curl.exe -X POST http://localhost:8080/api/sucursales/1/productos -H "Content-Type: application/json" -d "{\"nombre\":\"Paracetamol 500\",\"stock\":100}"
```

Respuesta 201:

```json
{"id":1,"nombre":"Paracetamol 500","stock":100}
```

### Actualizar stock de un producto (criterio #6)

```powershell
curl.exe -X PATCH http://localhost:8080/api/productos/1/stock -H "Content-Type: application/json" -d "{\"stock\":50}"
```

Respuesta 200 (el producto actualizado):

```json
{"id":1,"nombre":"Paracetamol 500","stock":50}
```

### Eliminar un producto (criterio #5)

```powershell
curl.exe -X DELETE http://localhost:8080/api/productos/1
```

Respuesta 204 sin cuerpo. Si el producto no existe, responde 404.

### Actualizar nombre de una franquicia (punto extra #3)

```powershell
curl.exe -X PATCH http://localhost:8080/api/franquicias/1/nombre -H "Content-Type: application/json" -d "{\"nombre\":\"Nuevo nombre\"}"
```

### Actualizar nombre de una sucursal (punto extra #4)

```powershell
curl.exe -X PATCH http://localhost:8080/api/sucursales/1/nombre -H "Content-Type: application/json" -d "{\"nombre\":\"Sucursal Renombrada\"}"
```

### Actualizar nombre de un producto (punto extra #5)

```powershell
curl.exe -X PATCH http://localhost:8080/api/productos/1/nombre -H "Content-Type: application/json" -d "{\"nombre\":\"Producto Renombrado\"}"
```

### Consultar productos con mayor stock por sucursal (criterio #7)

```powershell
curl.exe http://localhost:8080/api/franquicias/2/top-productos
```

Respuesta (01 producto por sucursal, incluido el nombre de la sucursal):

```json
[
  {"id":3,"nombre":"monitor","stock":50,"idSucursal":1,"nombreSucursal":"Sucursal centro"},
  {"id":4,"nombre":"iphone","stock":50,"idSucursal":2,"nombreSucursal":"Sucursal norte"}
]
```

### Listar productos de una franquicia con su sucursal (criterio #7)

```powershell
curl.exe http://localhost:8080/api/franquicias/2/productos
```

Respuesta (todos los productos y el nombre de la sucursal):

```json
[
  {"id":1,"nombre":"Laptop","stock":20,"idSucursal":1,"nombreSucursal":"Sucursal centro"},
  {"id":2,"nombre":"mouse","stock":30,"idSucursal":1,"nombreSucursal":"Sucursal centro"},
  {"id":3,"nombre":"monitor","stock":50,"idSucursal":1,"nombreSucursal":"Sucursal centro"},
  {"id":4,"nombre":"iphone","stock":50,"idSucursal":2,"nombreSucursal":"Sucursal norte"}
]
```

## 9. Pruebas

Con las variables del `.env` **ya cargadas** en la sesión de PowerShell (paso 6) y MySQL levantado:

- **Unitarias** (no requieren MySQL ni variables de entorno):
  ```powershell
  .\mvnw.cmd test
  ```

- **De integración** (`@Tag("integracion")`; requieren MySQL levantado y las variables del `.env` cargadas):
  ```powershell
  .\mvnw.cmd verify
  ```

- **Suite completa**: el propio `mvnw verify` ejecuta primero las unitarias y luego las de integración, por lo que es el comando con la cobertura total del proyecto.

## 10. Estructura del proyecto

Organización por funcionalidad con separación de capas:

- `controller/`: expone los endpoints HTTP; delega en los servicios. Sin lógica de negocio.
- `service/`: contiene las reglas y validaciones de negocio.
- `repository/`: interfaces de acceso a datos Spring Data R2DBC (consultas derivadas y consultas SQL personalizadas con `@Query`).
- `domain/`: entidades anotadas con `@Table` (franquicia, sucursal, producto).
- `dto/`: objetos de entrada/salida de la API, separados de las entidades de dominio; validados con Jakarta Validation.
- `shared/`: componentes transversales: `shared/exception` (excepciones de negocio y `GlobalExceptionHandler` con manejo global de errores) y `shared/error` (`ErrorResponse`).

## 11. Consideraciones adicionales

- Aplicación usa **Spring Data R2DBC** con MySQL. La configuración se toma de las variables `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` (ver variables de entorno en paso 4).
- Puerto de la aplicación: **8080**. Puerto de MySQL: **3306** (publicado por Docker Compose desde el contenedor `franquicias-mysql`).
- La persistencia de datos vive en el volumen Docker `mysql_data`; por eso los datos persisten entre reinicios del contenedor.
- Si MySQL no está saludable, ver los logs del contenedor con `docker compose logs mysql`.
- Si la aplicación no arranca, revisar los logs de la sesión de ejecución (`spring-boot:run`). Errores comunes: variables de entorno del `.env` sin cargar, o contenedor MySQL no levantado.
- La base de datos y las tablas se crean de forma idempotente al arranque (`schema.sql` con `CREATE TABLE IF NOT EXISTS`); no es necesario ejecutar scripts manualmente.
- El proyecto no incluye despliegue en la nube ni aprovisionamiento IaC.
