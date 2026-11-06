
---
name: java-coding-standards
description: "Java 21 coding standards for Spring Boot reactive backend applications using WebFlux, R2DBC and MySQL. Use when writing, reviewing or refactoring Java code."
---

# Java Coding Standards

## Objective

Maintain readable, secure, testable and maintainable Java code for Spring Boot backend applications.

Prioritize clarity, correctness and simplicity over unnecessary abstractions.

## Technology Context

This project uses:

- Java 21.
- Spring Boot.
- Spring WebFlux.
- Spring Data R2DBC.
- MySQL with the R2DBC driver.
- Maven.
- JUnit 5 and Mockito for testing.

Do not introduce Quarkus, Spring Data JPA, Hibernate ORM or JDBC into this project.

## 1. Naming Conventions

Follow standard Java naming conventions:

- Classes and records: `PascalCase`.
- Methods and variables: `camelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- Packages: lowercase.
- Interfaces: descriptive names without unnecessary prefixes.

Examples:

```java
public class FranchiseService {
}

public record ProductResponse(Long id, String name, Integer stock) {
}

private static final int MAX_STOCK = 10000;

public Mono<Product> findProductById(Long productId) {
    // Implementation
}
```

Use meaningful names that express business intent.

Avoid abbreviations unless they are widely understood.

## 2. Immutability

- Prefer immutable objects where practical.
- Use `private final` for required dependencies.
- Prefer Java records for immutable DTOs.
- Avoid unnecessary setters.
- Avoid shared mutable static state.
- Keep domain state changes explicit.

Example:

```java
public record CreateFranchiseRequest(
        String name
) {
}
```

Do not force immutability when the persistence framework requires a particular entity mapping strategy. Follow the established R2DBC mapping conventions.

## 3. Dependency Injection

Use constructor injection.

Preferred:

```java
@Service
public class FranchiseService {

    private final FranchiseRepository franchiseRepository;

    public FranchiseService(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }
}
```

Avoid field injection:

```java
@Autowired
private FranchiseRepository franchiseRepository;
```

Constructor injection makes dependencies explicit and simplifies testing.

## 4. Package Organization

Organize code by business feature or domain when appropriate.

Example:

```text
com.accenture.pruebaaccenture/
    franchise/
        controller/
        service/
        repository/
        domain/
        dto/
    branch/
        controller/
        service/
        repository/
        domain/
        dto/
    product/
        controller/
        service/
        repository/
        domain/
        dto/
    shared/
        exception/
        config/
```

Adapt the structure to the approved architecture.

Do not create packages or layers that have no meaningful responsibility.

Keep HTTP concerns separate from business rules and database access.

## 5. Optional Usage

Use `Optional<T>` when representing an optional synchronous value.

Do not use `Optional` as a replacement for reactive publishers.

For reactive repository operations:

- Use `Mono<T>` for zero or one result.
- Use `Flux<T>` for zero or multiple results.

Avoid calling `.block()` to convert reactive results into synchronous values.

## 6. Collections and Streams

Use Java collections and streams for in-memory data transformations.

Prefer readable pipelines.

Example:

```java
List<String> names = products.stream()
        .map(Product::getName)
        .toList();
