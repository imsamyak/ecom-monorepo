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
(none - ask the owner)

## Done (merged to main 2026-10-01)
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

