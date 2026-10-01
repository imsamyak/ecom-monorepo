# Progress checklist (resume point)

Rule 16 (AGENTS.md): at the START of every task or chunk, write its checklist here BEFORE doing the work.
Tick each box the moment that step is done, and commit/push the tick with the work.
After a crash or in a new session: read this file first and continue from the first unticked box.
When a task is fully merged, collapse its block to one line in "Done" at the bottom.

## Current: branch work - fold claude/gemini-cli-setup-check-8bda3d into work (one work branch)
- [x] Stash the undecided present-tense rename (stash present-tense-rename-prod-wip-claude, sha 24de9a01)
- [x] git merge --no-commit claude/gemini-cli-setup-check-8bda3d into work (conflicts: AGENTS.md, HISTORY.md, service/product/CONTEXT.md, service/product/HISTORY.md)
- [x] agy resolves the conflicts and makes the build green (scripts/agy-impl-loop.ps1) - green after 2 fix rounds
- [x] Claude reviews the resolution and any code agy changed (docs fixed by Claude; stale comments in code left for agy)
- [x] Claude commits the merge and pushes work
- [x] Stashed rename superseded: agy implemented CREATE/UPDATE/DELETE/ADD/REMOVE itself in this merge; stash kept until the owner says to drop it
## Previous: nothing in progress
- [x] Owner said merge all (2026-10-01); branch claude/gemini-cli-setup-check-8bda3d merged to main, Review lines set to approved, T-001/T-002 done
- [ ] Waiting for the owner to give the next task

## Current: T-003 on branch work
- [x] Drop the redundant stash present-tense-rename-prod-wip-claude
- [x] T-003.1: agy fixes stale names (scripts/agy-impl-loop.ps1), Claude reviews, commits, pushes
- [x] T-003.2 (ProductStatus enum replaces active): agy writes tests only, Claude reviews, commits red tests (1f86d60)
- [x] T-003.2: agy implements until green, Claude reviews, commits, pushes
- [ ] T-003.3 (PATCH partial update): agy writes tests only, Claude reviews, commits red tests
- [ ] T-003.3: agy implements until green, Claude reviews, commits, pushes
- [x] T-004: after the T-003.2 loop finishes, agy adds the watcher to the scripts and .gitignore; Claude updates docs/AGY.md; manual check run; separate commits
- [ ] Owner reviews the T-003.2 and T-003.3 tests and merges work when ready# Progress checklist (resume point)

Rule 16 (AGENTS.md): at the START of every task or chunk, write its checklist here BEFORE doing the work.
Tick each box the moment that step is done, and commit/push the tick with the work.
After a crash or in a new session: read this file first and continue from the first unticked box.
When a task is fully merged, collapse its block to one line in "Done" at the bottom.

## Current: branch work - fold claude/gemini-cli-setup-check-8bda3d into work (one work branch)
- [x] Stash the undecided present-tense rename (stash present-tense-rename-prod-wip-claude, sha 24de9a01)
- [x] git merge --no-commit claude/gemini-cli-setup-check-8bda3d into work (conflicts: AGENTS.md, HISTORY.md, service/product/CONTEXT.md, service/product/HISTORY.md)
- [x] agy resolves the conflicts and makes the build green (scripts/agy-impl-loop.ps1) - green after 2 fix rounds
- [x] Claude reviews the resolution and any code agy changed (docs fixed by Claude; stale comments in code left for agy)
- [x] Claude commits the merge and pushes work
- [x] Stashed rename superseded: agy implemented CREATE/UPDATE/DELETE/ADD/REMOVE itself in this merge; stash kept until the owner says to drop it
## Previous: nothing in progress
- [x] Owner said merge all (2026-10-01); branch claude/gemini-cli-setup-check-8bda3d merged to main, Review lines set to approved, T-001/T-002 done
- [ ] Waiting for the owner to give the next task

## Current: T-003 on branch work
- [x] Drop the redundant stash present-tense-rename-prod-wip-claude
- [x] T-003.1: agy fixes stale names (scripts/agy-impl-loop.ps1), Claude reviews, commits, pushes
- [ ] T-003.2: agy writes tests only (scripts/agy-run.ps1), Claude reviews the test diff, commits the red tests
- [ ] T-003.2: agy implements until green (scripts/agy-impl-loop.ps1), Claude reviews, commits, pushes
- [ ] Owner reviews the T-003.2 tests and merges work when ready