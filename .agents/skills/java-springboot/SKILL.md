
---
name: java-springboot
description: "Spring Boot best practices for Java 21 reactive backend applications using WebFlux, Spring Data R2DBC and MySQL. Use when designing, implementing or reviewing Spring Boot applications."
---

# Spring Boot Best Practices

## Objective

Build secure, maintainable and testable Spring Boot backend applications.

This project uses Spring Boot, Java 21, Spring WebFlux, Spring Data R2DBC and MySQL.

All recommendations must respect the reactive architecture.

## 1. Project Configuration

Use:

- Java 21.
- Spring Boot.
- Maven.
- Spring WebFlux.
- Spring Data R2DBC.
- MySQL R2DBC driver.
- Jakarta Bean Validation.

Use Spring Boot dependency management whenever possible.

Do not manually specify dependency versions when Spring Boot already manages compatible versions.

Do not introduce Spring Data JPA, Hibernate ORM or JDBC into this project.

## 2. Architecture

Prefer clear separation of responsibilities.

Recommended responsibilities:

### Domain

Contains:

- Business entities.
- Domain rules.
- Business exceptions.
- Domain behavior.

### Application

Contains:

- Use cases.
- Application services.
- Business workflows.
- Coordination between domain components and persistence interfaces.

### Infrastructure

Contains:

- R2DBC persistence implementations.
- Database mappings.
- External integrations.
- Configuration.
- Technical adapters.

### API

Contains:

- REST controllers.
- Request DTOs.
- Response DTOs.
- HTTP exception handling.

Adapt package organization to the actual project structure.

Do not introduce unnecessary architectural complexity.

## 3. Dependency Injection

Always prefer constructor injection.

Example:

```java
@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }
}
```

Use immutable dependency fields whenever possible.

Avoid field injection.

## 4. Spring WebFlux

Use Spring WebFlux for HTTP endpoints.

Controllers should return reactive types.

Example:

```java
@RestController
@RequestMapping("/api/franchises")
public class FranchiseController {

    private final FranchiseService franchiseService;

    public FranchiseController(FranchiseService franchiseService) {
        this.franchiseService = franchiseService;
    }

    @GetMapping
    public Flux<FranchiseResponse> findAll() {
        return franchiseService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<FranchiseResponse> findById(@PathVariable Long id) {
        return franchiseService.findById(id);
    }
}
```

Use:

- `Mono<T>` for zero or one result.
- `Flux<T>` for zero or multiple results.

Do not return blocking results from reactive endpoints.

## 5. Reactive Programming

Use Project Reactor consistently.

### Operators

- `map`: synchronous transformation.
- `flatMap`: asynchronous transformation returning a publisher.
- `filter`: filtering emitted values.
- `switchIfEmpty`: handling empty publishers.
- `onErrorResume`: controlled error recovery.
- `zip`: combining independent publishers.
- `then`: continuing after completion without using the previous value.

### Rules

- Compose operations instead of manually subscribing.
- Keep reactive pipelines readable.
- Propagate errors appropriately.
- Avoid unnecessary nested publishers.
- Avoid blocking event-loop threads.

Never use:

```java
block();
blockFirst();
blockLast();
```

inside application request flows.

Never assume that wrapping a blocking operation in `Mono.just()` makes it non-blocking.

Do not manually call `subscribe()` inside controllers or application services to execute business operations.

## 6. Persistence with Spring Data R2DBC

Use reactive database access.

Repository example:

```java
public interface FranchiseRepository
        extends ReactiveCrudRepository<Franchise, Long> {

    Mono<Franchise> findByName(String name);
}
```

Use:

- `ReactiveCrudRepository`.
- `R2dbcEntityTemplate` when more complex database operations are needed.
- Parameterized SQL queries for custom queries.
- R2DBC-compatible entity mappings.

Do not use:

- `JpaRepository`.
- `EntityManager`.
- `JdbcTemplate`.
- Hibernate ORM.
- Blocking Spring Data repositories.

Ensure database relationships and foreign keys correctly represent:

- Franchise to branches.
- Branch to products.

Avoid N+1 query problems and unnecessary database round trips.

## 7. MySQL R2DBC Configuration

Configure the connection through environment variables.

Example:

```properties
spring.r2dbc.url=${DB_URL:r2dbc:mysql://localhost:3306/franchise_db}
spring.r2dbc.username=${DB_USERNAME:app}
spring.r2dbc.password=${DB_PASSWORD}
```

The password must be supplied through the environment.

Do not commit real database credentials.

Use Docker Compose for local MySQL infrastructure when appropriate.

Use R2DBC connection pooling only when justified and correctly configured.

## 8. Transactions

Use reactive transaction management.

