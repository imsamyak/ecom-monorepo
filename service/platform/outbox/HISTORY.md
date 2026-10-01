# outbox history (append-only)

## 2026-10-01 immediate-delete – delete processed rows right after publish
- By: claude
- Changed: OutboxRelay, OutboxCleaner, OutboxRepository, OutboxAutoConfiguration, OutboxIntegrationTest
- Why: no need to keep processed rows; consumers dedupe on the event id, not on outbox rows
- Tests: `cd service && mvn test` -> 15 passed, 0 failed
- Review: approved (owner said "merge all branches to main", 2026-10-01)


## 2026-10-01 confess-module – Outbox.of(DomainEvent)
- By: claude
- Changed: Outbox (static factory `of`), pom (depends on confess); tests: OutboxOfDomainEventTest
- Why: build an outbox row from a DomainEvent: type = record name, id = the string the record returns
- Tests: `cd service && mvn test` -> all pass (12 new here)
- Review: pending

## 2026-10-01 rename-contract – module renamed confess -> contract
- By: claude
- Changed: pom dependency and imports now use contract / com.ecom.contract
- Why: owner asked for the module to be called contract
- Tests: `cd service && mvn test` -> all pass (no behavior change)
- Review: pending

## 2026-10-01 events-envelope – use cases return only a DomainEvent
- By: claude
- Changed: OutboxUseCase.buildOutbox(...) -> DomainEvent buildEvent(...); Outbox.of now reads the aggregate type from the enclosing interface, the action from the record name, and wraps the event in an EventEnvelope; OutboxAspect builds the row from the event
- Why: owner asked that use cases only return the event record and the framework does the rest
- Tests: `cd service && mvn test` -> all pass; OutboxOfDomainEventTest rewritten (10 tests) and new OutboxAspectTest (9 tests). Adjusted one of my own new aspect tests: JDK proxies wrap undeclared checked exceptions, so it looks through UndeclaredThrowableException
- Behavior change: every outbox payload is now the envelope {aggregate, action, data} instead of the bare object
- Review: pending

## 2026-10-01 outbox-aware-rename - rename OutboxUseCase to OutboxAwareUseCase
- By: claude
- Changed: OutboxUseCase.java renamed to OutboxAwareUseCase.java; OutboxAspect pointcut, Outbox, OutboxAutoConfiguration, OutboxAspectTest and CONTEXT.md use the new name
- Why: owner asked for the name OutboxAwareUseCase (no behavior change)
- Tests: cd service; mvn test -> all pass (116, no test changed except the renamed type)
- Review: pending

## 2026-10-01 T-003.1 - fix stale event names in comments and messages
- By: gemini (agy)
- Changed: Outbox.java
- Why: fix stale event name in javadoc and error message text
- Tests: cd service; mvn test -> all pass (product 62), comment and message text only
- Review: pending

## 2026-10-01 T-006 tests - event interfaces end in Event
- By: gemini (agy)
- Changed: service/platform/outbox/src/test/java/com/ecom/outbox/OutboxOfDomainEventTest.java, service/platform/outbox/src/test/java/com/ecom/outbox/aspect/OutboxAspectTest.java
- Why: test outbox parsing aggregate type from interfaces ending in Event. Existing tests change on purpose because the event interfaces are renamed.
- Depends on: T-003.2
- Rollback: git revert the commits found by git log --grep T-006 (newest first)
- Tests: written first, red on purpose
- Review: pending

## 2026-10-01 T-006 impl - event interfaces end in Event
- By: gemini (agy)
- Changed: Outbox.java, CONTEXT.md updated
- Why: Derive aggregate type by stripping Event suffix, reject invalid names
- Depends on: T-003.2
- Rollback: git revert the commits found by git log --grep T-006 (newest first)
- Tests: cd service; mvn test -> all pass (product 62), green after 1 fix round, no test changed. The old Product.java and Variant.java were removed by Claude with git rm (agy cannot delete files); the build stays green without them
- Review: pending
