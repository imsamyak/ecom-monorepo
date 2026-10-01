# product service

Spring Boot app (port 8081, H2). Hexagonal layout under `com.ecom.product`:
- `port/in/usecase/...` – use case interfaces and command/query/result DTOs
- `port/out/persistence/...` – outbound ports; `adapter/out/persistence` implements them with Spring Data
- `adapter/in/web/...` – controllers, request/response DTOs, MapStruct web mappers, `SecurityConfig`
- `service/...` – use case implementations (`@Service`); `domain/...` – entities, converters, exceptions

## Outbox usage
Use cases that must emit events implement `OutboxUseCase` (e.g. `CreateProductUseCase`/`CreateProductService.buildOutbox`, aggregateType `Product`). The outbox module is auto-configured; this service needs an `OutboxPublisher` bean to actually ship events (none is defined yet; the relay idles without one).

## Test
`cd service && mvn test`. `OutboxIntegrationTest` covers outbox write, relay ordering/backoff, payload limit, async cleanup and sweep.
