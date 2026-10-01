# Progress checklist (resume point)

Rule 16 (AGENTS.md): at the START of every task or chunk, write its checklist here BEFORE doing the work.
Tick each box the moment that step is done, and commit/push the tick with the work.
After a crash or in a new session: read this file first and continue from the first unticked box.
When a task is fully merged, collapse its block to one line in "Done" at the bottom.

## Current: branch work (the one work branch; the owner reviews and merges it)
- [x] T-006 (event interfaces end in Event): agy implements until green, Claude reviews, commits, pushes
- [x] T-003.3 (PATCH /products/{id} partial update): agy writes tests only, Claude reviews, commits red tests
- [x] T-003.3: agy implements until green, Claude reviews, commits, pushes
- [ ] Baseline: count Claude's cost for T-003.3 on the old process (tool calls, prompt size, diff lines read, retries, time)
- [ ] T-007 (agy chunk driver): agy builds scripts/agy-chunk.ps1, templates and report; Claude updates rule 15 and docs/AGY.md; separate commits
- [ ] Pilot: next chunk on the new process, compare with the baseline, keep / adjust / roll back, record in docs/AGY.md
- [ ] T-005 (event mapping in MapStruct mappers): waits for the owner's go ahead
- [ ] Owner reviews the tests of T-003.2, T-003.3, T-006 (and the agy-changed tests listed in service/product/HISTORY.md) and merges work when ready

## Done on branch work (not on main until the owner merges work)
- [x] Folded claude/gemini-cli-setup-check-8bda3d (T-001, T-002) and confess-module into work (8a67544)
- [x] T-003.1 stale event names in comments and messages (57c8995)
- [x] T-003.2 ProductStatus enum replaces the active boolean: tests 1f86d60, impl ab6f1a9
- [x] T-004 progress log for agy runs: impl f93aa9e, docs 7aaac90 (watcher confirmed on a longer run)
- [x] T-006 tests (d250a46)
- [x] Rules 18-21, SWGT.md with parked ideas
