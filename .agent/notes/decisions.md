# Decision log (append-only)

Every design discussion with the owner is recorded here when it happens: the conclusion, why, what was rejected, and what is still open. Newest entries at the bottom. Tasks in TASKS.md link here for the reasoning; history files record what was built.

## 2026-10-01 Outbox
- Outbox rows are written by an AOP aspect in the same transaction as the use case; the relay publishes strictly in createdAt order, one row per transaction, exponential backoff on failure (in memory, resets on restart), one relay instance only (outbox.relay.enabled).
- Processed rows are deleted right after publishing by an async single-thread cleaner; an hourly sweep removes leftovers. Rejected: keeping processed rows for dedupe (consumers dedupe on the event id, rows do not help).
- Per-aggregate ordering rejected for now: a systemic failure blocks everything anyway; only poison rows would benefit (parked in SWGT.md, with a stuck-row alert).
- Assessment of the AOP approach: correct and cheap; weak spots are hidden behavior (method must be named execute), events built from DTOs, one event per execution (list support parked in SWGT.md), generic-interface validation friction. Owner: returning data from delete/remove use cases is fine.

## 2026-10-01 Events (contract module)
- Module name: contract (was confess). DomainEvent has one method, String aggregateId(); each record converts its own id.
- Sealed event interfaces end in Event (ProductEvent, VariantEvent) to avoid clashing with entities; the aggregate type is the interface name without the suffix; nested record names are the action, present tense upper case: CREATE, UPDATE, DELETE, ADD, REMOVE. permits clause stays implicit (same file).
- Payload is the envelope {aggregate, action, data}. Use cases return only a DomainEvent from buildEvent; the aspect does the rest. OutboxUseCase renamed OutboxAwareUseCase.
- Variant events use the owning product id as aggregate id (ordering with the product).
- UPDATE always carries the full snapshot after the change (not changed fields only).
- All five events are built by MapStruct mappers (all or none, owner).
- Rejected: aggregate type with a colon (Product:CREATE) because of routing, ordering across topics and parsing; @OutboxEvent annotation (stringly typed).

## 2026-10-01 Product
- ProductStatus enum ACTIVE, INACTIVE; default INACTIVE; stored as text. ARCHIVED parked in SWGT.md with its open questions.
- One PATCH /products/{id} with JSON Merge Patch (Optional fields) replaces PUT and PATCH /active; title, price, status cannot be null (400); description null clears it; empty body 400; every successful patch emits one UPDATE.
- Variant sku: DB check that it is never empty; converter bug (String.join) fixed.

## 2026-10-01 Inbox
- Consumer side of the outbox, in module service/platform/inbox, auto-configured, broker-agnostic entry point InboxReceiver.receive(message); real broker adapters later.
- Dedupe: one row per aggregate (type, id) holding the last processed event id, overwritten; TTL 7 days by an hourly cleaner (inbox.dedupe.ttl); same transaction as the listener's work. Relies on three rules: one outbox relay, a publisher that never reorders or repeats, the inbox acknowledging one message at a time (so a duplicate is always the immediate repeat).
- Dispatch with plain synchronous Spring @EventListener on the record type (or the sealed parent); not @TransactionalEventListener.
- No listener: record and acknowledge. Unknown aggregate/action: log, record, acknowledge. Failing listener: roll back, broker redelivers (dead-letter queue parked). First user: test-only listeners.
- Storage behind an InboxStore port (a DynamoDB adapter could come later for a DynamoDB-based service).
- Rejected: Bloom filter alone (false positives lose events, lost on restart); in-memory only with idempotent listeners (owner chose DB for safety); DynamoDB for a relational service (no shared transaction); storing every event id (bloat). Parked: Bloom pre-check, dead-letter queue, backfill for listeners added later, DynamoDB adapter.

## 2026-10-01 JWT (T-009, design in progress)
- JWT lives only in the shared module, auto-configured; services declare only their access rules and keep @AuthenticationPrincipal JwtPrincipal.
- Issuer and signing algorithm not decided, but the same for all services; verification configured by properties (security.jwt.algorithm, secret from an environment variable or public key / JWK set URI, optional issuer, roles claim name). Spring Security resource server replaces the hand-written filter.
- UserRole becomes a shared enum in contract (values only added, never renamed; tolerate unknown values; only true cross-service concepts in contract, ProductStatus stays in product).
- Open: role values, claim names, token lifetime, 401 behavior.

