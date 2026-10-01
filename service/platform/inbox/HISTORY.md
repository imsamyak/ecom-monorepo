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
