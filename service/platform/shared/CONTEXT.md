# shared module

Common building blocks used by services: `UseCase<C,R>`, `SelfValidating`, domain exceptions (`InvalidRequestException`, `ResourceNotFoundException`, `UnauthorizedAccessException`), `GlobalExceptionHandler`, `JwtAuthenticationFilter`/`JwtPrincipal`, `DomainEntity`.
Keep it free of service-specific logic.

## Test
`cd service && mvn test` (JwtAuthenticationFilterTest, GlobalExceptionHandlerTest).
