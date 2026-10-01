# product history (append-only)

(no entries yet; outbox-related test changes are logged in service/platform/outbox/HISTORY.md)

## 2026-10-01 variant-sku-check – sku must never be empty (tests first)
- By: claude
- Changed: tests only so far: VariantSkuConverterTest, VariantPersistenceTest
- Why: the converter always wrote an empty sku (String.join misuse) and nothing in the DB forbade it
- Tests: `mvn -pl product -am test -Dtest='VariantSkuConverterTest,VariantPersistenceTest'` -> 14 of 19 failing, as intended before the fix
- Review: pending owner approval of the tests
