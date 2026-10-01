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
## 2026-10-01 Reduce token cost of the Claude + agy workflow
Idea: lower the cost per task. Findings at the time: delegating to agy saves Claude's code-writing (output) tokens, but adds prompt, review, monitoring, retry and commit overhead; for tiny mechanical tasks (renames, comment fixes) delegation can cost more than it saves. The biggest cost in a long session is the conversation itself, re-read on every reply.
Why parked: owner wants to do it later.
Options (recommendation in brackets):
- Start a fresh Claude session per task or per few tasks; AGENTS.md, docs/PROGRESS.md, TASKS.md and the HISTORY.md files already let a new session resume. (Do this first: biggest saving.)
- Give agy bigger chunks so the fixed overhead is spread over more code. (Yes, when chunks still stay stable alone.)
- Owner batches decisions in one message to save round trips. (Yes.)
- Rule change: Claude may make trivial non-logic edits itself (comments, renames, docs inside code files), agy keeps all logic and tests. (Owner decides; needs a change to rule 17.)
## 2026-10-01 Bloom filter in front of the inbox dedupe
Idea: an in-memory Bloom filter answers "definitely new" for most event ids so the database check is skipped.
Why parked: the agreed inbox keeps one row per aggregate with a direct key lookup, so there is little to save; a Bloom filter must be rebuilt after restarts and after rows expire. Revisit only if inbox lookups show up as a real cost.

## 2026-10-01 Dead-letter queue for poison messages
Idea: after N failed attempts the inbox moves a message aside (dead-letter store or topic) instead of letting the broker redeliver it forever.
Why parked: for now a failing listener rolls back and the broker redelivers (owner agreed). Open questions: N? where do dead letters go? how are they replayed after a fix?

## 2026-10-01 Giving a listener added later the past events (backfill)
Idea: events acknowledged while nobody listened are not replayed automatically. Options: (1) Kafka replay in a separate consumer group for the new listener (a compacted topic per aggregate keeps the latest full snapshot per product, which our UPDATE events are); (2) bootstrap from a snapshot (product API or bulk export) and then follow events; (3) store unhandled events in the inbox (rejected: database bloat).
Why parked: depends on the broker choice. Recommendation at the time: decide together with the broker task; with Kafka use option 1, otherwise option 2.

## 2026-10-01 DynamoDB inbox adapter with event history
Idea: an InboxStore adapter on DynamoDB: partition key aggregate id, sort key LAST for the last event and EVT#<seq> for the history; native TTL; history enables replay for new listeners.
Why parked: the inbox record must commit in the same transaction as the listener's work; with a relational service database and DynamoDB that is impossible (lost or duplicate events). It fits a service whose main database is DynamoDB (TransactWriteItems). Replay also needs an ordered sort key (event ids are random UUIDs).
