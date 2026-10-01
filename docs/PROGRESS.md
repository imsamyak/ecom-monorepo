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

## Next task (template - copy for a new task)
- [ ] Owner gives the task; Claude adds it to TASKS.md and splits it into chunks (each stable if merged alone)
- [ ] Per chunk: tests only via agy -> review test diff -> fixes via --continue -> commit tests
- [ ] Per chunk: implement via scripts/agy-impl-loop.ps1 until green -> review code -> fix CONTEXT/HISTORY lines -> mvn test + scripts/check-context-sync.sh main -> commit
- [ ] Push the branch, give the owner the PR link

## Done
- 2026-10-01 agy trial: T-001 and T-002 built on one branch (merged to main)

## Next
- [ ] agy: update the stale comments and the Outbox.java message that still say Created/Updated/Deleted/Added/Removed
- [ ] Owner decides: should SetProductActiveService emit a Product event (it changes state but emits none yet), and should events carry `active`