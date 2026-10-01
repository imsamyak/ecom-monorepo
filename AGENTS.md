# Working agreement (single source of truth)

`CLAUDE.md` and `GEMINI.md` only import this file. Edit rules here, nowhere else.

## START HERE (any new session, any agent)
1. Read this file completely.
2. Read `docs/PROGRESS.md`: it is the resume point. Continue from the first unticked box. If it is empty or all ticked, ask the owner for the next task.
3. Read `TASKS.md` (the queue and what each task means), then `service/<module>/CONTEXT.md` of every module you will touch, then the last entries of its `HISTORY.md`.
4. If you will use the Executor (`agy`), read `docs/AGY.md` (how to launch it, what breaks, scripts).
5. If a technology is unfamiliar, `docs/LEARNING.md` explains what this repo already uses.
6. Work only as the rules below say. When in doubt, ask the owner; the owner is the only one who can approve tests and merge.

## What this repo is
An e-commerce backend, Java 17 / Spring Boot 3.2.4, Maven multi-module under `service/`:
- `service/platform/shared` - common base types (UseCase, exceptions, JWT filter, validation)
- `service/platform/contract` – `DomainEvent` contract (record name = aggregate type, `String aggregateId()`)
- `service/platform/outbox` - transactional outbox module (auto-configured, pluggable)
- `service/product` - product service (hexagonal: `port/in`, `port/out`, `adapter`, `service`, `domain`)

Build and test everything: `cd service && mvn test`.

## Environment facts (Windows machine, 2026-10-01)
- Shell: Git Bash and PowerShell. Maven (3.9.16) and `JAVA_HOME` (JDK 21) are set in the Windows USER environment, so a NEW terminal has `mvn`. A session started before that change does not; refresh with `$env:Path = [Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [Environment]::GetEnvironmentVariable("Path","User")`.
- `gh` (GitHub CLI) is NOT installed: push with `git push`, and give the owner the compare URL `https://github.com/imsamyak/ecom-monorepo/pull/new/<branch>` to open the PR.
- Hook (once per clone): `git config core.hooksPath .githooks` (runs `scripts/check-context-sync.sh`).
- Files may have CRLF endings (git warns "LF will be replaced by CRLF"); that is harmless. Root `HISTORY.md` is Windows-1252 encoded: keep new text ASCII (use `-`, not an en dash).

## Roles
- **Owner** (the human) writes tasks, reviews the TESTS on a branch, and says "merge" or "approved". Nobody else merges.
- **Reviewer** (Claude) splits tasks into chunks, writes the checklist, reviews diffs and tests, runs the build, commits, and pushes.
- **Executor** (`agy`, the Antigravity CLI, a Gemini model) writes the tests and the production code for each chunk and does the CONTEXT/HISTORY paperwork. It cannot commit or run arbitrary commands (see `docs/AGY.md`).
- If the Executor is unavailable, Claude does both roles and still follows every rule.

## Context files (every module and the repo root)
- `CONTEXT.md` - what the module is and its current invariants and decisions. Short. **Rewrite** it when the truth changes; never let it describe old behavior. (Root context is this file.)
- `HISTORY.md` - append-only log, newest entry at the bottom, one entry per task or chunk. Never edit old entries except to add or update a review verdict (or fill a `Tests:` line that said pending).
- `TASKS.md` (root only) - the task queue with specs and statuses.
- `docs/PROGRESS.md` - the live checklist / resume point (rule 16).
- `docs/AGY.md` - everything known about running the Executor. `docs/LEARNING.md` - tech explanations (rule 13).

Entry format for `HISTORY.md`:
```
## YYYY-MM-DD <task-id> - <title>
- By: claude | gemini (agy)
- Changed: <files or areas>
- Why: <one line>
- Tests: <command> -> <result>
- Review: pending | approved | changes requested: <notes>   (reviewer fills this in)
```

## Rules
1. Code change and its context/history update go in **the same commit**. A change under a module's `src/` or `pom.xml` requires that module's `HISTORY.md` to change; a change anywhere else requires the root `HISTORY.md`. `scripts/check-context-sync.sh` enforces this (git hook + review).
2. If a decision or invariant changed, update that module's `CONTEXT.md` too. A stale `CONTEXT.md` is a review rejection.
3. Commit at the end of every task, never on `main`. **All work goes on ONE work branch** (currently `work`); do not create a new branch per task. Push it after every commit. The owner reviews and merges it whenever ready. **Never open pull requests**: the owner opens them. Do not commit build output (`target/` is ignored).
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

## Executor start-up and workflow (owner's policy)
14. **Read before you touch code.** Before any task the Executor reads, in order: this file, `docs/PROGRESS.md`, `docs/LEARNING.md`, `TASKS.md` (its task), then for every module it will change: that module's `CONTEXT.md` and the last entries of its `HISTORY.md`. It follows rules 1-13 without being reminded. When done it rewrites any `CONTEXT.md` that became stale and appends its `HISTORY.md` entry (rules 1-2).
15. **Task flow.** The Reviewer (Claude) splits an owner task into chunks; each chunk leaves the build green and the code stable if merged alone. All chunks of a task go on ONE branch, one commit group per chunk, and the owner merges that branch once. For each chunk: the Executor writes tests only; the Reviewer reads the test diff and has wrong tests corrected; the Executor then implements and runs the tests, fixing the code and retrying with **no retry limit** until they pass (a test is changed only if it was wrong, and that is said in the history entry). The Reviewer reads the final code, then pushes the branch. The owner opens pull requests. The Reviewer never merges (rule 9).
16. **Progress checklist.** At the start of every task or chunk the Reviewer writes its checklist in `docs/PROGRESS.md` before doing any work, ticks each box as the step is finished, and commits the ticks with the work. A new session or a crashed machine resumes from the first unticked box. Read `docs/PROGRESS.md` right after `AGENTS.md`.
17. Claude never writes or edits code, neither production code nor tests; every code change is done by the Executor agy; Claude splits tasks, writes prompts, reviews, runs the build and git, and edits docs.
18. **Discuss first, build only after go ahead.** When the owner asks for anything new (a feature, a design change, a new rule or tool), first discuss it: options, trade-offs, a recommendation and open questions. Do not change code, tests or task specs until the owner and Claude have reached a conclusion AND the owner explicitly says `go ahead`. Nothing else counts as a go ahead (not `ok`, not a choice between options, not silence).
