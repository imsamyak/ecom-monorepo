# Learning guide: the tech used in this repo

Every section: what it is, where it lives here, how it works, gotchas. New tech gets a new section (AGENTS.md rule 13).

## Hexagonal architecture (ports and adapters)
- **What:** the business core knows nothing about HTTP, SQL or message brokers. It talks through *ports* (interfaces); *adapters* plug real technology into them.
- **Here:** `service/product`. `port/in/usecase` = what callers can do; `port/out` = what the core needs; `adapter/in/web` = REST controllers; `adapter/out/persistence` = JPA; `service/` = use case logic.
- **Why:** you can swap the database or add a Kafka entry point without touching business rules, and test the core with plain mocks.
- **Gotcha:** dependencies must point inward only. A controller calling a repository directly breaks the model. Entities here carry JPA annotations (a pragmatic shortcut); they still must not cross the web boundary.

## AOP (Aspect-Oriented Programming)
- **What:** run cross-cutting code (transactions, logging, outbox) around methods without editing them. An *aspect* holds the code, a *pointcut* says which methods, *advice* (`@Around`) wraps the call.
- **Here:** `OutboxAspect` in the outbox module, pointcut `execution(* com.ecom.outbox.OutboxUseCase+.execute(..))` = the `execute` method of every class implementing `OutboxUseCase`. It runs the use case, calls `buildOutbox`, and saves the event row in the same transaction.
- **How:** Spring wraps matching beans in a *proxy*. Callers hit the proxy, which runs the advice, which calls the real method through `joinPoint.proceed()`.
- **Gotchas:** (1) a method calling another method of the *same* class bypasses the proxy, so the advice and `@Transactional`/`@Async` do not run; (2) only Spring beans are proxied; (3) pointcut typos fail silently (the aspect just never fires), so test that it matches.

## Transactional outbox pattern
- **Problem:** saving to the DB and publishing to a broker can't be one atomic step. Crash in between and you lose the event or publish a ghost.
- **Idea:** write the event into an `outbox_events` table **in the same DB transaction** as the business change. A separate *relay* later reads the table and publishes. DB commit or rollback covers both.
- **Here:** `OutboxAspect` writes rows; `OutboxRelay` publishes them. Delivery is **at-least-once**: a crash after publishing but before commit re-sends the row, so consumers must dedupe on `OutboxMessage.id` (the row id, stable across retries).
- **Strict ordering:** the relay publishes the oldest row first and stops on the first failure, so later events never overtake it. Cost: one bad row blocks everything behind it.

## Spring transactions
- `@Transactional` = declarative (proxy-based, subject to the self-invocation gotcha). `TransactionTemplate` = programmatic, explicit, no proxy needed. The aspect and relay use `TransactionTemplate` so the transaction boundary is visible in the code.
- **Per-event transactions:** the relay uses one transaction per published row, so a crash can duplicate at most the one event in flight, not a batch.
- **Rollback-only gotcha:** if code inside a `@Transactional` method catches an exception thrown by another transactional call, the transaction is already marked rollback-only and commit throws `UnexpectedRollbackException`. That's why `OutboxCleaner.cleanup()` is not `@Transactional` itself; the repository method is.

