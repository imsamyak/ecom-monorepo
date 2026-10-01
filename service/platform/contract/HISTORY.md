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

## 2026-10-01 events-envelope – sealed events, envelope payload, buildEvent (tests first, red)
- By: claude
- Changed: tests only so far: EventEnvelopeTest, event/ProductEventsTest, event/VariantEventsTest
- Why: owner design: sealed Product/Variant event interfaces with nested action records, {aggregate, action, data} envelope payload, use cases return only a DomainEvent
- Tests: written first; modules do not compile yet (red on purpose)
- Removed tests (replaced by the contract-module event tests): none
- Review: pending
