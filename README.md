# pruebaAccenture — API de Franquicias

## 1. Descripción del proyecto

API REST para la administración centralizada de una red comercial de franquicias: altas de franquicias y sucursales, catálogo de productos por sucursal, gestión de stock y consultas de inventario.

Resuelve el problema de conocer el estado de un inventario distribuido: qué productos vende cada sucursal, cuánto stock tiene cada uno y cuál es el producto de mayor stock en cada sucursal de una franquicia, sin tener que recorrer cada punto de venta.

Modelo de negocio:

- Una **franquicia** tiene un nombre y agrupa varias **sucursales**.
- Una **sucursal** pertenece a una franquicia, tiene un nombre y su propio catálogo de **productos**.
- Un **producto** pertenece a una sucursal, tiene un nombre y una cantidad de **stock**.

## 2. Funcionalidades

### Obligatorias (criterios de aceptación)

- Crear franquicias.
- Crear sucursales asociadas a una franquicia.
- Agregar productos a una sucursal.
- Eliminar productos.
- Actualizar el stock de un producto.
- Consultar el producto con mayor stock de cada sucursal de una franquicia (si hay empate, gana el ID más bajo).
- Listar los productos de una franquicia indicando la sucursal a la que pertenece cada uno.

### Adicionales (puntos extra)

- Actualizar el nombre de una franquicia, de una sucursal y de un producto.

