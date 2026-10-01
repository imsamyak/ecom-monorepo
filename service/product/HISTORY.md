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
