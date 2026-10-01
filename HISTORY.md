# Root history (cross-cutting changes: tooling, workflow, repo layout)

## 2026-10-01 setup-ai-workflow – shared context files and sync check
- By: claude
- Changed: AGENTS.md, CLAUDE.md, GEMINI.md, TASKS.md, per-module CONTEXT.md/HISTORY.md, scripts/check-context-sync.sh, .githooks/pre-commit
- Why: executor/reviewer workflow with context kept in sync with code
- Tests: scripts/check-context-sync.sh self-check (see below) -> passed
- Review: approved (owner said "merge all branches to main", 2026-10-01)

## 2026-10-01 tdd-and-teaching-rules – TDD policy, hexagonal rule, comment rule, learning guide
- By: claude
- Changed: AGENTS.md (rules 7-13), docs/LEARNING.md
- Why: owner policy: tests-first with test-only review and merge gate; keep hexagonal structure; comment every step; teach new tech
- Tests: n/a (docs only)
- New tech: docs/LEARNING.md (all sections written from what the repo already uses)
- Review: approved (owner said "merge all branches to main", 2026-10-01)

## 2026-10-01 merge-means-approved – owner approval convention
- By: claude
- Changed: AGENTS.md rule 9; Review lines of earlier entries set to approved
- Why: owner clarified that saying "merge" means the tests are approved and merging is authorized
- Tests: n/a (docs only)
- Review: approved (owner said "approved", 2026-10-01)

## 2026-10-01 confess-module – confess module registered
- By: claude
- Changed: service/pom.xml (module platform/confess); AGENTS.md project list; docs/LEARNING.md
- Why: new DomainEvent contract module, see service/platform/confess/HISTORY.md
- Tests: `cd service && mvn test` -> all pass
- Review: pending