El detalle de rutas, cuerpos y códigos HTTP está en la sección [6. Endpoints](#6-endpoints).

## 3. Tecnologías

| Tecnología | Función |
|---|---|
| Java 21 | Lenguaje de la aplicación. |
| Spring Boot 4.1.2 | Framework base de la API. |
| Spring WebFlux (Netty) | Capa HTTP reactiva (endpoints sobre `Mono` y `Flux`). |
| Spring Data R2DBC + driver `r2dbc-mysql` (io.asyncer) | Acceso a datos no bloqueante hacia MySQL. |
| MySQL 8.4 | Base de datos, ejecutada en Docker. |
| Docker y Docker Compose | Construcción de la imagen de la API y orquestación de API + MySQL. |
| Maven Wrapper (`mvnw.cmd` / `mvnw`) | Compilación y pruebas sin instalar Maven. |
| JUnit 5, Mockito y StepVerifier (Reactor Test) | Pruebas unitarias y de integración. |

La aplicación es **reactiva** de extremo a extremo: controladores, servicios y repositorios componen flujos `Mono`/`Flux` sin llamadas bloqueantes, sin mezclar acceso bloqueante a la base de datos (no se usa JPA ni JDBC).

## 4. Requisitos e instalación

| | Opción A: Docker Compose (recomendada) | Opción B: desarrollo local |
|---|---|---|
| Necesita | Git y Docker Desktop | JDK 21 y Docker Desktop |
| No necesita | Java ni Maven instalados (el JDK 21 y Maven que compilan el proyecto se ejecutan dentro de Docker) | Maven (usa el Maven Wrapper) |
| Cómo empezar | Sección 5 | Sección 9 |

En la Opción A, la persona que clona el repositorio no necesita más requisitos: Docker Desktop se encarga de construir la imagen (compilando y empaquetando el proyecto dentro del propio build) y de ejecutar MySQL y la API en contenedores separados.

## 5. Configuración y ejecución con Docker

Docker ejecuta dos contenedores: `mysql` (base de datos MySQL 8.4) y `api` (Spring Boot, imagen construida desde el `Dockerfile`). La API arranca cuando MySQL reporta estar saludable (`depends_on: condition: service_healthy`) y los datos persisten en el volumen Docker `mysql_data`.

1. **Clonar el repositorio:**
2. **Crear el archivo `.env`** a partir del ejemplo:

   Copy-Item .env.example .env

3. **Configurar las contraseñas** editando `.env`. Para la Opción A solo se leen estas dos variables:

   | Variable | Significado |
   |---|---|
   | `MYSQL_ROOT_PASSWORD` | Contraseña de root de MySQL (interna del contenedor; la API no la usa). |
   | `MYSQL_FRANQUICIAS_APP_PASSWORD` | Contraseña del usuario de aplicación `franquicias_app`, que Compose crea e inyecta en el contenedor de la API. |

   Los valores `DB_*` del `.env` no se usan en esta modalidad: Compose configura directamente el contenedor de la API con la URL `r2dbc:mysql://mysql:3306/franquicias_db` (host interno `mysql`) y la contraseña de `franquicias_app`.

4. **Construir e iniciar ambos servicios:**
   ```powershell
   docker compose up -d --build
   ```
5. **Comprobar los servicios** (`mysql` debe verse `healthy` y `api` `running`):

   ```powershell
   docker compose ps
   ```

6. **Consultar logs** si es necesario:

   ```powershell
   docker compose logs -f api   # logs de la API
   docker compose logs mysql    # logs de MySQL
   ```

7. **Acceder a la API desde Postman:** base URL `http://localhost:8080`, sin autenticación. Las peticiones con cuerpo requieren `Content-Type: application/json`. Use las rutas de la [sección 6](#6-endpoints) y los ejemplos de la [sección 7](#7-ejemplos-de-uso).

8. **Detener los servicios conservando los datos:**

   ```powershell
   docker compose down
   ```

   `down` detiene los contenedores sin borrar datos (quedan en el volumen `mysql_data`). Tras cambiar código, reconstruya solo la API con `docker compose up -d --build api`. Para eliminar también los datos: `docker compose down -v` (advertencia: borra todo el inventario registrado).

## 6. Endpoints

Base URL: `http://localhost:8080`. Los `/api` prefijan todas las rutas.

### Endpoints obligatorios

| Método | Ruta | Descripción | Body JSON | Código |
|---|---|---|---|---|
| POST | `/api/franquicias` | Crear franquicia | `{"nombre": "Farmacias XYZ"}` | 201 |
| POST | `/api/franquicias/{idFranquicia}/sucursales` | Crear sucursal en una franquicia | `{"nombre": "Sucursal Norte"}` | 201 |
| POST | `/api/sucursales/{idSucursal}/productos` | Agregar producto a una sucursal | `{"nombre": "Paracetamol 500", "stock": 100}` | 201 |
| DELETE | `/api/productos/{id}` | Eliminar producto | — | 204 |
| PATCH | `/api/productos/{id}/stock` | Actualizar stock | `{"stock": 50}` | 200 |
| GET | `/api/franquicias/{idFranquicia}/top-productos` | Producto con mayor stock de cada sucursal de la franquicia | — | 200 |
| GET | `/api/franquicias/{idFranquicia}/productos` | Listar productos de la franquicia con su sucursal | — | 200 |

### Endpoints adicionales (puntos extra)

| Método | Ruta | Descripción | Body JSON | Código |
|---|---|---|---|---|
| PATCH | `/api/franquicias/{id}/nombre` | Renombrar franquicia | `{"nombre": "Nuevo nombre"}` | 200 |
| PATCH | `/api/sucursales/{id}/nombre` | Renombrar sucursal | `{"nombre": "Nuevo nombre"}` | 200 |
| PATCH | `/api/productos/{id}/nombre` | Renombrar producto | `{"nombre": "Nuevo nombre"}` | 200 |

Reglas de validación: los nombres son obligatorios, de hasta 150 caracteres, y el stock no puede ser negativo.

Formato de error (400 con datos inválidos, 404 si el recurso no existe, 409 en conflicto de integridad referencial):

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "nombre: El campo es obligatorio y no puede estar vacío",
  "timestamp": "2026-11-06T10:15:32.123456"
}
```

## 7. Ejemplos de uso

> Los **IDs de los ejemplos no están garantizados**: cada instalación genera registros propios. Use los IDs devueltos por cada respuesta `201 Created` para encadenar las siguientes peticiones. A continuación se muestra ese encadenamiento real con PowerShell.

Crear una franquicia y una sucursal dentro de ella, usando el `id` de cada respuesta:

```powershell
$f = & curl.exe -s -X POST http://localhost:8080/api/franquicias -H "Content-Type: application/json" -d "{\"nombre\":\"Farmacias XYZ\"}" | ConvertFrom-Json

& curl.exe -s -X POST http://localhost:8080/api/franquicias/$($f.id)/sucursales -H "Content-Type: application/json" -d "{\"nombre\":\"Sucursal Norte\"}"
```

Agregar un producto con su stock a la sucursal creada:

```powershell
& curl.exe -s -X POST http://localhost:8080/api/sucursales/1/productos -H "Content-Type: application/json" -d "{\"nombre\":\"Paracetamol 500\",\"stock\":100}"
```

Respuesta 201: `{"id":1,"nombre":"Paracetamol 500","stock":100}`

Actualizar stock, renombrar y eliminar un producto:

```powershell
& curl.exe -s -X PATCH http://localhost:8080/api/productos/1/stock -H "Content-Type: application/json" -d "{\"stock\":50}"

& curl.exe -s -X DELETE http://localhost:8080/api/productos/1
```

Consultas de inventario de una franquicia (200):

```powershell
# producto con mayor stock de cada sucursal de la franquicia
& curl.exe -s http://localhost:8080/api/franquicias/1/top-productos

# todos los productos de la franquicia con el nombre de su sucursal
& curl.exe -s http://localhost:8080/api/franquicias/1/productos
```

Respuesta típica (incluye `nombreSucursal`; los valores dependen de sus datos):

```json
[
  {"id":1,"nombre":"Paracetamol 500","stock":50,"idSucursal":1,"nombreSucursal":"Sucursal Norte"}
]
```

Si la franquicia o el producto no existen, la API responde `404` con el formato de error de la [sección 6](#6-endpoints).

## 8. Pruebas

Requisitos: JDK 21 y, para la suite de integración, MySQL corriendo (`docker compose up -d mysql`) y las variables del `.env` cargadas en PowerShell (ver [sección 9](#9-ejecución-para-desarrollo-local)). Las pruebas unitarias no necesitan MySQL ni variables.

| Tipo | Comando | Detalles |
|---|---|---|
| Unitarias | `.\mvnw.cmd test` | No requieren MySQL ni variables de entorno. |
| De integración | `.\mvnw.cmd verify` | Etiquetadas con `@Tag("integracion")`; requieren MySQL levantado y las variables del `.env` cargadas. `verify` siempre las ejecuta después de las unitarias, por lo que no existe una corrida realmente aislada en el flujo Maven normal. |
| Suite completa | `.\mvnw.cmd verify` | Unitarias + integración (cobertura total del proyecto). |

Nota: las pruebas se ejecutan fuera de Docker con el Maven Wrapper, ya que necesitan el JDK local y acceso a `localhost:3306`.

## 9. Ejecución para desarrollo local

Para iterar con Maven y recarga manual, se usa el MySQL de Docker y Spring Boot local:

1. **Iniciar solo MySQL** (sin la API de Docker; `docker compose up -d` sin el servicio levantaría también la API):

   ```powershell
   docker compose up -d mysql
   docker compose ps   # esperar que mysql esté "healthy"
   ```

2. **Cargar las variables del `.env`** en la sesión de PowerShell (Spring Boot con Maven no lee `.env` automáticamente):

   ```powershell
   Get-Content .env | ForEach-Object {
       if ($_ -match '^\s*([^#][^=]+?)\s*=\s*(.*)\s*$') {
           [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
       }
   }
   Get-ChildItem Env:DB_*, Env:MYSQL_*   # comprobar que quedaron cargadas
   ```

   En esta modalidad se usan las variables `DB_*` (con `DB_URL` apuntando a `localhost:3306`). Solo valen para la sesión actual; al abrir una terminal nueva hay que cargarlas de nuevo.

3. **Arrancar Spring Boot:**

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   La API queda en `http://localhost:8080`. Al arrancar, Spring aplica `schema.sql` (`CREATE TABLE IF NOT EXISTS`), que crea tablas de forma idempotente sin scripts manuales.

## 10. Estructura del proyecto

Organización por funcionalidad, con separación de responsabilidades HTTP, negocio, datos y modelo:

```
com.accenture.pruebaaccenture
├── franquicia/                  # funcionalidad "franquicia"
│   ├── controller/  (FranquiciaController)
│   ├── service/     (FranquiciaService)
│   ├── repository/  (FranquiciaRepository — R2DBC)
│   ├── domain/      (Franquicia)
│   └── dto/         (CrearFranquiciaRequest, FranquiciaResponse, ActualizarNombreRequest)
├── sucursal/                    # funcionalidad "sucursal" (misma estructura interna)
├── producto/                    # funcionalidad "producto" (misma estructura interna)
└── shared/                      # común a todas las funcionalidades
    ├── exception/   (excepciones NotFound y GlobalExceptionHandler)
    └── error/       (ErrorResponse)
```

Responsabilidades por capa:

- `controller/`: expone los endpoints HTTP, valida la entrada a través de DTOs anotados con Jakarta Validation y delega en el servicio. Sin lógica de negocio.
- `service/`: concentra las reglas de negocio (existencia previa de franquicia/sucursal, transacciones, renombres, consulta de mayor stock).
- `repository/`: interfaces de Spring Data R2DBC: consultas derivadas y consultas SQL personalizadas con `@Query`.
- `domain/`: entidades de persistencia anotadas con `@Table` (mapeadas a las tablas de MySQL).
- `dto/`: objetos de entrada/salida de la API, separados de las entidades del dominio.
- `shared/`: manejo global de errores y excepciones de negocio, comunes a todas las funcionalidades.

Archivos de configuración relevantes (raíz del proyecto): `Dockerfile` (imagen multi-etapa de la API), `docker-compose.yml` (servicios `mysql` y `api`), `.env.example` (plantilla de configuración) y `src/main/resources/schema.sql` (DDL de tablas + FKs + CHECK de stock, respetando el modelo sin `ON DELETE CASCADE`).
