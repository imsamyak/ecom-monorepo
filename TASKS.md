# Task queue

How it works: the owner adds a task below (spec + acceptance). The Reviewer (Claude) splits it into chunks that are each stable if merged alone, writes the checklist in `docs/PROGRESS.md`, and runs the chunks through the Executor (`agy`) on ONE branch (AGENTS.md rules 7-16).

Statuses: `open` (not started) | `in progress` | `review` (committed and pushed, waiting for the owner to review the tests and merge) | `done` (merged and Review lines set to approved).

## Template
```
### T-000 <title>   [open]
Module(s): <paths>
Spec: <what to change and why>
Acceptance: <observable checks; tests that must exist or pass>
```

## Open tasks
### T-003 Stale event names, product status enum, PATCH partial update   [in progress]
Branch: work. Two chunks, in order, each stable if merged alone.

#### T-003.1 Fix stale event names in comments and messages   [review]
Module(s): service/product, service/platform/outbox
Spec: comments in the five product services and RemoveVariantUseCase, and the javadoc and error message of Outbox.of, still name the old event records (Created, Updated, Deleted, Added, Removed). Rename them to CREATE, UPDATE, DELETE, ADD, REMOVE. Comment and message text only; no behavior change; no tests.
Acceptance: no old event record names left in service main code; `cd service && mvn test` passes.

