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

### Para ejecutar TODO el stack con Docker Compose (Opción A)

Obligatorios:

- **Git**: para clonar el repositorio.
- **Docker Desktop** con Docker Compose: compila la imagen de la API y ejecuta MySQL.

Con Docker **NO es necesario tener Java ni Maven instalados localmente**: el JDK 21 y Maven que compilan el proyecto lo hace Docker internamente (etapas del Dockerfile).

Opcionales:

- Un cliente HTTP (Postman, curl o similar) para probar los endpoints.

### Para desarrollo local con Maven (Opción B)

Obligatorios:

- **JDK 21**: verificar con `java -version` (debe indicar versión 21.x).
- **Docker Desktop**: para ejecutar MySQL en contenedor.

Opcionales:

- IDE: IntelliJ IDEA, VS Code o Eclipse con soporte de Java 21.
- Un cliente HTTP (Postman, curl o similar).

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

> **Nota por modalidad:** en la **Opción A (Docker Compose)** solo hacen falta `MYSQL_ROOT_PASSWORD` y `MYSQL_FRANQUICIAS_APP_PASSWORD`, porque Compose configura directamente las variables `DB_*` del contenedor de la API (incluida la URL con host interno `mysql`). Las variables `DB_*` del `.env` se usan únicamente en la **Opción B (Maven local)**.

El archivo `.env` está excluido en `.gitignore` y no debe commitearse. `.env.example` sí se sube al repositorio solo como referencia sin valores reales.

## 5. Ejecución de MySQL con Docker Compose (para la Opción B)

MySQL corre en Docker en todas las modalidades. Para iniciar **solo MySQL** (sin la API, que en la Opción B se ejecuta con Maven):

```powershell
# iniciar solo el contenedor de MySQL (lee el .env de la raíz automáticamente)
docker compose up -d mysql

# verificar que el estado del contenedor sea "healthy"
docker compose ps
```

> Si ejecuta `docker compose up -d` **sin indicar el servicio**, Compose levantará también el servicio `api` (la API dentro de Docker, Opción A).

Para detener los contenedores conservando los datos:

```powershell
docker compose down
```

`docker compose down` detiene tanto MySQL como la API si está corriendo, pero **NO borra la información**: los datos se conservan en el volumen nombrado `mysql_data`. Solo si quiere eliminarla por completo (borra todos los datos):

```powershell
docker compose down -v   # ¡cuidado: borra todos los datos!
```

## 6. Ejecución de la aplicación

### Opción A: TODO el stack con Docker Compose (API + MySQL) — recomendada

Requiere únicamente **Git y Docker Desktop** (ver requisitos previos). Con el `.env` ya creado (paso 4), clona el proyecto, entra a su carpeta y construye e inicia ambos servicios:

```powershell
git clone <URL-DE-TU-REPOSITORIO-GITHUB>
cd pruebaAccenture
```

```powershell
docker compose up -d --build
```

Comprueba el estado: `api` y `mysql` deben estar `running` y `mysql` además `healthy`:

```powershell
docker compose ps
```

La API arranca **solo cuando MySQL ya está saludable** (dependencia `service_healthy`); no es necesario esperar manualmente. La API queda en `http://localhost:8080`. Dentro de la red de Docker, la API se conecta a MySQL usando el nombre del servicio como host (`r2dbc:mysql://mysql:3306/franquicias_db`), configurado automáticamente por Compose.

Comandos útiles:

```powershell
# logs de la API (y de MySQL si hace falta)
docker compose logs -f api
docker compose logs mysql

# detener todo conservando los datos
docker compose down

# reconstruir y reiniciar solo la API tras cambios de código
docker compose up -d --build api
```

El volumen `mysql_data` conserva los datos entre reinicios; `docker compose down` NO los borra.

**Acceso con Postman (y cualquier cliente HTTP):** una vez que `docker compose ps` muestre la API en ejecución, apunte sus peticiones a la base URL `http://localhost:8080` (puerto publicado por Compose). La API no exige autenticación ni cabeceras especiales más allá de `Content-Type: application/json` en los cuerpos. Use la tabla de endpoints (paso 7) y los ejemplos (paso 8) para armar las peticiones.

### Opción B: ejecución local con Maven (desarrollo)

Requiere **JDK 21** y Docker Desktop (MySQL en Docker; inícielo con `docker compose up -d mysql`, paso 5). Spring Boot **no lee el archivo `.env`** cuando se ejecuta con Maven. Antes de arrancar o ejecutar pruebas, cargue las variables en la sesión de PowerShell:

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

Requieren JDK 21 (modalidad Opción B). Con las variables del `.env` **ya cargadas** en la sesión de PowerShell (bloque de carga de la Opción B) y MySQL levantado (`docker compose up -d mysql`):

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
- La imagen Docker de la API es multi-etapa (Maven+JDK21 para compilar, solo JRE en la imagen final), corre con un usuario no root y no contiene secretos: las credenciales se inyectan al arrancar por Compose desde el `.env`.
- Las pruebas automatizadas se ejecutan fuera de Docker con `mvnw test` y `mvnw verify` (necesitan MySQL local alcanzable en `localhost:3306`).
- Si MySQL no está saludable, ver los logs del contenedor con `docker compose logs mysql`.
- Si la aplicación no arranca, revisar los logs de la sesión de ejecución (`spring-boot:run`). Errores comunes: variables de entorno del `.env` sin cargar, o contenedor MySQL no levantado.
- La base de datos y las tablas se crean de forma idempotente al arranque (`schema.sql` con `CREATE TABLE IF NOT EXISTS`); no es necesario ejecutar scripts manualmente.
- El proyecto no incluye despliegue en la nube ni aprovisionamiento IaC.