- Use ReactiveTransactionManager.
- Use reactive transaction operators or supported reactive `@Transactional` methods.
- Keep transaction boundaries explicit.
- Ensure database operations participate in the same reactive transaction when required.

Do not mix blocking and reactive transaction managers.

## 9. REST API Design

Use consistent RESTful resource naming.

Example:

```text
POST   /api/franchises
GET    /api/franchises
GET    /api/franchises/{franchiseId}

POST   /api/franchises/{franchiseId}/branches

POST   /api/branches/{branchId}/products
DELETE /api/products/{productId}

PATCH  /api/products/{productId}/stock

GET    /api/franchises/{franchiseId}/top-products
GET    /api/franchises/{franchiseId}/products
```

Treat these as proposed endpoints, subject to the approved API contract.

Use appropriate HTTP methods and status codes:

- `200 OK`: successful retrieval or update.
- `201 Created`: resource creation.
- `204 No Content`: successful deletion without a response body.
- `400 Bad Request`: invalid request.
- `404 Not Found`: resource does not exist.
- `409 Conflict`: business or uniqueness conflict when applicable.
- `500 Internal Server Error`: unexpected server error.

Avoid returning `200 OK` for every operation.

## 10. DTOs and Validation

Separate API DTOs from persistence entities.

Example:

```java
public record CreateProductRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @PositiveOrZero
        Integer stock
) {
}
```

Use `@Valid` on request bodies.

Apply validation based on real business rules.

Do not expose database entities directly in API responses.

## 11. Exception Handling

Use centralized exception handling with `@RestControllerAdvice`.

Handle:

- Missing franchises.
- Missing branches.
- Missing products.
- Invalid stock values.
- Invalid request payloads.
- Persistence errors.

Return consistent error response structures.

Do not expose stack traces or SQL implementation details.

Preserve meaningful HTTP status codes.

## 12. Testing

Use:

- JUnit 5.
- Mockito.
- Reactor Test.
- WebTestClient.
- Spring Boot integration tests.

### Unit tests

Test application services and business rules independently.

### Reactive tests

Use `StepVerifier` to verify:

- Emitted values.
- Empty publishers.
- Completion signals.
- Error signals.

### API tests

Use `WebTestClient` to verify:

- HTTP methods.
- Status codes.
- Request validation.
- Response bodies.
- Error handling.

### Persistence tests

Verify R2DBC queries and relationships against a real or appropriately managed MySQL test database.

Consider Testcontainers when its setup is justified.

Do not introduce JPA testing annotations such as `@DataJpaTest`.

Do not use `@WebMvcTest` for WebFlux controllers.

## 13. Security

- Validate incoming requests.
- Never hardcode credentials.
- Use environment variables for secrets.
- Use parameterized SQL queries.
- Restrict database privileges.
- Avoid exposing internal exceptions.
- Do not log sensitive information.
- Review dependency vulnerabilities.
- Do not introduce unnecessary security dependencies.

Only implement authentication and authorization when required by the specification or approved design.

## 14. Logging and Observability

Use SLF4J.

Prefer parameterized logging.

Example:

```java
log.info("Creating branch for franchise {}", franchiseId);
```

Log important business and technical events.

Avoid logging passwords, tokens or sensitive configuration.

Avoid excessive logging inside reactive pipelines.

## 15. Configuration and Profiles

Use Spring configuration files for non-sensitive application settings.

Use profiles when different environments require different configuration.

Prefer `@ConfigurationProperties` for grouped and type-safe configuration.

Do not commit production credentials.

Keep example configuration safe and documented.

## 16. Performance

- Avoid unnecessary database queries.
- Use appropriate indexes.
- Avoid N+1 access patterns.
- Return only required response fields.
- Consider pagination for potentially large result sets.
- Avoid blocking event-loop threads.
- Avoid unnecessary subscriptions.
- Keep reactive pipelines efficient and understandable.

Do not optimize prematurely without evidence.

## 17. Docker and Deployment

Use Docker Compose for local infrastructure when appropriate.

Document:

- Required environment variables.
- Database configuration.
- Application startup.
- Database initialization.
- Build and test commands.

Never include production secrets in Dockerfiles or committed Compose files.

## Final Review Checklist

Before considering a Spring Boot implementation complete:

1. Uses the approved Java and Spring Boot versions.
2. Uses WebFlux for reactive HTTP endpoints.
3. Uses R2DBC for database persistence.
4. Uses MySQL as the selected database.
5. Avoids JPA and JDBC.
6. Uses Mono and Flux consistently.
7. Avoids blocking operations in reactive request flows.
8. Implements validation and centralized error handling.
9. Maintains correct franchise, branch and product relationships.
10. Includes meaningful automated tests.
11. Does not expose credentials.
12. Builds and verifies successfully with Maven.

Always prioritize correctness, maintainability, security and simplicity.