```

Avoid deeply nested streams and unnecessarily complex transformations.

Do not confuse Java Streams with Reactor Flux.

Use Reactor operators for asynchronous reactive pipelines.

## 7. Reactive Programming

This project uses Spring WebFlux and Project Reactor.

### Mono and Flux

- `Mono<T>` represents zero or one asynchronous result.
- `Flux<T>` represents zero or multiple asynchronous results.

### Rules

- Return reactive types throughout the reactive application flow.
- Compose asynchronous operations using Reactor operators.
- Use `map` for synchronous transformations.
- Use `flatMap` for asynchronous operations returning publishers.
- Use `filter` when filtering emitted values.
- Use `switchIfEmpty` for empty publisher handling.
- Use `zip` when combining independent publishers.
- Handle errors using appropriate Reactor operators.

Example:

```java
public Mono<Product> findProduct(Long productId) {
    return productRepository.findById(productId)
            .switchIfEmpty(
                    Mono.error(new ProductNotFoundException(productId))
            );
}
```

### Prohibited Reactive Anti-patterns

Never use these operations in application request flows:

```java
block();
blockFirst();
blockLast();
```

Avoid:

- Calling `subscribe()` manually inside service methods.
- Mixing blocking JDBC operations with reactive flows.
- Performing blocking database access on event-loop threads.
- Wrapping blocking operations in `Mono.just()` and assuming they become non-blocking.
- Returning `Mono<Mono<T>>` or `Flux<Mono<T>>` when flattening is required.

Use `flatMap` and other appropriate Reactor operators to compose publishers.

## 8. Database Access

Use Spring Data R2DBC for database persistence.

Rules:

- Use reactive repositories.
- Use R2DBC-compatible entities and mappings.
- Use parameterized queries.
- Avoid SQL injection vulnerabilities.
- Preserve franchise, branch and product relationships.
- Use database constraints where appropriate.
- Handle empty results and persistence errors explicitly.
- Avoid unnecessary database queries.

Do not use:

- JpaRepository.
- CrudRepository from the blocking Spring Data stack.
- EntityManager.
- JDBC templates.
- Hibernate ORM.
- Blocking database transactions.

## 9. Transactions

Use reactive transaction management.

- Use a ReactiveTransactionManager.
- Use reactive transaction operators or supported reactive `@Transactional` methods.
- Keep transaction boundaries explicit.
- Avoid mixing reactive and blocking transaction managers.
- Do not assume that ordinary imperative transaction handling works identically with reactive publishers.

Transactions must cover the complete asynchronous operation when data consistency requires it.

## 10. Exception Handling

- Use meaningful domain-specific exceptions.
- Avoid catching broad exceptions unnecessarily.
- Do not silently swallow errors.
- Preserve the original cause when wrapping technical exceptions.
- Centralize HTTP error handling where appropriate.

Example:

```java
public class FranchiseNotFoundException extends RuntimeException {

    public FranchiseNotFoundException(Long franchiseId) {
        super("Franchise not found: " + franchiseId);
    }
}
```

Use `@RestControllerAdvice` and `@ExceptionHandler` for consistent API error responses.

Do not expose stack traces, SQL details or internal implementation information to API clients.

## 11. DTOs and Validation

Use DTOs for incoming requests and outgoing responses.

Do not expose persistence entities directly as public API contracts.

Use Jakarta Bean Validation annotations when appropriate:

- `@NotNull`
- `@NotBlank`
- `@Size`
- `@Min`
- `@Max`
- `@Positive`

Validate incoming requests using `@Valid`.

Validation rules must reflect actual business requirements.

## 12. Logging

Use SLF4J.

Prefer parameterized logging:

```java
private static final Logger log =
        LoggerFactory.getLogger(FranchiseService.class);

log.info("Creating franchise with name: {}", name);
```

Never log:

- Passwords.
- Access tokens.
- Database credentials.
- Sensitive environment variables.

Avoid excessive logging inside reactive pipelines.

## 13. Configuration

- Keep environment-specific configuration outside application code.
- Use `application.properties` or `application.yml`.
- Prefer `@ConfigurationProperties` for grouped configuration.
- Use environment variables for credentials.
- Keep secrets out of Git.
- Provide safe example configuration when needed.

Never hardcode database passwords or production credentials.

## 14. Testing

Use:

- JUnit 5.
- Mockito.
- Reactor Test (`StepVerifier`).
- WebTestClient.
- Spring Boot integration testing where appropriate.

Test:

- Business rules.
- Reactive success and error signals.
- Empty publishers.
- Input validation.
- HTTP status codes.
- Database persistence.
- Franchise, branch and product relationships.
- Stock updates and highest-stock queries.

Avoid blocking operations in production code merely to simplify tests.

Use `StepVerifier` for testing reactive publishers.

## 15. Code Quality

- Keep methods focused and reasonably short.
- Avoid duplicated business logic.
- Avoid unnecessary abstractions.
- Remove unused imports and dead code.
- Avoid magic numbers.
- Prefer explicit types when they improve readability.
- Use appropriate access modifiers.
- Keep one public top-level type per file.
- Follow consistent formatting.
- Explain non-obvious business decisions.

## 16. Security

- Validate all external input.
- Use parameterized database queries.
- Never expose credentials.
- Avoid mass assignment of persistence entities.
- Return safe error messages.
- Apply least privilege to database access.
- Do not introduce authentication or authorization requirements without analyzing the test scope.

## Final Review Checklist

Before considering an implementation complete, verify:

1. The code follows Java conventions.
2. Dependencies use constructor injection.
3. Reactive operations remain non-blocking.
4. No JPA or JDBC has been introduced.
5. DTOs and validations are appropriate.
6. Database operations use R2DBC.
7. Exceptions are handled consistently.
8. Tests cover important behavior.
9. No secrets are exposed.
10. No unnecessary dependencies or abstractions were introduced.

Remember: maintainability and correctness are more important than clever or unnecessarily complex code.
