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
