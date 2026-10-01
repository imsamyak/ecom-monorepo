# Root history (cross-cutting changes: tooling, workflow, repo layout)

## 2026-10-01 setup-ai-workflow â€“ shared context files and sync check
- By: claude
- Changed: AGENTS.md, CLAUDE.md, GEMINI.md, TASKS.md, per-module CONTEXT.md/HISTORY.md, scripts/check-context-sync.sh, .githooks/pre-commit
- Why: executor/reviewer workflow with context kept in sync with code
- Tests: scripts/check-context-sync.sh self-check (see below) -> passed
- Review: approved (owner said "merge all branches to main", 2026-10-01)

## 2026-10-01 tdd-and-teaching-rules â€“ TDD policy, hexagonal rule, comment rule, learning guide
- By: claude
- Changed: AGENTS.md (rules 7-13), docs/LEARNING.md
- Why: owner policy: tests-first with test-only review and merge gate; keep hexagonal structure; comment every step; teach new tech
- Tests: n/a (docs only)
- New tech: docs/LEARNING.md (all sections written from what the repo already uses)
- Review: approved (owner said "merge all branches to main", 2026-10-01)

## 2026-10-01 merge-means-approved â€“ owner approval convention
- By: claude
- Changed: AGENTS.md rule 9; Review lines of earlier entries set to approved
- Why: owner clarified that saying "merge" means the tests are approved and merging is authorized
- Tests: n/a (docs only)
- Review: approved (owner said "approved", 2026-10-01)

## 2026-10-01 confess-module â€“ confess module registered
- By: claude
- Changed: service/pom.xml (module platform/confess); AGENTS.md project list; docs/LEARNING.md
- Why: new DomainEvent contract module, see service/platform/confess/HISTORY.md
- Tests: `cd service && mvn test` -> all pass
- Review: pending

## 2026-10-01 rename-contract â€“ module renamed confess -> contract
- By: claude
- Changed: service/pom.xml module name; AGENTS.md and docs/LEARNING.md references
- Why: owner asked for the module to be called contract
- Tests: `cd service && mvn test` -> all pass (no behavior change)
- Review: pending

## 2026-10-01 events-envelope â€“ sealed domain events used by all state-changing use cases
- By: claude
- Changed: docs/LEARNING.md; see the contract, outbox and product HISTORY.md files
- Why: owner design: sealed events per aggregate, {aggregate, action, data} envelope, use cases return only an event
- Tests: `cd service && mvn test` -> all pass (116 tests)
- Review: pending

## 2026-10-01 outbox-aware-rename - rename OutboxUseCase to OutboxAwareUseCase
- By: claude
- Changed: docs/LEARNING.md uses the new name; code changes are in the outbox and product HISTORY.md
- Why: owner asked for the name OutboxAwareUseCase (no behavior change)
- Tests: cd service; mvn test -> all pass (116, no test changed except the renamed type)
- Review: pending

## 2026-10-01 one-work-branch - owner's branch and PR policy
- By: claude
- Changed: AGENTS.md rules 3 and 9
- Why: owner: always keep one branch for work, the owner reviews and merges it, Claude opens no PRs
- Tests: n/a (docs only)
- Review: pending
## 2026-10-01 agy-notes – record Antigravity CLI setup and options
- By: claude
- Changed: docs/AGY.md
- Why: save the researched agy (Antigravity CLI) install path, options, models and executor loop so it is not re-researched
- Tests: n/a (docs only)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 executor-startup-rules – make the executor read context and follow the chunk flow
- By: claude
- Changed: AGENTS.md (rules 14-15), docs/AGY.md (prompt preamble, permissions)
- Why: owner wants agy to read and maintain CONTEXT/HISTORY and follow the agreed one-branch, tests-first, no-retry-limit flow
- Tests: n/a (docs only)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 T-002 - seller can deactivate and reactivate a product (3 chunks, PR)
- By: claude (reviewer) with gemini (agy) as executor
- Changed: service/product (see its HISTORY.md for T-001 and T-002.1 to T-002.3), TASKS.md, docs/AGY.md (verified agy behaviour)
- Why: trial of the one-branch chunk flow: tests first, review, implement with retry until green, one PR
- Tests: `cd service && mvn test` -> all pass (product 44, shared 31, outbox 5)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 progress-checklist - resume point for crashes and new sessions
- By: claude
- Changed: docs/PROGRESS.md (new), AGENTS.md rule 16 (+ rule 14 reads it), scripts/agy-run.ps1, scripts/agy-impl-loop.ps1
- Why: owner wants a checkbox list kept from the start of each task so work resumes from the first unticked box
- Tests: n/a (docs and helper scripts; scripts syntax-checked)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 docs-rewrite - make the .md files self-explanatory for a new session
- By: claude
- Changed: AGENTS.md (START HERE, environment facts, roles), docs/AGY.md, docs/PROGRESS.md, service/product/CONTEXT.md, TASKS.md (template status bug fixed)
- Why: owner wants any new session to understand the project, the workflow and where work stopped; rule numbers 1-16 unchanged
- Tests: n/a (docs only)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 rule-18 - discuss first, build only after go ahead
- By: claude
- Changed: AGENTS.md rule 18
- Why: owner: always discuss anything new first, implement only after the owner says go ahead
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 swgt-and-status-plan - SWGT.md, rule 19, T-003 re-planned for ProductStatus
- By: claude
- Changed: SWGT.md (new, ARCHIVED parked), AGENTS.md rule 19, TASKS.md T-003.2/T-003.3, docs/PROGRESS.md
- Why: owner: status is an enum ACTIVE/INACTIVE (default INACTIVE), events carry status as a string, ARCHIVED parked for later
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 swgt-entries - two earlier parked ideas added to SWGT.md
- By: claude
- Changed: SWGT.md (per-aggregate outbox ordering, stuck outbox row alert)
- Why: owner: SWGT.md is the dump file for future ideas
- Tests: n/a (docs only)
- Review: pending
