# Root history (cross-cutting changes: tooling, workflow, repo layout)

## 2026-10-01 setup-ai-workflow – shared context files and sync check
- By: claude
- Changed: AGENTS.md, CLAUDE.md, GEMINI.md, TASKS.md, per-module CONTEXT.md/HISTORY.md, scripts/check-context-sync.sh, .githooks/pre-commit
- Why: executor/reviewer workflow with context kept in sync with code
- Tests: scripts/check-context-sync.sh self-check (see below) -> passed
- Review: pending

## 2026-10-01 tdd-and-teaching-rules – TDD policy, hexagonal rule, comment rule, learning guide
- By: claude
- Changed: AGENTS.md (rules 7-13), docs/LEARNING.md
- Why: owner policy: tests-first with test-only review and merge gate; keep hexagonal structure; comment every step; teach new tech
- Tests: n/a (docs only)
- New tech: docs/LEARNING.md (all sections written from what the repo already uses)
- Review: pending
