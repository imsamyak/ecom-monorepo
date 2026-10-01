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
