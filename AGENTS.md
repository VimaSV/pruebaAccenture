
# Accenture Backend Technical Test

## Project Objective

Develop a REST API to manage franchises, branches and products according to the Accenture technical test requirements.

## Technology Stack

- Java 21
- Spring Boot
- Spring WebFlux
- Spring Data R2DBC
- MySQL
- Maven
- Docker

## Required Skills

Consult the following skills when relevant:

- java-docs
- java-coding-standards
- java-springboot

Location: `.agents/skills/`

## Architecture Principles

- Prefer simple, maintainable architecture.
- Separate domain rules, application logic, persistence and HTTP concerns.
- Inspect the existing project before changing its structure.
- Avoid unnecessary abstractions.
- Do not introduce technologies outside the approved stack without discussing them first.

## Reactive Programming

- Use Mono and Flux consistently.
- Use Spring WebFlux for HTTP.
- Use Spring Data R2DBC for persistence.
- Never introduce JPA, Hibernate ORM or JDBC.
- Avoid block(), blockFirst() and blockLast() in application flows.
- Avoid manual subscribe() inside application services and controllers.
- Do not mix blocking and reactive database operations.

## Domain Requirements

The application manages:

- Franchises.
- Branches belonging to franchises.
- Products belonging to branches.
- Product stock.

Required operations:

1. Create franchises.
2. Create branches for franchises.
3. Add products to branches.
4. Delete products.
5. Update product stock.
6. Find the product with the highest stock per branch for a franchise.
7. List products including their branch.

Do not change the functional requirements without approval.

## Code Quality

- Use constructor injection.
- Validate external input.
- Use DTOs for API requests and responses.
- Handle errors consistently.
- Use meaningful names.
- Avoid duplicated logic.
- Keep methods focused.
- Write automated tests.
- Document important business rules.

## Security

- Never hardcode credentials.
- Never expose secrets.
- Never read or print secret files.
- Never commit passwords, tokens or private keys.
- Use environment variables for sensitive configuration.
- Use parameterized database queries.
- Do not expose internal exceptions to API clients.

## Git Rules

- Do not create commits automatically.
- Do not push changes.
- Do not reset or rewrite Git history.
- Do not delete branches.
- Do not discard user changes.
- The user controls Git through SourceTree.

## Agent Responsibilities

### Architect
Analyzes requirements and proposes architecture. Does not modify files.

### Developer
Implements explicitly approved requirements.

### QA
Tests functionality and reports defects. Does not modify application code.

### Security Reviewer
Audits security and reports findings. Does not modify files.

### Final Auditor
Independently evaluates the final submission. Does not modify files.

## Collaboration Rules

- Inspect the actual repository before making assumptions.
- Explain significant technical decisions.
- Do not implement unapproved architectural changes.
- Do not introduce unnecessary dependencies.
- Report commands actually executed.
- Report failures honestly.
- Preserve existing user changes.
- Explain important implementations so the project owner understands the code.

## Completion Criteria

A task is not complete merely because code was generated.

Verify the implementation with appropriate tests and Maven commands.

Report remaining issues and unverified behavior.
