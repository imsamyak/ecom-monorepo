# Progress checklist (resume point)

Rule 16 (AGENTS.md): at the START of every task or chunk, write its checklist here BEFORE doing the work.
Tick each box the moment that step is done, and commit/push the tick with the work.
After a crash or in a new session: read this file first and continue from the first unticked box.
When a task is fully merged, collapse its block to one line in "Done" at the bottom.

## Current: nothing in progress
- [x] Owner said merge all (2026-10-01); branch claude/gemini-cli-setup-check-8bda3d merged to main, Review lines set to approved, T-001/T-002 done
- [ ] Waiting for the owner to give the next task

## Next task (template - copy for a new task)
- [ ] Owner gives the task; Claude adds it to TASKS.md and splits it into chunks (each stable if merged alone)
- [ ] Per chunk: tests only via agy -> review test diff -> fixes via --continue -> commit tests
- [ ] Per chunk: implement via scripts/agy-impl-loop.ps1 until green -> review code -> fix CONTEXT/HISTORY lines -> mvn test + scripts/check-context-sync.sh main -> commit
- [ ] Push the branch, give the owner the PR link

## Done
- 2026-10-01 agy trial: T-001 and T-002 built on one branch (merged to main)
