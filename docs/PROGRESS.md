# Progress checklist (resume point)

Rule 16 (AGENTS.md): at the START of every task or chunk, write its checklist here BEFORE doing the work.
Tick each box the moment that step is done, and commit/push the tick with the work.
After a crash or in a new session: read this file first and continue from the first unticked box.
When a task is fully merged, collapse its block to one line in "Done" at the bottom.

## Current: branch claude/gemini-cli-setup-check-8bda3d (T-001, T-002.1-3, docs and tooling)
Everything is built, tested (product 44, shared 31, outbox 5 all pass) and pushed. Only the owner's steps remain:
- [x] T-001 trim title/description (tests, code, history)
- [x] T-002.1 active flag, T-002.2 set-active use case, T-002.3 PATCH /products/{id}/active
- [x] Rules 14-16, docs/AGY.md, driver scripts in scripts/ (agy-run.ps1, agy-impl-loop.ps1)
- [x] All .md files rewritten so a new session can pick up (AGENTS.md has a START HERE section)
- [x] Branch pushed to origin
- [ ] Owner opens the PR (gh is not installed): https://github.com/imsamyak/ecom-monorepo/pull/new/claude/gemini-cli-setup-check-8bda3d
- [ ] Owner reviews the 5 test files (ProductPersistenceTest, ProductActiveFlagTest, ProductActiveFlagPersistenceTest, SetProductActiveServiceTest, ProductActiveControllerTest) and says merge or approved
- [ ] After the owner says merge: Claude merges to main, sets every `Review: pending` in the HISTORY.md files to `Review: approved (owner said <word>, <date>)` (rule 9), sets T-001/T-002 to `done` in TASKS.md, moves this block to Done
- [ ] Optional, only if the owner asks: commit 858f6de holds the T-002.3 code under a wrong message; rewrite history

## Next task (template - copy for a new task)
- [ ] Owner gives the task; Claude adds it to TASKS.md and splits it into chunks (each stable if merged alone)
- [ ] Per chunk: tests only via agy -> review test diff -> fixes via --continue -> commit tests
- [ ] Per chunk: implement via scripts/agy-impl-loop.ps1 until green -> review code -> fix CONTEXT/HISTORY lines -> mvn test + scripts/check-context-sync.sh main -> commit
- [ ] Push the branch, give the owner the PR link

## Done
- 2026-10-01 agy trial: T-001 and T-002 built on one branch (waiting for merge, see above)
