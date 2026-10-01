# contract history (append-only; this module was called confess until 2026-10-01)

## 2026-10-01 confess-module – DomainEvent contract
- By: claude
- Changed: new module: DomainEvent (String aggregateId()), pom, DomainEventTest
- Why: events decide their own aggregate id; the record name is the aggregate type (owner design: simple, id returned as String by the record)
- Tests: `cd service && mvn test` -> all pass (5 new here)
- New tech: docs/LEARNING.md "Domain events as records"
- Review: pending

## 2026-10-01 rename-contract – module renamed confess -> contract
- By: claude
- Changed: directory platform/confess -> platform/contract, artifactId, package com.ecom.confess -> com.ecom.contract
- Why: owner asked for the module to be called contract
- Tests: `cd service && mvn test` -> all pass (no behavior change)
- Review: pending

## 2026-10-01 events-envelope – sealed events and envelope
- By: claude
- Changed: new EventEnvelope record; new event/Product (Created, Updated, Deleted) and event/Variant (Added, Removed) sealed interfaces; tests: EventEnvelopeTest, ProductEventsTest, VariantEventsTest (written first, commit cb8c9f6)
- Why: owner design: the interface name is the aggregate type, the nested record name is the action, one aggregate id rule per aggregate (product id; variant events also use the product id), payload is {aggregate, action, data}
- Tests: `cd service && mvn test` -> all pass (13 new here)
- New tech: docs/LEARNING.md "Domain events as sealed interfaces and records"
- Review: pending

## 2026-10-01 present-tense-actions - event actions named CREATE/UPDATE/DELETE and ADD/REMOVE (tests first, red)
- By: claude
- Changed: tests only: ProductEventsTest, VariantEventsTest
- Why: owner asked for present-tense upper-case action names; the action string in the outbox payload changes accordingly
- Tests: written first; module does not compile yet (red on purpose)
- Review: pending

## 2026-10-01 merge-into-work - event records renamed to present tense
- By: gemini (agy), reviewed by claude
- Changed: event/Product records Created/Updated/Deleted -> CREATE/UPDATE/DELETE; event/Variant records Added/Removed -> ADD/REMOVE (implements the red tests of 9559d74); CONTEXT.md updated by claude
- Why: owner asked for present-tense upper-case action names; the action string in the outbox payload changes accordingly
- Tests: cd service; mvn test -> all pass
- Review: pending

## 2026-10-01 T-003.2 tests - ProductStatus enum replaces active
- By: gemini (agy)
- Changed: ProductEventsTest
- Why: update events to carry status instead of active boolean
- Depends on: T-002.1, T-003.1
- Rollback: git revert the commits found by git log --grep T-003.2 (newest first) and also revert T-003.3 which depends on it
- Tests: written first, red on purpose
- Review: pending

## 2026-10-01 T-003.2 impl - ProductStatus enum replaces active
- By: gemini (agy)
- Changed: service/platform/contract/src/main/java/com/ecom/contract/event/Product.java, CONTEXT.md
- Why: replace active boolean with ProductStatus enum and verify new default is INACTIVE
- Depends on: T-002.1 and T-003.1
- Rollback: git revert the commits found by git log --grep T-003.2 (newest first) and also revert T-003.3 which depends on it
- Tests: cd service; mvn test -> all pass (product 62), green on the first round, no test changed
- Review: pending
- New tech: docs/LEARNING.md JPA section (@Enumerated)

## 2026-10-01 T-006 tests - event interfaces end in Event
- By: gemini (agy)
- Changed: service/platform/contract/src/test/java/com/ecom/contract/event/ProductEventsTest.java, service/platform/contract/src/test/java/com/ecom/contract/event/VariantEventsTest.java
- Why: test renaming event interfaces to ProductEvent and VariantEvent to fix name clash. Existing tests change on purpose because the event interfaces are renamed.
- Depends on: T-003.2
- Rollback: git revert the commits found by git log --grep T-006 (newest first)
- Tests: written first, red on purpose
- Review: pending

## 2026-10-01 T-006 impl - event interfaces end in Event
- By: gemini (agy)
- Changed: Product.java renamed to ProductEvent.java, Variant.java renamed to VariantEvent.java, CONTEXT.md updated
- Why: Rename event interfaces to avoid clash with entities
- Depends on: T-003.2
- Rollback: git revert the commits found by git log --grep T-006 (newest first)
- Tests: cd service; mvn test -> all pass (product 62), green after 1 fix round, no test changed. The old Product.java and Variant.java were removed by Claude with git rm (agy cannot delete files); the build stays green without them
- Review: pending

## 2026-10-01 T-008.2 tests - EventCatalog and envelope decoding
- By: gemini (agy)
- Changed: service/platform/contract/src/test/java/com/ecom/contract/EventCatalogTest.java
- Why: test EventCatalog resolves product and variant events to their specific record types
- Depends on: none
- Rollback: git revert the commits found by git log --grep T-008.2 (newest first)
- Tests: pending
- Review: pending

## 2026-10-01 T-008.2 impl - EventCatalog and envelope decoding
- By: gemini (agy)
- Changed: service/platform/contract/src/main/java/com/ecom/contract/EventCatalog.java
- Why: Implement EventCatalog to resolve Event records from aggregate type and action
- Depends on: T-006
- Rollback: git revert the commits found by git log --grep T-008.2 (newest first)
- Tests: pending
- Review: pending
- Claude review: full build green (contract 25, shared 31, outbox 31, inbox 7, product 78); chosen over experiment S2, which aborted; agy's edit to docs/PROGRESS.md was undone

## 2026-10-01 T-009.1 tests - UserRole enum in contract
- By: gemini (agy)
- Changed: service/platform/contract/src/test/java/com/ecom/contract/enums/UserRoleTest.java
- Why: test UserRole mapping from claim
- Depends on: none
- Rollback: git revert the commits found by git log --grep T-009.1 (newest first)
- Tests: pending
- Review: pending

## 2026-10-01 T-009.1 impl - UserRole enum in contract
- By: gemini (agy)
- Changed: service/platform/contract/src/main/java/com/ecom/contract/enums/UserRole.java, service/platform/contract/CONTEXT.md
- Why: Create UserRole enum to map claim values case-insensitively and add constraints to CONTEXT.md
- Depends on: none
- Rollback: git revert the commits found by git log --grep T-009.1 (newest first)
- Tests: pending
- Review: pending
- locale-independent upper-casing after Claude's review
- Claude review (T-009.1): locale-independent upper-casing (Locale.ROOT) added after review; full build green.
