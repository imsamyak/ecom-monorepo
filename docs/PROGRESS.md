# Progress checklist (resume point)

Rule 16 (AGENTS.md): at the START of every task or chunk, write its checklist here BEFORE doing the work.
Tick each box the moment that step is done, and commit/push the tick with the work.
After a crash or in a new session: read this file first and continue from the first unticked box.
When a task is fully merged, move its block to the "Done" list at the bottom (keep it one line).

## Current: PR for branch claude/gemini-cli-setup-check-8bda3d (T-001, T-002.1-3)
- [x] T-001 trim title/description (tests, code, history)
- [x] T-002.1 active flag
- [x] T-002.2 set-active use case
- [x] T-002.3 PATCH /products/{id}/active
- [x] docs/AGY.md records verified agy behaviour; AGENTS.md rules 14-15
- [x] Branch pushed to origin
- [x] Driver scripts saved in scripts/ (agy-run.ps1, agy-impl-loop.ps1)
- [ ] Owner opens the PR (gh is not installed): https://github.com/imsamyak/ecom-monorepo/pull/new/claude/gemini-cli-setup-check-8bda3d
- [ ] Owner reviews the 5 test files and says merge/approved
- [ ] After merge: set every `Review: pending` in HISTORY.md files to `Review: approved (owner said <word>, <date>)` (rule 9)
- [ ] Optional: commit 858f6de holds the T-002.3 code under a wrong message; rewrite only if the owner asks

## How the executor loop works (so nobody re-learns it)
1. Claude writes the chunk checklist here and the task in TASKS.md.
2. Tests first: `powershell -File scripts/agy-run.ps1 <promptfile>`; Claude reviews the test diff, sends fixes with `--continue`; commit tests.
3. Implement: `powershell -File scripts/agy-impl-loop.ps1 <promptfile>` (agy edits, mvn test runs, failures go back until green, no retry limit).
4. Claude reviews the code, fixes history/context lines, runs `mvn test` and scripts/check-context-sync.sh main, commits, pushes.
Prompt rules (Windows): put the prompt in a file; ASCII only; no double quotes; tell agy to use only file tools and no subagents. Details: docs/AGY.md.

## Done
- 2026-10-01 agy trial: T-001 and T-002 built on one branch
