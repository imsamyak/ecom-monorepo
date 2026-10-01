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
### T-003 Stale event names, UPDATE carries active, PATCH partial update   [in progress]
Branch: work. Two chunks, in order, each stable if merged alone.

#### T-003.1 Fix stale event names in comments and messages   [review]
Module(s): service/product, service/platform/outbox
Spec: comments in the five product services and RemoveVariantUseCase, and the javadoc and error message of Outbox.of, still name the old event records (Created, Updated, Deleted, Added, Removed). Rename them to CREATE, UPDATE, DELETE, ADD, REMOVE. Comment and message text only; no behavior change; no tests.
Acceptance: no old event record names left in service main code; `cd service && mvn test` passes.

#### T-003.2 Product UPDATE event carries the active flag   [in progress]
Module(s): service/platform/contract, service/product
Spec: the record `Product.UPDATE` gets a `boolean active` component as its LAST component: UPDATE(productId, sellerId, title, description, price, createdAt, updatedAt, active). `UpdateProductService.buildEvent` fills it from its result. CREATE and DELETE are unchanged; `Product` still permits exactly CREATE, UPDATE, DELETE. No API change.
Acceptance: contract test that UPDATE carries active; UseCaseEventsTest expects UPDATE with active; the integration update row has data.active; `cd service && mvn test` passes. Tests that pin the UPDATE fields change on purpose.

#### T-003.3 PATCH /products/{id} partial update replaces PUT and PATCH /active   [open]
Module(s): service/product
Spec (owner agreed 2026-10-01, JSON Merge Patch, RFC 7396):
- `PATCH /products/{productId}` with a JSON body holding any of `title`, `description`, `price`, `active`. A field that is not sent is left unchanged. `"description": null` clears the description. `title`, `price` and `active` must not be null: sending null for them is a 400.
- Absent vs null is told apart with `Optional` fields (no new library): the request DTO and `UpdateProductCommand` hold `Optional<String> title`, `Optional<String> description`, `Optional<Double> price`, `Optional<Boolean> active`; a Java null field means not sent, `Optional.empty()` means set to null, `Optional.of(v)` means set to v.
- Validation only for fields that are sent, with today's rules: title 3 to 100 characters, description at most 500, price above 0. An empty body `{}` (nothing sent) is a 400 (nothing to update).
- 404 for a missing product, 403 for another seller (nothing saved, no event). 200 with the updated product on success.
- Every successful patch emits one `Product.UPDATE` (full snapshot including active), also when the values did not change.
- `UpdateProductUseCase` becomes this partial update; `active` changes go through the entity's `activate()`/`deactivate()`.
- Removed: `PUT /products/{productId}`, `PATCH /products/{productId}/active`, `SetProductActiveUseCase`, `SetProductActiveService`, `SetProductActiveCommand`, `SetProductActiveRequest` and their web mapper methods. Removed tests (owner agreed): `SetProductActiveServiceTest`, `ProductActiveControllerTest`; their behavior is covered by the new PATCH tests.
- Hexagonal rule 11 applies: the controller calls only the use case.
Acceptance: web and integration tests for: each field alone, several fields together, absent fields unchanged, description set to null, null title/price/active is 400, invalid values are 400, empty body is 400, 404, 403, one UPDATE outbox row per successful patch with data.active, no row on failure; PUT and PATCH /active answer 405 or 404 (gone); `cd service && mvn test` passes.
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

