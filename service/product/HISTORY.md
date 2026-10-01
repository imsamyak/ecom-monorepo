# product history (append-only)

(no entries yet; outbox-related test changes are logged in service/platform/outbox/HISTORY.md)

## 2026-10-01 variant-sku-check – sku must never be empty (tests first)
- By: claude
- Changed: tests: VariantSkuConverterTest, VariantPersistenceTest (commit 1, written first, 14 of 19 failing); production: VariantSkuConverter (fix), Variant (@Check on sku) (commit 2)
- Why: the converter always wrote an empty sku (String.join misuse), so variants lost their properties, a product could hold only one variant, and reads crashed; nothing in the DB forbade an empty sku
- Tests: `cd service && mvn -pl product -am test` -> all pass (19 new in this task, 6 existing integration, outbox and shared suites green)
- New tech: docs/LEARNING.md "JPA / Hibernate features used" (AttributeConverter, @Check) and the String.join gotcha
- Review: approved (owner said "merge all branches to main", 2026-10-01)

## 2026-10-01 confess-module – product publishes a Product domain event
- By: claude
- Changed: new domain/event/Product record; CreateProductService.buildOutbox uses Outbox.of; pom (depends on confess); tests: ProductEventTest, CreateProductServiceOutboxTest
- Why: emit the outbox row through a DomainEvent record named Product, keeping the same payload fields
- Tests: `cd service && mvn test` -> all pass (5 new here; OutboxIntegrationTest still passes unchanged)
- Review: pending

