# SWGT - see when got time

Ideas the owner parked for later (AGENTS.md rule 19). These are NOT tasks: nothing here is built until the owner picks an entry, it is discussed again, moved to TASKS.md, and the owner says go ahead.

Entry format: date, idea, why parked, open questions with Claude's recommendation at the time.

## 2026-10-01 ARCHIVED product status
Idea: a third `ProductStatus` value `ARCHIVED` ("hide but keep"), next to `ACTIVE` and `INACTIVE`.
Why parked: owner wants to think about it; T-003 ships only ACTIVE and INACTIVE.
Open questions (recommendation in brackets):
- Can an archived product come back? (No: ARCHIVED is final.)
- Can an archived product still be edited (title, price, ...)? (No: any PATCH on it is rejected.)
- Response for a forbidden change, e.g. ARCHIVED -> ACTIVE or editing an archived product? (409 Conflict with a clear message; needs a shared exception such as InvalidStateException mapped to 409 in GlobalExceptionHandler.)
- How is a product archived? (Same PATCH /products/{id} with "status": "ARCHIVED".)
- What happens to DELETE /products/{id}? (Keep it as a real delete emitting DELETE; archive is the soft alternative.)
Proposed transitions at the time: INACTIVE <-> ACTIVE; INACTIVE -> ARCHIVED; ACTIVE -> ARCHIVED; ARCHIVED -> nothing.
## 2026-10-01 Per-aggregate outbox ordering
Idea: instead of one strict global order, a failing outbox row would block only later events of the same aggregate id; other products keep publishing.
Why parked: owner's point: a systemic failure (broker down) blocks everything anyway, so the benefit is only for row-specific "poison" failures; it needs per-aggregate locking and backoff and loses cross-aggregate order. Strict global order stays for now.
Open questions: is head-of-line blocking by one bad row a real problem in practice? (Watch for it with the stuck-row alert below before building this.)

## 2026-10-01 Alert for a stuck outbox row
Idea: expose a metric such as "age of the oldest PENDING outbox row" (Spring Boot Actuator + Micrometer gauge) and alert when it grows, so a blocked relay is noticed without reading logs.
Why parked: not needed until events are really published (no OutboxPublisher exists yet).
Open questions: which monitoring system reads the metric? What age threshold means stuck? (Recommendation: start with a gauge and a log warning after a few minutes.)
## 2026-10-01 More than one event per use case
Idea: let a use case emit several events from one execution, for example `buildEvent` returning a list of DomainEvent (or a new `buildEvents` method). The aspect would save one outbox row per event, all in the same transaction, in list order.
Why parked: owner agrees it is useful but not needed yet; today every use case emits at most one event.
Open questions (recommendation in brackets):
- Change `buildEvent` to return a list, or add a separate `buildEvents` with a default that wraps `buildEvent`? (Add `buildEvents` with a default, so existing use cases do not change.)
- Should an empty list mean emit nothing, like null today? (Yes.)
- Order of the rows for one execution? (Keep list order; they share the transaction, and createdAt ties are broken by insertion order.)
Note from the same review: returning data from delete/remove use cases so the event can carry it is fine (owner); no change needed.