#### T-003.2 Product status enum replaces the active boolean   [review]
Module(s): service/platform/contract, service/product
Spec (owner agreed 2026-10-01):
- New enum `ProductStatus` with `ACTIVE` and `INACTIVE` in `service/product/.../domain/enums/ProductStatus.java`. (ARCHIVED is parked in SWGT.md.)
- `Product` replaces `boolean active` with `ProductStatus status`: not null, stored as text in a `status` column (`@Enumerated(EnumType.STRING)`), default `INACTIVE` (also with the builder). New products therefore start INACTIVE (this changes T-002's default on purpose).
- Domain methods stay: `activate()` sets ACTIVE, `deactivate()` sets INACTIVE.
- `ProductResult` carries `ProductStatus status` instead of `boolean active`; `ProductResponse` carries `String status` (for example "INACTIVE") instead of `boolean active`.
- Events: `Product.CREATE` and `Product.UPDATE` each get a `String status` component as their LAST component (UPDATE does not get a boolean). DELETE unchanged; Product still permits exactly CREATE, UPDATE, DELETE.
- The existing `PATCH /products/{id}/active` keeps working until T-003.3 removes it (it calls activate/deactivate); its response shows `status`.
Acceptance: a new product is INACTIVE; activate/deactivate switch it; the `status` column holds the text ACTIVE or INACTIVE (read back with a native query); responses carry `"status"`; the CREATE outbox row has data.status INACTIVE; the UPDATE row has data.status; `cd service && mvn test` passes. Existing tests about the active boolean or the old default change on purpose (say so in HISTORY).

#### T-003.3 PATCH /products/{id} partial update replaces PUT and PATCH /active   [review]
Module(s): service/product
Spec (owner agreed 2026-10-01, JSON Merge Patch, RFC 7396):
- `PATCH /products/{productId}` with a JSON body holding any of `title`, `description`, `price`, `status`. A field that is not sent is left unchanged. `"description": null` clears the description. `title`, `price` and `status` must not be null: sending null for them is a 400. An unknown status value is a 400.
- Absent vs null is told apart with `Optional` fields (no new library): the request DTO and `UpdateProductCommand` hold `Optional<String> title`, `Optional<String> description`, `Optional<Double> price`, `Optional<ProductStatus> status`; a Java null field means not sent, `Optional.empty()` means set to null, `Optional.of(v)` means set to v.
- Validation only for fields that are sent, with today's rules: title 3 to 100 characters, description at most 500, price above 0. An empty body `{}` (nothing sent) is a 400 (nothing to update).
- 404 for a missing product, 403 for another seller (nothing saved, no event). 200 with the updated product on success.
- Every successful patch emits one `Product.UPDATE` (full snapshot including status), also when the values did not change.
- `UpdateProductUseCase` becomes this partial update; status changes go through the entity's `activate()`/`deactivate()`.
- Removed: `PUT /products/{productId}`, `PATCH /products/{productId}/active`, `SetProductActiveUseCase`, `SetProductActiveService`, `SetProductActiveCommand`, `SetProductActiveRequest` and their web mapper methods. Removed tests (owner agreed): `SetProductActiveServiceTest`, `ProductActiveControllerTest`; their behavior is covered by the new PATCH tests.
- Hexagonal rule 11 applies: the controller calls only the use case.
Acceptance: web and integration tests for: each field alone, several fields together, absent fields unchanged, description set to null, null title/price/status is 400, unknown status is 400, invalid values are 400, empty body is 400, 404, 403, one UPDATE outbox row per successful patch with data.status, no row on failure; PUT and PATCH /active are gone (405 or 404); `cd service && mvn test` passes.
### T-004 Progress log for agy runs   [review]
Module(s): scripts/, .gitignore, docs/AGY.md (no Java code)
Spec (owner said go ahead 2026-10-01):
- One shared progress file `logs/agy-progress.log` in the repo root; `logs/` is added to `.gitignore`.
- Watcher in `scripts/agy-run.ps1` and `scripts/agy-impl-loop.ps1`: while agy runs, a background job appends one line every 30 seconds: time, elapsed minutes, the last step number and the last file written, both read from `~/.gemini/antigravity-cli/cli.log`, and in the loop the current round. The watcher stops when agy exits. Each script writes a start line and an end line (exit code, duration).
- Every agy prompt asks agy to append one plain-English line per step to `logs/agy-progress.log` (what it is doing and why, or what blocks it). Documented as a prompt rule in `docs/AGY.md`, together with how Claude reads the file to spot a stuck run.
- Rule 12 applies to the scripts (a comment before every step).
Acceptance: a short agy run produces start, periodic watcher and end lines in `logs/agy-progress.log`; the file is not tracked by git; existing script usage is unchanged. (Scripts have no test framework, so this is checked by a manual run, noted in HISTORY.)
### T-006 Event interfaces end in Event (fixes the Product/Variant name clash)   [review]
Module(s): service/platform/contract, service/platform/outbox, service/product
Spec (owner chose option 1 and said go ahead 2026-10-01):
- Rename the sealed event interfaces `com.ecom.contract.event.Product` -> `ProductEvent` and `Variant` -> `VariantEvent` (files renamed too). The nested records (CREATE, UPDATE, DELETE, ADD, REMOVE) and their components do not change.
- `Outbox.of` derives the aggregate type from the declaring interface name with the trailing `Event` removed (`ProductEvent` -> `Product`). An interface whose name does not end in `Event`, or is exactly `Event`, is rejected with `IllegalArgumentException` naming the class (like lambdas today).
- The outbox payload and row are unchanged for consumers: `"aggregate": "Product"` / `"Variant"`, same actions, same data.
- Product services use `ProductEvent.CREATE`, `VariantEvent.ADD` and so on (no fully qualified names, no clash with the entities).
Acceptance: contract tests use ProductEvent/VariantEvent; outbox tests: sample interfaces named `...Event` give the aggregate type without the suffix, a name without the suffix is rejected, a name exactly `Event` is rejected; product integration tests still see aggregate Product/Variant (unchanged); `cd service && mvn test` passes.
### T-007 agy chunk driver to cut Claude's cost (pilot)   [open]
Module(s): scripts/, docs/AGY.md, AGENTS.md rule 15 (no Java code)
Spec (owner: you can do it, pilot it, keep optimizing, pivot if needed; 2026-10-01):
- `scripts/agy-chunk.ps1 <task-id>`: builds the prompts from templates in `scripts/prompts/` (tests.txt, implement.txt) plus the task's section in TASKS.md, runs the tests-only step, then the implement-until-green loop, then writes `logs/agy-report.md`.
- Templates hold all standing prompt rules (no commands, no file deletion, read AGENTS.md etc., HISTORY format, rule 12), so Claude writes only the task spec.
- Guardrails: on a headless command abort retry once automatically; production files changed in the tests step or tests changed in the implement step -> stop and flag; no new agy step for 5 minutes or over the time limit -> stop and flag; files agy asked to delete are listed in the report.
- Report (about 20 lines): test files changed, production files changed, test counts before/after and green or not, tests changed during implement, fix rounds, duration, flags.
- Claude reviews once per chunk from the report plus the test files in full, then still makes two commits (tests, then impl).
Owner addition 2026-10-01 (agy has a very large context window; agy may be made smarter: take whole tasks, delegate further, test itself, reduce overhead):
- Pilot A: the chunk driver above (two steps, one Claude review from the report).
- Pilot B: ONE agy run per task: it reads the task, the rules and all relevant files once, writes tests, implements, runs the approved exact command mvn -f service/pom.xml test itself and fixes failures in its own conversation, may use its own subagents (pilot whether they return complete results), then self-checks its diff against the spec and the AGENTS.md checklist and writes logs/agy-report.md (files changed split into tests and production, test results, tests changed during implement, doubts). Claude reads the report and the test files in full, spot-checks the rest, and still commits tests first and implementation second from the report's file lists.
- Measure both against the T-003.3 baseline; keep the cheaper one that stays correct; pivot otherwise.Pilot: T-003.3 runs on the old process as the baseline; the next chunk after T-007 runs on the new one. Compare Claude tool calls, prompt size, diff lines read, retries and elapsed time; keep the new process only if it is clearly cheaper, else adjust or roll back. Record the numbers in docs/AGY.md.
Acceptance: one agy-chunk.ps1 run on a small real chunk produces both steps and a correct report; the pilot comparison is recorded.
### T-005 Every event is built by a MapStruct mapper   [open]
Module(s): service/product
Spec (owner: use mappers for all events or none; go ahead 2026-10-01):
- `ProductMapper` gets `ProductEvent.CREATE toCreateEvent(ProductResult)`, `ProductEvent.UPDATE toUpdateEvent(ProductResult)` (both map `id` to `productId`; the status enum becomes its name) and `ProductEvent.DELETE toDeleteEvent(DeleteProductCommand)` (fields copied by name).
- `VariantMapper` gets `VariantEvent.ADD toAddEvent(VariantResult)` and `VariantEvent.REMOVE toRemoveEvent(VariantResult)` (both map `id` to `variantId`).
- Every `buildEvent` in CreateProduct, UpdateProduct, DeleteProduct, AddVariant and RemoveVariant services becomes one mapper call; no event is built by hand any more. DeleteProductService gets the ProductMapper injected.
- No behavior change: events and outbox payloads stay exactly the same.
Acceptance: new unit tests for each of the five mapper methods (written first, using the generated mappers via `Mappers.getMapper`); the existing UseCaseEventsTest and integration tests pass unchanged except for service constructors that gain a mapper; `cd service && mvn test` passes.
Runs as pilot A of T-007 (the chunk driver), measured against the T-003.3 baseline.

## Done (owner approved; on branch work, reaches main when the owner merges work)
### T-001 Trim product title and description before saving   [done]
Module(s): service/product
Spec: `Product` must strip leading/trailing whitespace from `title` and `description` before it is persisted (insert and update), in the domain `sanitize()` step. A description that is blank after trimming is still stored as null. Chunk is self-contained: no API or schema change.
Acceptance: saving a product with title `"  Shoe  "` stores `"Shoe"`; description `"  nice  "` stores `"nice"`; description `"   "` stores null; updating a product re-applies the same trimming; `cd service && mvn test` passes.

### T-002 Seller can deactivate and reactivate a product   [done]
Split into three chunks, one branch, in order. Each chunk is stable if merged alone.

#### T-002.1 Product has an active flag (domain + persistence)   [done]
Module(s): service/product
Spec: add a non-null boolean `active` to `Product`, true by default (also when built with the builder). Add domain methods `deactivate()` and `activate()` that set it. Nothing else reads it yet, so no behavior elsewhere changes. Hexagonal rule 11: business rules live in the entity.
Acceptance: a new product is active; after `deactivate()` it is inactive and after `activate()` active again; the flag survives a save and reload; existing tests still pass. Tests: new `ProductActiveFlagTest` (plain unit test of the domain methods) and persistence round trip tests.

#### T-002.2 Use case to set a product active/inactive   [done]
Module(s): service/product
Spec: add use case `SetProductActiveUseCase` in `port/in/usecase/product` with command `SetProductActiveCommand(productId, sellerId, active)` (validated like the other commands), implemented by `SetProductActiveService` in `service/product`. It loads the product (`ProductNotFoundException` if missing), verifies ownership (`ProductNotOwnedException`, and nothing is saved), calls `activate()` or `deactivate()`, saves, and returns `ProductResult`. Add `boolean active` to `ProductResult` (mapped by MapStruct). No controller yet.
Acceptance: unit tests with mocked ports: missing product, wrong seller (nothing saved), deactivate, reactivate, result carries `active`. Existing tests (incl. `OutboxIntegrationTest`) still pass.

#### T-002.3 REST endpoint to set active (web)   [done]
Module(s): service/product
Spec: `PATCH /products/{productId}/active` with JSON body `{"active": true|false}` calls `SetProductActiveUseCase` using the seller id from the JWT principal, like the other endpoints. Add `SetProductActiveRequest`, web mapper methods, and `boolean active` to `ProductResponse`. Controller touches only the use case (rule 11).
Acceptance: web-layer tests (MockMvc) : 200 with updated `active`; 404 when the product does not exist; 403 when another seller owns it; existing tests pass.