## 2026-10-01 variant-event – Variant domain event record
- By: claude
- Changed: new domain/event/Variant record (DomainEvent); tests: VariantEventTest (written first, failed to compile, then passed)
- Why: owner asked for product and variant event records implementing DomainEvent; the variant uses the owning product id as aggregate id so its events stay ordered with that product's events
- Tests: `cd service && mvn test` -> all pass (4 new)
- Not done on purpose: AddVariantService/RemoveVariantService do not emit it yet (would add outbox rows for variant changes; needs the owner's decision)
- Review: pending

## 2026-10-01 rename-contract – module renamed confess -> contract
- By: claude
- Changed: pom dependency and imports now use contract / com.ecom.contract
- Why: owner asked for the module to be called contract
- Tests: `cd service && mvn test` -> all pass (no behavior change)
- Review: pending

## 2026-10-01 events-envelope – every state-changing use case emits a sealed event
- By: claude
- Changed: Create/Update/Delete product and Add/Remove variant use cases now extend OutboxUseCase, method renamed to execute, each implements buildEvent (Product.Created/Updated/Deleted, Variant.Added/Removed); RemoveVariant now returns the removed variant's data; Delete returns Void; controllers call execute; removed the old domain/event Product and Variant records; Delete and RemoveVariant services validate their command with an injected Validator
- Why: owner asked to emit events from all use cases. Validation moved into the services because Hibernate Validator forbids redeclaring @Valid on an override (HV000151); behavior is unchanged (ConstraintViolationException for an invalid command), and tests pin it
- Tests: `cd service && mvn test` -> all pass (UseCaseEventsTest 7, ProductEventsOutboxIntegrationTest 9, ProductApiOutboxTest 2; OutboxIntegrationTest unchanged and passing)
- Removed tests, replaced by the contract-module event tests: ProductEventTest, VariantEventTest, CreateProductServiceOutboxTest
- Behavior changes: adding/removing a variant and updating/deleting a product now write outbox rows; payloads are envelopes
- Review: pending

## 2026-10-01 outbox-aware-rename - rename OutboxUseCase to OutboxAwareUseCase
- By: claude
- Changed: the five state-changing use case interfaces extend OutboxAwareUseCase; CONTEXT.md
- Why: owner asked for the name OutboxAwareUseCase (no behavior change)
- Tests: cd service; mvn test -> all pass (116, no test changed except the renamed type)
- Review: pending

## 2026-10-01 present-tense-actions - event actions named CREATE/UPDATE/DELETE and ADD/REMOVE (tests first, red)
- By: claude
- Changed: tests only: UseCaseEventsTest, ProductEventsOutboxIntegrationTest, ProductApiOutboxTest
- Why: owner asked for present-tense upper-case action names; the action string in the outbox payload changes accordingly
- Tests: written first; module does not compile yet (red on purpose)
- Review: pending
## 2026-10-01 T-001 tests – trim product title and description (tests first)
- By: gemini (agy) wrote the tests, claude reviewed them
- Changed: tests: ProductPersistenceTest (new); no production code yet
- Why: tests first for T-001; 3 of 4 fail because Product.sanitize() does not trim yet, the 4th (blank description becomes null) protects existing behavior
- Tests: `cd service && mvn -pl product -am test -Dtest=ProductPersistenceTest` -> 3 failures (expected <Shoe> but was <  Shoe  >), as intended
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 T-001 – Trim product title and description before saving
- By: gemini (agy)
- Changed: src/main/java/com/ecom/product/domain/entity/Product.java, CONTEXT.md
- Why: T-001 implementation: title and description are trimmed before insert and update, and a description blank after trimming becomes null
- Tests: `cd service && mvn test` -> all pass (product 29 incl. 4 new, shared 31, outbox 5); run by claude, agy had no shell
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 T-002.1 tests – product active flag (tests first)
- By: gemini (agy) wrote the tests, claude reviewed them and asked for one more (reactivate persisted)
- Changed: tests: ProductActiveFlagTest, ProductActiveFlagPersistenceTest (new); no production code yet
- Why: tests first for T-002.1; they do not compile until Product has isActive/activate/deactivate
- Tests: `mvn test -pl product -am` -> testCompile fails on the missing methods, as intended
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 T-002.1 Product has an active flag
- By: gemini (agy)
- Changed: service/product/src/main/java/com/ecom/product/domain/entity/Product.java, service/product/CONTEXT.md
- Why: Added the active flag and its domain methods as per T-002.1 spec.
- Tests: `cd service && mvn test` -> all pass (product 35 tests)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 T-002.2 tests – set product active use case (tests first)
- By: gemini (agy) wrote the tests, claude reviewed them and sent back two fixes (tautological assertions on a stubbed result, mapper only tested for the default)
- Changed: tests: SetProductActiveServiceTest (new); no production code yet
- Why: tests first for T-002.2; they do not compile until the use case, command, service and ProductResult.active exist
- Tests: not run yet (compile fails by design)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 T-002.2 Set product active use case
- By: gemini (agy)
- Changed: port/in/usecase/product/dto/result/ProductResult.java, port/in/usecase/product/dto/command/SetProductActiveCommand.java, port/in/usecase/product/SetProductActiveUseCase.java, service/product/SetProductActiveService.java, CONTEXT.md
- Why: Implemented SetProductActiveUseCase to allow sellers to activate or deactivate products, updated ProductResult with active flag
- Tests: `cd service && mvn test` -> all pass (product 41 tests); run by the driver loop (claude side)
- Test change (rule 8): the two mapper tests in SetProductActiveServiceTest were changed by agy to add `.sellerId(UUID.randomUUID())` to the Product builder, because Product.sellerId is @NonNull and the builder threw an NPE. The tests were wrong (missed in review); assertions are unchanged.
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 T-002.3 tests – PATCH /products/{id}/active (tests first)
- By: gemini (agy) wrote the tests, claude reviewed them and sent back one fix (wrong use case method name)
- Changed: tests: ProductActiveControllerTest (new, @WebMvcTest); no production code yet
- Why: tests first for T-002.3; they do not compile until SetProductActiveRequest, the web mapper methods, the endpoint and ProductResponse.active exist
- Tests: not run yet (compile fails by design)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 T-002.3 PATCH endpoint to set product active
- By: gemini (agy)
- Changed: ProductController, ProductWebMapper, ProductResponse, SetProductActiveRequest, CONTEXT.md
- Why: T-002.3 implementation
- Tests: `cd service && mvn test` -> all pass (product 44 tests incl. 3 new web tests); run by the driver loop (claude side)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 merge-into-work - fold claude/gemini-cli-setup-check-8bda3d into work
- By: gemini (agy), reviewed by claude
- Changed: resolved conflicts in AGENTS.md, HISTORY.md, service/product/CONTEXT.md, service/product/HISTORY.md; event records renamed to CREATE/UPDATE/DELETE and ADD/REMOVE with their uses in the five services (implements the red tests of 9559d74); SetProductActiveUseCase/Service method setProductActive renamed to execute, ProductController updated
- Tests changed by agy (owner please review): UseCaseEventsTest gets the new ProductResult active argument (needed after the merge); SetProductActiveServiceTest and ProductActiveControllerTest call execute instead of setProductActive (only the method name, assertions unchanged). The setProductActive -> execute rename was not requested; agy did it for consistency with the other use cases.
- Tests: cd service; mvn test -> all pass (product 62 incl. the merged active-flag tests)
- Follow-up: comments in five product services, RemoveVariantUseCase and the Outbox.java javadoc/message still mention the old names Created/Updated/Deleted/Added/Removed (code files, left for agy)
- Review: pending

## 2026-10-01 T-003.1 - fix stale event names in comments and messages
- By: gemini (agy)
- Changed: CreateProductService, UpdateProductService, DeleteProductService, AddVariantService, RemoveVariantService, RemoveVariantUseCase
- Why: fix stale event names in comments
- Tests: cd service; mvn test -> all pass (product 62), comment and message text only
- Review: pending

## 2026-10-01 T-003.2 tests - ProductStatus enum replaces active
- By: gemini (agy)
- Changed: ProductActiveFlagTest, ProductActiveFlagPersistenceTest, SetProductActiveServiceTest, ProductActiveControllerTest, UseCaseEventsTest, ProductEventsOutboxIntegrationTest
- Why: replace active boolean with ProductStatus enum and verify new default is INACTIVE
- Depends on: T-002.1, T-003.1
- Rollback: git revert the commits found by git log --grep T-003.2 (newest first) and also revert T-003.3 which depends on it
- Tests: written first, red on purpose. Existing tests changed on purpose to replace active boolean with status enum and check INACTIVE as default.
- Review: pending

## 2026-10-01 T-003.2 impl - ProductStatus enum replaces active
- By: gemini (agy)
- Changed: service/product/src/main/java/com/ecom/product/domain/enums/ProductStatus.java, Product.java, ProductResult.java, ProductResponse.java, CreateProductService.java, UpdateProductService.java, CONTEXT.md
- Why: replace active boolean with ProductStatus enum and verify new default is INACTIVE
- Depends on: T-002.1 and T-003.1
- Rollback: git revert the commits found by git log --grep T-003.2 (newest first) and also revert T-003.3 which depends on it
- Tests: cd service; mvn test -> all pass (product 62), green on the first round, no test changed
- Review: pending
- New tech: docs/LEARNING.md JPA section (@Enumerated)

## 2026-10-01 T-006 tests - event interfaces end in Event
- By: gemini (agy)
- Changed: service/product/src/test/java/com/ecom/product/service/UseCaseEventsTest.java
- Why: use new event names ProductEvent and VariantEvent. Existing tests change on purpose because the event interfaces are renamed.
- Depends on: T-003.2
- Rollback: git revert the commits found by git log --grep T-006 (newest first)
- Tests: written first, red on purpose
- Review: pending