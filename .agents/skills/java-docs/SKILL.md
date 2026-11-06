
---
name: java-docs
description: "Javadoc documentation standards for Java 21 and Spring Boot backend applications. Use when documenting or reviewing Java classes, interfaces, methods, APIs and domain models."
---

# Java Documentation Standards

## Objective

Write clear, useful and maintainable documentation for Java applications.

Documentation must explain the purpose, behavior and relevant constraints of the code without repeating what the implementation already makes obvious.

## Javadoc Rules

- Document public and protected classes, interfaces, records and methods when they form part of the application's API or expose meaningful behavior.
- Document package-private and private members when their behavior is complex or requires additional explanation.
- Avoid unnecessary Javadoc on simple getters, setters and self-explanatory private methods.
- Use `/** ... */` for Javadoc comments.
- Use the first sentence as a concise summary ending with a period.
- Write documentation in English unless the project explicitly establishes another language.

## Tags

Use the appropriate tags:

- `@param` to describe method parameters.
- `@return` to describe returned values.
- `@throws` to describe exceptions callers may need to handle.
- `@see` to reference related types or methods.
- `@since` only when the project maintains meaningful version history.
- `{@inheritDoc}` when inheriting documentation from an interface or superclass without changing its contract.
- `{@code}` for inline code, identifiers and examples.
- `<pre>{@code ... }</pre>` for multiline examples.
- `@param <T>` for generic type parameters.

Do not add `@author` or `@version` automatically. Use them only when explicitly required by project conventions.

## Spring Boot Documentation

Document public components when their responsibilities are not obvious:

- REST controllers and their externally visible behavior.
- Application services and important business rules.
- Domain models and business invariants.
- Repository interfaces when custom query behavior needs explanation.
- Configuration classes and non-obvious configuration decisions.
- Custom exceptions and error contracts.

## REST API Documentation

For externally exposed endpoints, document when appropriate:

- Endpoint purpose.
- Required parameters.
- Request and response expectations.
- Validation constraints.
- Relevant HTTP status codes.
- Possible error conditions.

Do not duplicate information already clearly defined in DTO validation annotations or API documentation tools.

## Reactive Programming Documentation

When documenting reactive methods:

- Explain whether the publisher emits zero, one or multiple elements when relevant.
- Describe meaningful asynchronous behavior.
- Document error signals and important side effects.
- Clarify transaction boundaries when they are not obvious.

Do not describe Mono or Flux as if they were ordinary synchronous return values.

## Examples

```java
/**
 * Creates a franchise with the supplied name.
 *
 * @param request franchise creation data
 * @return a publisher containing the created franchise
 */
public Mono<FranchiseResponse> createFranchise(
        CreateFranchiseRequest request) {
    // Implementation
}
```

```java
/**
 * Finds all products belonging to a branch.
 *
 * @param branchId identifier of the branch
 * @return a publisher containing the matching products
 */
public Flux<ProductResponse> findProductsByBranch(Long branchId) {
    // Implementation
}
```

## Quality Requirements

- Keep documentation accurate and synchronized with the implementation.
- Do not document behavior that the code does not guarantee.
- Do not use Javadoc to explain obvious syntax.
- Avoid excessive comments.
- Explain business rules and non-obvious decisions.
- Never include passwords, tokens or sensitive configuration values in documentation.

## Review Checklist

Before completing documentation, verify:

1. Public API documentation is meaningful.
2. Parameters and return values are described accurately.
3. Exceptions reflect actual behavior.
4. Reactive return types are explained correctly.
5. Examples compile or accurately represent the API.
6. No unnecessary or misleading documentation was introduced.
