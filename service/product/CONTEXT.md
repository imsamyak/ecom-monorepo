# product service

Spring Boot app (port 8081, H2). Hexagonal layout under `com.ecom.product`:
- `port/in/usecase/...` – use case interfaces and command/query/result DTOs
- `port/out/persistence/...` – outbound ports; `adapter/out/persistence` implements them with Spring Data
- `adapter/in/web/...` – controllers, request/response DTOs, MapStruct web mappers, `SecurityConfig`
- `service/...` – use case implementations (`@Service`); `domain/...` – entities, converters, exceptions

## Variant sku
A variant's properties map is stored in one `sku` column by `VariantSkuConverter` as sorted `key:value` pairs joined by commas (`color:red,size:M`). Keys and values are trimmed and blank ones dropped. The column has a DB check `LENGTH(TRIM(sku)) > 0`, so a variant with no usable properties is rejected by the database, and `(sku, product_id)` is unique so the same variant cannot be added twice to one product. Known limit: keys or values containing `,` are not supported (they would be split on read); a value may contain `:`.

## Outbox usage
Use cases that must emit events implement `OutboxUseCase` (e.g. `CreateProductUseCase`/`CreateProductService.buildOutbox`, aggregateType `Product`). The outbox module is auto-configured; this service needs an `OutboxPublisher` bean to actually ship events (none is defined yet; the relay idles without one).

## Product entity
The product has a non-null boolean `active` flag (true by default), updated via `activate()` and `deactivate()` methods in the entity.
Before persistence (insert and update), the product's `title` and `description` are stripped of leading and trailing whitespace. If the description is blank after trimming, it is stored as null.

## Product activation
The `SetProductActiveUseCase` allows sellers to activate or deactivate their products, exposed via `PATCH /products/{productId}/active`. Its result, `ProductResult`, carries the `active` state of the product.

## Test
`cd service && mvn test`. `OutboxIntegrationTest` covers outbox write, relay ordering/backoff, payload limit, async cleanup and sweep.
