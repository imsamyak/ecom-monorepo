# Working agreement (single source of truth)

`CLAUDE.md` and `GEMINI.md` only import this file. Edit rules here, nowhere else.

## Roles
- **Owner** writes tasks in `TASKS.md` (spec + acceptance criteria).
- **Executor** (Gemini, once set up) implements a task on branch `gemini/<task-id>`, runs the tests, updates context, commits.
- **Reviewer** (Claude) reads the diff and test output, then approves or requests changes in the module's `HISTORY.md`.
- Until the owner says the executor is live, Claude does both roles and still follows every rule below.

## Project
Java 17, Spring Boot 3.2.4, Maven multi-module under `service/`:
- `service/platform/shared` – common base types (UseCase, exceptions, JWT filter, validation)
- `service/platform/outbox` – transactional outbox module (auto-configured, pluggable)
- `service/product` – product service (hexagonal: `port/in`, `port/out`, `adapter`, `service`, `domain`)

Build and test everything: `cd service && mvn test` (needs network for first dependency download).

## Context files (every module and the repo root)
- `CONTEXT.md` – what the module is and its current invariants and decisions. Short. **Rewrite** it when the truth changes; never let it describe old behavior. (Root context is this file.)
- `HISTORY.md` – append-only log, newest entry at the bottom, one entry per task. Never edit old entries except to add a review verdict.
- `TASKS.md` (root only) – the task queue.

Entry format for `HISTORY.md`:
```
## YYYY-MM-DD <task-id> – <title>
- By: claude | gemini
- Changed: <files or areas>
- Why: <one line>
- Tests: <command> -> <result>
- Review: pending | approved | changes requested: <notes>   (reviewer fills this in)
```

## Rules
1. Code change and its context/history update go in **the same commit**. A change under a module's `src/` or `pom.xml` requires that module's `HISTORY.md` to change; a change anywhere else requires the root `HISTORY.md`. `scripts/check-context-sync.sh` enforces this (git hook + review).
2. If a decision or invariant changed, update that module's `CONTEXT.md` too. A stale `CONTEXT.md` is a review rejection.
3. Commit at the end of every task, on a feature branch, never on `main`. Do not commit build output (`target/` is ignored). No pull requests unless the owner asks.
4. Keep tasks small so diffs are cheap to review. Do not refactor beyond the task.
5. Tests must pass before committing; record the command and result in `HISTORY.md`.
6. Enable the hook once per clone: `git config core.hooksPath .githooks`.
