# outbox module

Transactional outbox, plug-and-play: adding the dependency auto-configures it (`OutboxAutoConfiguration` via `AutoConfiguration.imports`).

## How it works
- A use case implements `OutboxAwareUseCase<C,R>` (`execute` + `buildEvent(command, result)`). `OutboxAspect` wraps `execute` in a transaction and saves an `OutboxEntity` row in the same transaction as the business write. Rollback drops both. `buildEvent` returning null means no event.
- Payload is stored as JSON (the envelope); length limit `outbox.max-payload-length` (default 1,000,000 chars), over-limit fails the use case.
- `OutboxRelay` (`@Scheduled`, `outbox.relay.fixed-delay-ms`, default 1s) publishes PENDING rows **strictly in `createdAt` order, one row per transaction**. First failure stops the run so order is kept; retry with in-memory exponential backoff (`initial-backoff-ms` 1000, doubling, cap `max-backoff-ms` 300000). Backoff state resets on restart. A permanently failing head row blocks the outbox (watch the error log).
- Delivery is **at-least-once**. `OutboxMessage.id` (the row id) is stable across retries and is the consumer dedupe key. Apps provide an `OutboxPublisher` bean.
- After each publish commits, `OutboxCleaner.cleanup()` runs `@Async` on the single-thread `outboxCleanupExecutor` and bulk-deletes all PROCESSED rows (`deleteByStatus`). Overlapping calls are skipped; failures are only logged. `OutboxCleaner.purge()` (`outbox.cleanup.cron`, hourly) sweeps leftovers. There is no retention window; processed rows are not kept.
- Relay and cleaner run only when `outbox.relay.enabled` is true (default). Run on **one instance only**.

## Building events
A use case implements `OutboxAwareUseCase<C,R>`: `execute` plus `DomainEvent buildEvent(command, result)` (return null to emit nothing). It only describes what happened. `OutboxAspect` then calls `Outbox.of(event)`: aggregate type = the interface the event record is nested in (`event.getClass().getDeclaringClass()`), action = the record name, aggregate id = `event.aggregateId()`, payload = `EventEnvelope(aggregate, action, event)` serialized to JSON. It throws `IllegalArgumentException` for a null/blank id, anonymous classes, lambdas and top-level records. The size limit applies to the whole envelope.

## Invariants / known limits
- Order can break if concurrent transactions commit out of `createdAt` order (would need a sequence column or CDC).
- No metric/alert for a stuck row; only error logs.

## Test
`cd service && mvn test` – unit tests here, integration tests in `service/product` (`OutboxIntegrationTest`).
