# shared history (append-only)

(no entries yet)

## 2026-10-01 tests-baseline-shared – regression tests for the shared module
- By: claude
- Changed: tests only: DomainEntityTest, SelfValidatingTest, SharedExceptionsTest, GlobalExceptionHandlerConstraintTest, JwtAuthenticationFilterEdgeCasesTest (no production code touched)
- Why: protect existing behavior before further changes (TDD policy, AGENTS.md rules 7-10)
- Tests: `cd service && mvn -pl platform/shared test` -> 31 passed, 0 failed (20 new, 11 existing)
- Disabled bug tests: none
- Review: pending owner approval of the tests
