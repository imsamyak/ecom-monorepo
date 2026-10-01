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