## 2026-10-01 Inventory service (design in progress, not started)
- Listens to product events (through the inbox) and creates a stock row per variant: available 0, reserved 0.
- Reservations are entirely synchronous API calls (owner: events for commit feel like losing integrity): reserve (available -n, reserved +n), commit (reserved -n), release (reserved -n, available +n). Idempotent by checkoutId. Reserve is one conditional update (available >= n) so overselling is impossible; multi-item all or nothing, locks in variant-id order.
- Expiry stays as a safety net for abandoned checkouts; expiry and commit both require status RESERVED so exactly one wins; a commit after expiry gets 409 before payment is captured. Checkout's own timeout must be shorter than the reservation TTL.
- Inventory may emit its own events (stock changed, out of stock) for others; reservations do not depend on events.
- Recommended database: PostgreSQL (atomic conditional updates, transactions) with read replicas and an optional short cache for high reads; DynamoDB only at very large scale.
- Open: database choice, expiry time (60 min suggested, checkout timeout 30), restock API, behavior when a variant with reservations is removed, order of work (inbox, then JWT, then inventory).

## 2026-10-01 Way of working
- Owner reviews tests only and merges work himself; Claude never opens PRs; one work branch named work; push only when the tip builds.
- Discuss first; implement only after the owner says go ahead. Parked ideas go to SWGT.md.
- Small commits (one task per commit), one HISTORY entry per commit with Depends on and Rollback.
- agy writes all code; Claude plans, reviews, runs the build and git, edits docs, and is accountable for everything agy produces (rule 17).
- Executor model: gemini-3.1-pro-high (strongest; correctness first). Only agy's Gemini models, not its Claude models.
- agy never runs builds or tests: root cause of early stops and aborts (AGY.md section 11). Chunk driver (pilot A) is the standard; pilot B (agy builds itself) was worse.
- Low-memory kills: restart after 5, 10, 15, 20, 25 seconds, then ask the owner.
- Keep optimizing cost by small measured pilots; pivot if not better.
- Everything is recorded in files so recovery never depends on the chat: discussions here, tasks in TASKS.md, state in PROGRESS.md, history in HISTORY.md files, runs in reports.
- AI files move into .agent/ (owner go ahead): docs/LEARNING.md stays in docs/, module context files go to .agent/modules/, settings in .agent/config.json.

## 2026-10-01 agy strategy experiment and latest owner decisions
- Strategy experiment on T-008.2: the two-step chunk driver (S1) produced a correct result; one combined prompt (S2) aborted on a grep command. S1 stays the standard. Details in docs/AGY.md section 8.
- Next driver improvement: continue the same agy conversation after a command abort instead of starting over.
- JWT (T-009): owner gave go ahead; open details use the recommendations (roles SELLER, BUYER, ADMIN; claims sub and roles; 401 for a missing or invalid token).
- Inventory: waits for a design discussion and the owner's go ahead.
- Executor model stays gemini-3.1-pro-high (only agy's Gemini models).

## 2026-10-01 Cost: two chats
- Owner: 17 percent of the weekly limit used in one day; reduce cost.
- Biggest cost is the long chat re-read on every turn. Plan: two chats on the same files. Design chat (Opus, this one, compacted with /compact): discussions, decisions, task specs; edits only docs, commits only while the execution chat is idle. Execution chat (new session, /model sonnet): runs tasks through agy, reviews, builds, commits, pushes; only it commits code and runs agy. Go aheads are written into TASKS.md so both chats see them.
- Execution chat reports once per task, keeps output short (Maven totals only, reports not full diffs). Memory kills are handled silently with the restart policy unless all 5 retries fail (owner runs the machine under heavy use).

## 2026-10-01 One git writer at a time: the lock (rule 24)
- Owner asked whether only the design chat could commit, using a flag the execution chat sets. Rejected: every commit would run on Opus (more cost), the owner would have to relay each chunk, and the design chat would have to trust or redo the build.
- Chosen (owner: implement b): both chats may commit, never at the same time. Shared file .agent/lock (gitignored) with one line: who, date, time, task. Check before any git change; if held by the other chat, report waiting for lock to the owner; release after push. Execution chat holds it for a whole chunk; design chat only for doc commits. A lock older than 2 hours is not removed by a chat; the owner decides.
