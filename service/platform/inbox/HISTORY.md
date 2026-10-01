## 2026-10-01 T-008.1 tests - inbox store tests
- By: gemini (agy)
- Changed: service/platform/inbox/src/test/java/com/ecom/inbox/adapter/out/persistence/InboxJpaAdapterTest.java, service/platform/inbox/src/test/java/com/ecom/inbox/cleaner/InboxCleanerTest.java, service/platform/inbox/src/test/java/com/ecom/inbox/config/InboxAutoConfigurationTest.java
- Why: Tests for the inbox module storage port, JPA adapter, and TTL cleaner (Setup changed to construct components manually instead of relying on @Import)
- Depends on: T-005, T-006
- Rollback: git revert the commits found by git log --grep T-008.1 (newest first)
- Tests: mvn -f service/pom.xml test -> GREEN
- Review: pending

## 2026-10-01 T-008.1 impl - inbox module, store port, JPA adapter, TTL cleaner
- By: gemini (agy)
- Changed: service/platform/inbox/...
- Why: Implement the inbox deduplication storage and cleaner. Fixed duplicate bean exception by defining AutoConfigurationPackage at the root module level, and changed InboxCleaner to a non-Component instantiated by InboxAutoConfiguration.
- Depends on: T-005, T-006
- Rollback: git revert the commits found by git log --grep T-008.1 (newest first)
- Tests: mvn -f service/pom.xml test -> GREEN
- Review: pending
- Review notes by Claude: three fix rounds after review. (1) Claude asked for two missing tests: the inbox.dedupe.ttl property (default 7 days, override) and different aggregates being independent. (2) Bug found by Claude's full build: inboxRepository was registered twice; fixed by registering the module root package with @AutoConfigurationPackage(basePackages = com.ecom.inbox), like the outbox. (3) Adapter and cleaner are created only by the auto-configuration (no component scan).
- Test change (rule 8): agy changed its own new tests after writing them: InboxJpaAdapterTest uses separate isDuplicate and saveEvent calls (for the lock-then-dispatch flow of T-008.3); InboxJpaAdapterTest and InboxCleanerTest create the adapter and cleaner directly instead of @Import; InboxAutoConfigurationTest uses @SpringBootTest instead of ApplicationContextRunner. Assertions unchanged.
- Tests (Claude, full build): cd service; mvn test -> all green (contract 18, shared 31, outbox 26, inbox 7, product 78)
