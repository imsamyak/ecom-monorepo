# product service

Spring Boot app (port 8081, H2). Hexagonal layout under `com.ecom.product` (rule 11 in AGENTS.md):
- `port/in/usecase/...` - use case interfaces and command/query/result DTOs
- `port/out/persistence/...` - outbound ports; `adapter/out/persistence` implements them with Spring Data
- `adapter/in/web/...` - controllers, request/response DTOs, MapStruct web mappers, `SecurityConfig`
- `service/...` - use case implementations (`@Service`); `domain/...` - entities, converters, exceptions

## Product entity
- Before every insert and update (`sanitize()` in `Product`), `title` and `description` are trimmed of leading and trailing whitespace; a description that is blank after trimming is stored as null.
- `status` is a non-null `ProductStatus` enum, `INACTIVE` by default (also via the builder). `activate()` / `deactivate()` on the entity change it. Ownership is checked with `Product.verifyOwnership(sellerId)` (throws `ProductNotOwnedException`).

## Set product active / inactive
- Use case `SetProductActiveUseCase.setProductActive(SetProductActiveCommand(productId, sellerId, active))` implemented by `SetProductActiveService`: load (`ProductNotFoundException`), verify ownership (nothing saved on failure), activate or deactivate, save, return `ProductResult` (which carries `status`).
- Endpoint `PATCH /products/{productId}/active` with body `{"active": true|false}`; the seller id comes from the JWT principal; returns `ProductResponse` (carries `status`). 404 if missing, 403 if another seller owns it (mapped by the shared `GlobalExceptionHandler`).
- Nothing else reads `status` yet (listing and get do not filter on it).

## Variant sku
A variant's properties map is stored in one `sku` column by `VariantSkuConverter` as sorted `key:value` pairs joined by commas (`color:red,size:M`). Keys and values are trimmed and blank ones dropped. The column has a DB check `LENGTH(TRIM(sku)) > 0`, so a variant with no usable properties is rejected by the database, and `(sku, product_id)` is unique so the same variant cannot be added twice to one product. Known limit: keys or values containing `,` are not supported (they would be split on read); a value may contain `:`.

## Events (outbox)
Every state-changing use case implements `OutboxAwareUseCase` (method `execute`) and returns only a `DomainEvent` from `buildEvent`, using the sealed events of the `contract` module:
- `CreateProductService` -> `Product.CREATE` (includes `status`), `UpdateProductService` -> `Product.UPDATE` (includes `status`), `DeleteProductService` -> `Product.DELETE` (from the command alone; its result is `Void`)
- `AddVariantService` -> `Variant.ADD`, `RemoveVariantService` -> `Variant.REMOVE` (the use case returns the removed variant's data so the event can carry it; the controller still answers 204)
Variant events use the owning product id as aggregate id. Query use cases (get, list) emit nothing. The app needs an `OutboxPublisher` bean to ship events (none is defined yet; the relay idles without one).
`DeleteProductService` and `RemoveVariantService` validate their command with an injected `Validator` (Hibernate Validator forbids redeclaring `@Valid` on an overridden `execute`).

## Tests
`cd service && mvn test` (product: 44 tests). Persistence tests use `@DataJpaTest` with their own H2 url; the controller test uses `@WebMvcTest` with `Authorization: Bearer <userId>:SELLER`; service tests are plain Mockito. `OutboxIntegrationTest` covers outbox write, relay ordering/backoff, payload limit, async cleanup and sweep; `ProductEventsOutboxIntegrationTest` and `ProductApiOutboxTest` cover the events each use case writes, over the use cases and over HTTP.
