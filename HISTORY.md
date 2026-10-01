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

## 2026-10-01 agy-notes – record Antigravity CLI setup and options
- By: claude
- Changed: docs/AGY.md
- Why: save the researched agy (Antigravity CLI) install path, options, models and executor loop so it is not re-researched
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 executor-startup-rules – make the executor read context and follow the chunk flow
- By: claude
- Changed: AGENTS.md (rules 14-15), docs/AGY.md (prompt preamble, permissions)
- Why: owner wants agy to read and maintain CONTEXT/HISTORY and follow the agreed one-branch, tests-first, no-retry-limit flow
- Tests: n/a (docs only)
- Review: pending
