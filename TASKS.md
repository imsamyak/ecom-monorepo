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
### T-003 Product activation events and stale event names   [in progress]
Branch: work. Two chunks, in order, each stable if merged alone.

#### T-003.1 Fix stale event names in comments and messages   [review]
Module(s): service/product, service/platform/outbox
Spec: comments in the five product services and RemoveVariantUseCase, and the javadoc and error message of Outbox.of, still name the old event records (Created, Updated, Deleted, Added, Removed). Rename them to CREATE, UPDATE, DELETE, ADD, REMOVE. Comment and message text only; no behavior change; no tests.
Acceptance: no old event record names left in service main code; `cd service && mvn test` passes.

#### T-003.2 Setting a product active or inactive emits a Product UPDATE event   [open]
Module(s): service/platform/contract, service/product
Spec (owner: keep it under the UPDATE event, no new actions): the `Product.UPDATE` event record gets a `boolean active` component, so it is a full snapshot including the active flag. `UpdateProductService` fills it from its result. `SetProductActiveUseCase` extends `OutboxAwareUseCase<SetProductActiveCommand, ProductResult>` and its `buildEvent` returns a `Product.UPDATE` built from the result (with the new active value). Its command stays validated: because Hibernate Validator forbids redeclaring `@Valid` on an overridden `execute` (HV000151), the service validates the command with an injected `Validator`, like DeleteProductService. `CREATE` is unchanged. HTTP behavior is unchanged.
Acceptance: `Product` still permits exactly CREATE, UPDATE, DELETE; UPDATE carries `active`; unit tests of buildEvent for set-active (true and false) and for update; integration: PATCH /products/{id}/active writes one Product:UPDATE outbox row keyed by the product id whose data.active is the new value; a normal update's UPDATE row also has data.active; a 404 or 403 writes no row; an invalid command still throws ConstraintViolationException; `cd service && mvn test` passes. Existing tests that pin the UPDATE fields or constructor change on purpose (say so in HISTORY).
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

