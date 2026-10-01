# outbox history (append-only)

## 2026-10-01 immediate-delete – delete processed rows right after publish
- By: claude
- Changed: OutboxRelay, OutboxCleaner, OutboxRepository, OutboxAutoConfiguration, OutboxIntegrationTest
- Why: no need to keep processed rows; consumers dedupe on the event id, not on outbox rows
- Tests: `cd service && mvn test` -> 15 passed, 0 failed
- Review: approved (owner said "merge all branches to main", 2026-10-01)


## 2026-10-01 confess-module – Outbox.of(DomainEvent) (tests only so far)
- By: claude
- Changed: tests only: OutboxOfDomainEventTest
- Why: build an outbox row from a DomainEvent: type = record name, id = chosen attribute as string
- Tests: not runnable yet (red on purpose, implementation not written)
- Review: pending; waiting for the owner's explicit go ahead
