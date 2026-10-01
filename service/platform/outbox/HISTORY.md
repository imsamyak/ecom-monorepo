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

## 2026-10-01 events-envelope – sealed events, envelope payload, buildEvent (tests first, red)
- By: claude
- Changed: tests only so far: OutboxOfDomainEventTest (rewritten for nested events and the envelope), new aspect/OutboxAspectTest
- Why: owner design: sealed Product/Variant event interfaces with nested action records, {aggregate, action, data} envelope payload, use cases return only a DomainEvent
- Tests: written first; modules do not compile yet (red on purpose)
- Removed tests (replaced by the contract-module event tests): none
- Review: pending
