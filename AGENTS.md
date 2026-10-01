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
- `service/platform/contract` – `DomainEvent` contract (record name = aggregate type, `String aggregateId()`)
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
3. Commit at the end of every task, never on `main`. **All work goes on ONE work branch** (currently `confess-module`); do not create a new branch per task. Push it after every commit. The owner reviews and merges it whenever ready. **Never open pull requests**: the owner opens them. Do not commit build output (`target/` is ignored).
4. Keep tasks small so diffs are cheap to review. Do not refactor beyond the task.
5. Tests must pass before committing; record the command and result in `HISTORY.md`.
6. Enable the hook once per clone: `git config core.hooksPath .githooks`.

## Test-driven development (owner's policy)
7. **Tests first.** For any new behavior or bug fix, write or change the tests first, watch them fail for the right reason, then change production code until they pass. Existing behavior is protected by tests; a change that breaks an old test is a regression unless the owner approves changing that test.
8. **Owner reviews tests only.** The owner reviews the added or changed test files on a branch, not the production diff. So: keep test-only changes in their own commit where possible, name tests as sentences describing behavior, and never weaken or delete an existing test to make a change pass without saying so explicitly in the history entry.
9. **Merge gate.** The owner merges the work branch to `main` himself. The reviewer merges only if the owner explicitly asks, and only after the owner has approved the tests on that branch. The owner saying "merge" (for one branch, or "merge all") or "approved" is itself the approval and the go-ahead: it means the tests on those branches are approved and merging is authorized. On merging, change that branch's `Review: pending` lines in the `HISTORY.md` files to `Review: approved (owner said <merge|approved>, <date>)`. Without one of those words, never merge. Approval covers the named branches only; it is not a standing approval for later branches.
10. **Known bugs.** A test that exposes a production bug is written for the *correct* behavior and marked `@Disabled("BUG: <what is wrong>")` so the build stays green. Never encode wrong behavior as expected. List disabled tests in the history entry for the owner to decide on.

## Architecture, comments and teaching (owner's policy)
11. **Hexagonal structure, always.** Every service follows the layout of `service/product`; new modules and features copy it, they do not invent another.
    - `port/in/usecase/...` – use case interfaces plus their command/query/result DTOs. This is what the outside world may call.
    - `port/out/...` – interfaces the core needs from the outside (persistence, messaging, other services).
    - `service/...` – use case implementations. They depend only on `port/in` types, `port/out` interfaces and `domain`. Never on controllers, repositories or other adapters.
    - `adapter/in/...` – entry points (web controllers, request/response DTOs, web mappers, security). They call `port/in` use cases only and never touch repositories or entities.
    - `adapter/out/...` – implementations of `port/out` (persistence adapters, Spring Data repositories). Nothing outside `adapter/out` references a repository.
    - `domain/...` – entities, domain exceptions, converters. Business rules live here (e.g. `Product.verifyOwnership`), not in controllers or adapters.
    - Dependencies point inward: adapters -> ports <- services -> domain. Each layer has its own DTOs and mappers; no entity crosses the web boundary.
    - Tests mirror the package layout of the code they cover.
12. **Comment every step, without numbering.** This applies to ALL code you write or change: production code, tests, scripts, configuration. Put a short comment before each meaningful step saying what it does and why (for example `// Reject the request if the seller does not own this product`). Plain sentences, no "Step 1/2/3" and no numbered lists. When you touch an existing method, add the missing step comments to that method in the same change. Comment-only edits never change behavior.
13. **Teach the new tech.** Whenever a task uses a technology, library, pattern or Spring feature not yet explained in `docs/LEARNING.md` (AOP, actuator, async, auto-configuration, etc.), add a section there: what it is, where it is used in this repo, how it works, gotchas. Then add a `New tech:` line to the task's `HISTORY.md` entry linking the section, and explain it briefly in the reply to the owner.