## Spring Boot auto-configuration
- **What:** adding a dependency wires beans automatically. A library lists its config class in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`; Boot loads it at startup.
- **Here:** `OutboxAutoConfiguration` creates the aspect, relay, cleaner and executor. `@ConditionalOnProperty(outbox.relay.enabled)` turns relay and cleaner on or off. `@AutoConfigurationPackage` *adds* the outbox package to entity/repository scanning without replacing the app's own scan.
- **Gotcha:** ordering annotations (`before`/`after`) matter; the outbox config must run before JPA auto-config so its entity is found.

## @Scheduled and @EnableScheduling
- Runs a method on a timer. `fixedDelay` waits N ms **after the previous run finishes** (no overlap); `fixedRate` fires every N ms regardless; `cron` uses a calendar expression.
- **Here:** the relay polls with `fixedDelay` (1 s default); the cleaner sweeps hourly with `cron`.
- **Gotcha:** by default all `@Scheduled` methods share **one thread**; a slow job delays the others. In a multi-instance deployment every instance runs the schedule, hence `outbox.relay.enabled` so only one does.

## @Async, executors and thread pools
- **What:** `@Async` runs a method on another thread (via a proxy, so same self-invocation gotcha), returning to the caller immediately.
- **Here:** after each publish the relay calls `OutboxCleaner.cleanup()`, which deletes processed rows on a dedicated single-thread executor (`outboxCleanupExecutor`), so a slow or failing delete never touches publishing.
- **Details:** core=max=1 thread plus queue of 1 and `DiscardPolicy` means extra requests are dropped; an `AtomicBoolean` guard skips a cleanup if one is already running. That is safe because every cleanup deletes *all* processed rows, and the next publish triggers another.

## JPA / Hibernate features used
- **Pessimistic lock** (`@Lock(PESSIMISTIC_WRITE)` = `SELECT ... FOR UPDATE`): the relay locks the head row so two relays can't publish the same one. No `SKIP LOCKED` on purpose: a second relay waits, preserving order.
- **`Persistable<UUID>`:** with app-assigned ids, Spring Data's `save` would first SELECT to decide insert vs update. Implementing `isNew()` skips that.
- **`@Version`** (in `DomainEntity`): optimistic locking; concurrent updates to the same row fail instead of silently overwriting.
- **Lifecycle callbacks** (`@PrePersist`/`@PreUpdate`): set `createdAt`/`updatedAt`, sanitize blank descriptions.
- **`@Check`:** database CHECK constraints (price > 0, title length) as a last line of defence beyond Java validation.
- **`AttributeConverter`:** `VariantSkuConverter` stores a variant's properties map as one string column. Hibernate calls it on every write and read, so a bug there corrupts data silently.
- **Java gotcha, `String.join`:** its signature is `join(delimiter, elements...)`. Passing one already-joined string makes it the *delimiter* with nothing to join, which returns `""` and compiles without warning. That was the converter bug. Defence in depth: the converter is unit tested, and the `sku` column has a DB `@Check` so an empty value can never be stored even if code regresses.
- **`@Modifying @Query`:** bulk update/delete in one statement, needs a transaction.

## Domain events as sealed interfaces and records
- **Java records:** a short way to declare an immutable data class (`record Created(UUID productId, ...)`). The compiler generates the constructor, accessors, `equals`, `hashCode` and `toString`. Jackson serializes a record's components.
- **Sealed interfaces (Java 17):** `sealed interface Product extends DomainEvent` limits which types may implement it. When the implementations are nested records in the same file the `permits` list is inferred. The set of actions (`Created`, `Updated`, `Deleted`) is therefore closed and `Product.class.getPermittedSubclasses()` lists it. (An exhaustive `switch` over a sealed type needs Java 21; on 17 use `instanceof` patterns.)
- **Where the names come from (reflection):** `event.getClass().getSimpleName()` is the action (`Created`) and `event.getClass().getDeclaringClass().getSimpleName()` is the aggregate type (`Product`). Lambdas, anonymous classes and top-level records have no declaring class, so `Outbox.of` rejects them.
- **Default interface methods:** `aggregateId()` is written once in `Product` (the product id as a string) and inherited by every action. `Variant` events deliberately return the owning product id, so they share the product's partition key and stay ordered with its events.
- **Aggregate id = partition key:** events with the same key land on the same partition and are consumed in order. Choose the entity whose events must stay ordered relative to each other.
- **Envelope:** every outbox payload is `{aggregate, action, data}` (`EventEnvelope`). Consumers parse that first, then `data` by (aggregate, action).
- **Use cases return only an event:** `OutboxUseCase.buildEvent(command, result)` describes what happened; the `OutboxAspect` does the rest (type, action, envelope, JSON, size limit, saving in the same transaction).
- **Gotchas:** a record named like an entity (`Product`) needs nested-type imports or full names where both are in scope; Hibernate Validator rejects `@Valid` redeclared on an overriding method (HV000151), so the delete and remove services validate with an injected `Validator`; JDK proxies wrap undeclared checked exceptions in `UndeclaredThrowableException`.

## Exponential backoff
After each failure wait twice as long (1 s, 2 s, 4 s ... capped at 5 min) so a down broker is not hammered. State lives in memory in `RetryBackoff`; a restart retries from 1 s again.

## MapStruct
Generates mapping code between DTOs and entities at compile time (no reflection). `@Mapper(componentModel = "spring")` makes the generated class a Spring bean. Used in `service/*/mapper` (entity <-> result) and `adapter/in/web/mapper` (request/response <-> command/result). Gotcha: needs the `lombok-mapstruct-binding` annotation processor ordering (already set in the pom).

## Bean Validation
Annotations like `@NotBlank`, `@Size`, `@Positive` on commands and entities. `@Valid` triggers validation of an argument/field; `@Validated` on a Spring bean enables method-level validation through a proxy. Gotcha: validation on a method parameter only runs if the interface or class declares `@Valid`/constraints on it; `CreateProductUseCase.execute` has none, so invalid commands are caught later by entity validation.

## Spring Security (as used here)
A *filter chain* inspects each request. `JwtAuthenticationFilter` reads `Authorization: Bearer <uuid>:<ROLE>` (a **simulated** token, not real JWT) and puts a `JwtPrincipal` in the `SecurityContext`. `SecurityConfig` requires role `SELLER` for `/products/**`, makes sessions stateless and disables CSRF (fine for token APIs). Controllers receive the user with `@AuthenticationPrincipal`.

## Testing toolbox
- **JUnit 5** test runner; **Mockito** fakes collaborators; **AssertJ/JUnit assertions** check results.
- **MockMvc** calls controllers without a real server; `@SpringBootTest` boots the whole app; `@DataJpaTest` boots only JPA with an in-memory H2 DB.
- **`ApplicationContextRunner`** tests auto-configuration cheaply (which beans exist under which properties).
- **Awaitility** waits for asynchronous effects (e.g. the async delete) instead of `Thread.sleep`.
- **Characterization test:** a test that records what code *currently* does so later changes can't silently break it. **TDD:** write the failing test first, then the code.
- **`@Disabled("BUG: ...")`:** parks a test that exposes a known bug, keeping the build green while the bug stays visible.

## Maven multi-module and git workflow
Parent `service/pom.xml` lists modules (`platform/shared`, `platform/outbox`, `product`); `mvn test` in `service/` builds all in dependency order. `git config core.hooksPath .githooks` makes git run `.githooks/pre-commit`, which calls `scripts/check-context-sync.sh`.

## Not used yet, but relevant
- **Spring Boot Actuator:** a starter (`spring-boot-starter-actuator`) that exposes health and metrics endpoints (`/actuator/health`, `/actuator/metrics`) and supports custom metrics via Micrometer. Natural next step here: a gauge for "age of oldest PENDING outbox row" to detect a stuck relay, which is the visibility gap noted in `service/platform/outbox/CONTEXT.md`. Not added yet.
- **CDC (Debezium) / sequence column:** the standard fixes for the outbox ordering limitation.
