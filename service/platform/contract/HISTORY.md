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
