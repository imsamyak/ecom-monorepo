# product history (append-only)

(no entries yet; outbox-related test changes are logged in service/platform/outbox/HISTORY.md)

## 2026-10-01 variant-sku-check – sku must never be empty (tests first)
- By: claude
- Changed: tests: VariantSkuConverterTest, VariantPersistenceTest (commit 1, written first, 14 of 19 failing); production: VariantSkuConverter (fix), Variant (@Check on sku) (commit 2)
- Why: the converter always wrote an empty sku (String.join misuse), so variants lost their properties, a product could hold only one variant, and reads crashed; nothing in the DB forbade an empty sku
- Tests: `cd service && mvn -pl product -am test` -> all pass (19 new in this task, 6 existing integration, outbox and shared suites green)
- New tech: docs/LEARNING.md "JPA / Hibernate features used" (AttributeConverter, @Check) and the String.join gotcha
- Review: approved (owner said "merge all branches to main", 2026-10-01)

## 2026-10-01 T-001 tests – trim product title and description (tests first)
- By: gemini (agy) wrote the tests, claude reviewed them
- Changed: tests: ProductPersistenceTest (new); no production code yet
- Why: tests first for T-001; 3 of 4 fail because Product.sanitize() does not trim yet, the 4th (blank description becomes null) protects existing behavior
- Tests: `cd service && mvn -pl product -am test -Dtest=ProductPersistenceTest` -> 3 failures (expected <Shoe> but was <  Shoe  >), as intended
- Review: pending

## 2026-10-01 T-001 – Trim product title and description before saving
- By: gemini (agy)
- Changed: src/main/java/com/ecom/product/domain/entity/Product.java, CONTEXT.md
- Why: T-001 implementation: title and description are trimmed before insert and update, and a description blank after trimming becomes null
- Tests: `cd service && mvn test` -> all pass (product 29 incl. 4 new, shared 31, outbox 5); run by claude, agy had no shell
- Review: pending
