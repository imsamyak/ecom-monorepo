# Root history (cross-cutting changes: tooling, workflow, repo layout)

## 2026-10-01 setup-ai-workflow â€“ shared context files and sync check
- By: claude
- Changed: AGENTS.md, CLAUDE.md, GEMINI.md, TASKS.md, per-module CONTEXT.md/HISTORY.md, scripts/check-context-sync.sh, .githooks/pre-commit
- Why: executor/reviewer workflow with context kept in sync with code
- Tests: scripts/check-context-sync.sh self-check (see below) -> passed
- Review: approved (owner said "merge all branches to main", 2026-10-01)

## 2026-10-01 tdd-and-teaching-rules â€“ TDD policy, hexagonal rule, comment rule, learning guide
- By: claude
- Changed: AGENTS.md (rules 7-13), docs/LEARNING.md
- Why: owner policy: tests-first with test-only review and merge gate; keep hexagonal structure; comment every step; teach new tech
- Tests: n/a (docs only)
- New tech: docs/LEARNING.md (all sections written from what the repo already uses)
- Review: approved (owner said "merge all branches to main", 2026-10-01)

## 2026-10-01 merge-means-approved â€“ owner approval convention
- By: claude
- Changed: AGENTS.md rule 9; Review lines of earlier entries set to approved
- Why: owner clarified that saying "merge" means the tests are approved and merging is authorized
- Tests: n/a (docs only)
- Review: approved (owner said "approved", 2026-10-01)

## 2026-10-01 confess-module â€“ confess module registered
- By: claude
- Changed: service/pom.xml (module platform/confess); AGENTS.md project list; docs/LEARNING.md
- Why: new DomainEvent contract module, see service/platform/confess/HISTORY.md
- Tests: `cd service && mvn test` -> all pass
- Review: pending

## 2026-10-01 rename-contract â€“ module renamed confess -> contract
- By: claude
- Changed: service/pom.xml module name; AGENTS.md and docs/LEARNING.md references
- Why: owner asked for the module to be called contract
- Tests: `cd service && mvn test` -> all pass (no behavior change)
- Review: pending

## 2026-10-01 events-envelope â€“ sealed domain events used by all state-changing use cases
- By: claude
- Changed: docs/LEARNING.md; see the contract, outbox and product HISTORY.md files
- Why: owner design: sealed events per aggregate, {aggregate, action, data} envelope, use cases return only an event
- Tests: `cd service && mvn test` -> all pass (116 tests)
- Review: pending

## 2026-10-01 outbox-aware-rename - rename OutboxUseCase to OutboxAwareUseCase
- By: claude
- Changed: docs/LEARNING.md uses the new name; code changes are in the outbox and product HISTORY.md
- Why: owner asked for the name OutboxAwareUseCase (no behavior change)
- Tests: cd service; mvn test -> all pass (116, no test changed except the renamed type)
- Review: pending

## 2026-10-01 one-work-branch - owner's branch and PR policy
- By: claude
- Changed: AGENTS.md rules 3 and 9
- Why: owner: always keep one branch for work, the owner reviews and merges it, Claude opens no PRs
- Tests: n/a (docs only)
- Review: pending
## 2026-10-01 agy-notes – record Antigravity CLI setup and options
- By: claude
- Changed: docs/AGY.md
- Why: save the researched agy (Antigravity CLI) install path, options, models and executor loop so it is not re-researched
- Tests: n/a (docs only)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 executor-startup-rules – make the executor read context and follow the chunk flow
- By: claude
- Changed: AGENTS.md (rules 14-15), docs/AGY.md (prompt preamble, permissions)
- Why: owner wants agy to read and maintain CONTEXT/HISTORY and follow the agreed one-branch, tests-first, no-retry-limit flow
- Tests: n/a (docs only)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 T-002 - seller can deactivate and reactivate a product (3 chunks, PR)
- By: claude (reviewer) with gemini (agy) as executor
- Changed: service/product (see its HISTORY.md for T-001 and T-002.1 to T-002.3), TASKS.md, docs/AGY.md (verified agy behaviour)
- Why: trial of the one-branch chunk flow: tests first, review, implement with retry until green, one PR
- Tests: `cd service && mvn test` -> all pass (product 44, shared 31, outbox 5)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 progress-checklist - resume point for crashes and new sessions
- By: claude
- Changed: docs/PROGRESS.md (new), AGENTS.md rule 16 (+ rule 14 reads it), scripts/agy-run.ps1, scripts/agy-impl-loop.ps1
- Why: owner wants a checkbox list kept from the start of each task so work resumes from the first unticked box
- Tests: n/a (docs and helper scripts; scripts syntax-checked)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 docs-rewrite - make the .md files self-explanatory for a new session
- By: claude
- Changed: AGENTS.md (START HERE, environment facts, roles), docs/AGY.md, docs/PROGRESS.md, service/product/CONTEXT.md, TASKS.md (template status bug fixed)
- Why: owner wants any new session to understand the project, the workflow and where work stopped; rule numbers 1-16 unchanged
- Tests: n/a (docs only)
- Review: approved (owner said merge all, 2026-10-01)

## 2026-10-01 rule-18 - discuss first, build only after go ahead
- By: claude
- Changed: AGENTS.md rule 18
- Why: owner: always discuss anything new first, implement only after the owner says go ahead
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 swgt-and-status-plan - SWGT.md, rule 19, T-003 re-planned for ProductStatus
- By: claude
- Changed: SWGT.md (new, ARCHIVED parked), AGENTS.md rule 19, TASKS.md T-003.2/T-003.3, docs/PROGRESS.md
- Why: owner: status is an enum ACTIVE/INACTIVE (default INACTIVE), events carry status as a string, ARCHIVED parked for later
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 swgt-entries - two earlier parked ideas added to SWGT.md
- By: claude
- Changed: SWGT.md (per-aggregate outbox ordering, stuck outbox row alert)
- Why: owner: SWGT.md is the dump file for future ideas
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 rule-20-21 rule - small commits, history entries made for rollback
- By: claude
- Changed: AGENTS.md rules 20 and 21, HISTORY.md entry format (task kind, Depends on, Rollback)
- Why: owner: keep commits and history entries small so any feature can be understood and rolled back easily
- Depends on: none
- Rollback: git revert the commit found by git log --grep "rule: small commits"
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-004 docs - plan the agy progress log
- By: claude
- Changed: TASKS.md (T-004), docs/PROGRESS.md
- Why: owner: agy should log what it is doing so a blocker is visible
- Depends on: none
- Rollback: git revert the commits found by git log --grep T-004 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-004 impl - progress log watcher for agy scripts
- By: gemini (agy)
- Changed: .gitignore, scripts/agy-run.ps1, scripts/agy-impl-loop.ps1
- Why: track agy execution progress in background to detect stuck runs
- Depends on: none
- Rollback: git revert the commits found by git log --grep T-004 (newest first)
- Tests: manual check run (agy-run.ps1, 28s): start line, agy step note and end line written to logs/agy-progress.log; logs/ ignored by git; the 30 s watcher line is confirmed on the next run longer than 30 s
- Review: pending

## 2026-10-01 T-004 docs - document the progress log and stuck-run checks
- By: claude
- Changed: docs/AGY.md section 7
- Why: so every session reads the progress log and stops a stuck agy run
- Depends on: T-004 impl
- Rollback: git revert the commits found by git log --grep T-004 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-006 docs - plan the Event suffix rename
- By: claude
- Changed: TASKS.md (T-006), docs/PROGRESS.md
- Why: owner chose option 1 to fix the Product/Variant name clash between entities and events
- Depends on: none
- Rollback: git revert the commits found by git log --grep T-006 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 docs - park multiple events per use case in SWGT.md
- By: claude
- Changed: SWGT.md
- Why: owner: list support is useful later, park it
- Depends on: none
- Rollback: git revert the commit found by git log --grep "park multiple events"
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 docs - park token-cost reduction ideas in SWGT.md
- By: claude
- Changed: SWGT.md
- Why: owner: reduce cost later
- Depends on: none
- Rollback: git revert the commit found by git log --grep "park token-cost"
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 docs - clean up docs/PROGRESS.md
- By: claude
- Changed: docs/PROGRESS.md (removed a duplicated block and a false merged-to-main line, ticked finished steps)
- Why: owner asked for the pending status; the checklist had drifted
- Depends on: none
- Rollback: git revert the commit found by git log --grep "clean up docs/PROGRESS.md"
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-007 docs - plan the agy chunk driver pilot, rule 22
- By: claude
- Changed: TASKS.md (T-007), docs/PROGRESS.md, AGENTS.md rule 22
- Why: owner: design agy to cut Claude's cost, pilot it, keep optimizing, pivot if needed
- Depends on: T-004
- Rollback: git revert the commits found by git log --grep T-007 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-007 docs - add pilot B (one large self-checking agy run)
- By: claude
- Changed: TASKS.md (T-007)
- Why: owner: use agy's large context, let agy take whole tasks, delegate, test itself and reduce overhead
- Depends on: T-004
- Rollback: git revert the commits found by git log --grep T-007 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-005 docs - plan event mapping in the MapStruct mappers
- By: claude
- Changed: TASKS.md (T-005), docs/PROGRESS.md
- Why: owner: build every event with a mapper (all or none), go ahead given
- Depends on: T-006
- Rollback: git revert the commits found by git log --grep T-005 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-007 docs - record the T-003.3 baseline cost
- By: claude
- Changed: docs/AGY.md section 8, docs/PROGRESS.md
- Why: rule 22: measure the old process before piloting the new one
- Depends on: T-003.3
- Rollback: git revert the commits found by git log --grep T-007 (newest first)
- Tests: n/a (docs only)
- Review: pending
## 2026-10-01 T-007 impl - agy chunk driver and prompt templates (pilot A)
- By: gemini (agy)
- Changed: scripts/prompts/tests.txt, scripts/prompts/implement.txt, scripts/agy-chunk.ps1
- Why: build pilot A chunk driver and templates
- Depends on: T-004
- Rollback: git revert the commits found by git log --grep T-007 (newest first)
- Tests: syntax check (0 parse errors); reviewed by Claude (fixed: git status -uall so new files in new folders are seen; templates gained the search rule, the HISTORY heading format and the CONTEXT/LEARNING duty); known gap: no stall/time-limit guard yet (agy calls keep their 25 min timeout, the loop stops after 5 identical failures); real check is the T-005 pilot run
- Review: pending

## 2026-10-01 rule - rule 17: Claude is accountable for agy's output
- By: claude
- Changed: AGENTS.md rule 17
- Why: owner: Claude controls agy and must make sure the code never breaks
- Depends on: none
- Rollback: git revert the commit found by git log --grep "rule 17: Claude is accountable"
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 rule - rule 3: push only when the tip of work builds
- By: claude
- Changed: AGENTS.md rule 3
- Why: owner: code must never break; a red tests commit alone on the remote would break a merge
- Depends on: none
- Rollback: git revert the commit found by git log --grep "rule 3: push only"
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-007 docs - document the chunk driver (docs/AGY.md section 9, rule 15)
- By: claude
- Changed: docs/AGY.md section 9, AGENTS.md rule 15
- Why: how to run a chunk with one command and review once from the report
- Depends on: T-007 impl
- Rollback: git revert the commits found by git log --grep T-007 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-007 docs - pilot A result
- By: claude
- Changed: docs/AGY.md section 8, docs/PROGRESS.md
- Why: rule 22: record the measured cost of pilot A against the T-003.3 baseline
- Depends on: T-007 impl, T-005
- Rollback: git revert the commits found by git log --grep T-007 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-008 docs - plan the inbox module
- By: claude
- Changed: TASKS.md (T-008 with three chunks), docs/PROGRESS.md
- Why: owner agreed the inbox design and all five recommendations, go ahead given
- Depends on: T-005, T-006
- Rollback: git revert the commits found by git log --grep T-008 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 docs - park inbox follow-ups in SWGT.md
- By: claude
- Changed: SWGT.md (Bloom filter, dead-letter queue, backfill for new listeners, DynamoDB adapter)
- Why: owner agreed to park them during the inbox design
- Depends on: none
- Rollback: git revert the commit found by git log --grep "park inbox follow-ups"
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-008.1 impl - inbox module registration
- By: gemini (agy)
- Changed: service/pom.xml
- Why: Register the new platform/inbox module
- Depends on: T-005, T-006
- Rollback: git revert the commits found by git log --grep T-008.1 (newest first)
- Tests: mvn -f service/pom.xml test -> GREEN
- Review: pending

## 2026-10-01 docs - low-memory automatic restart policy
- By: claude
- Changed: docs/AGY.md section 10
- Why: owner: restart a run killed for low memory after 5, 10, 15, 20, 25 seconds, then ask
- Depends on: none
- Rollback: git revert the commit found by git log --grep "low-memory automatic restart"
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-007 docs - pilot B result
- By: claude
- Changed: docs/AGY.md section 8
- Why: rule 22: record pilot B (one self-building agy run) against pilot A
- Depends on: T-008.1
- Rollback: git revert the commits found by git log --grep T-007 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-009 docs - queue the JWT task next (design in progress)
- By: claude
- Changed: TASKS.md (T-009), docs/PROGRESS.md
- Why: owner: JWT is the next task; design points agreed so far recorded
- Depends on: none
- Rollback: git revert the commits found by git log --grep T-009 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 docs - root cause of agy stopping early or aborting
- By: claude
- Changed: docs/AGY.md section 11
- Why: owner asked for the root cause; reproduced with a kept agy log
- Depends on: none
- Rollback: git revert the commit found by git log --grep "root cause of agy"
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-010 docs - plan agy tooling (kept logs, stream-json, JSON report)
- By: claude
- Changed: TASKS.md (T-010), docs/PROGRESS.md
- Why: owner go ahead: persist agy logs, use the CLI features, helper skills, try other models
- Depends on: T-007
- Rollback: git revert the commits found by git log --grep T-010 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-010 impl - agy tooling: kept logs, stream-json, JSON report, stall guard
- By: gemini (agy)
- Changed: scripts/agy-run.ps1, scripts/agy-impl-loop.ps1, scripts/agy-chunk.ps1, scripts/prompts/report.schema.json, scripts/prompts/tests.txt, scripts/prompts/implement.txt
- Why: Keep logs, use stream-json, structured output, and stall guard
- Depends on: T-007
- Rollback: git revert the commits found by git log --grep T-010 (newest first)
- Tests: check runs by Claude on Windows PowerShell 5.1: parse check clean; schema valid JSON; a run keeps logs/agy/<time>-<prompt>.log and .jsonl and prints agy's final reply from the stream result event. Three bugs found by the first check run and fixed by agy (PowerShell 5.1 Split-Path, stream result parsing, fix-round prompt name)
- Notes: these three fixes came from Claude's check run.
- Notes: these three fixes came from Claude's experiment run.
- Review: pending

## 2026-10-01 rule - rule 23 and the decision log
- By: claude
- Changed: AGENTS.md rule 23, .agent/notes/decisions.md (new, backfilled with every decision so far, incl. the inventory and JWT discussions)
- Why: owner: track all discussions, implementations, history and logs in files so recovery is always possible
- Depends on: none
- Rollback: git revert the commit found by git log --grep "rule 23"
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 T-007 docs - strategy experiment result (S1 two-step wins)
- By: claude
- Changed: docs/AGY.md section 8, .agent/notes/decisions.md, docs/PROGRESS.md, TASKS.md
- Why: rule 22: owner asked to test strategies on separate branches and keep the better one
- Depends on: T-008.2
- Rollback: git revert the commits found by git log --grep "strategy experiment result"
- Tests: n/a (docs only)
- Review: pending
## 2026-10-01 T-010 impl - driver resumes the same agy conversation after a command abort
- By: gemini (agy)
- Changed: scripts/agy-chunk.ps1
- Why: resume the same conversation after a command abort instead of starting from scratch
- Depends on: T-010
- Rollback: git revert the commit found by git log --grep "resumes the same agy conversation"
- Tests: pending (checked by the next chunk run)
- Review: pending

## 2026-10-01 T-009 docs - final JWT spec in three chunks
- By: claude
- Changed: TASKS.md (T-009 spec, T-007 status), docs/PROGRESS.md
- Why: owner go ahead for JWT; open details settled with the recommendations
- Depends on: T-008
- Rollback: git revert the commits found by git log --grep T-009 (newest first)
- Tests: n/a (docs only)
- Review: pending

## 2026-10-01 docs - cost plan: design chat and execution chat
- By: claude
- Changed: .agent/notes/decisions.md, docs/PROGRESS.md (start-here line for the execution chat)
- Why: owner: reduce cost; split into a compacted design chat and a fresh execution chat
- Depends on: none
- Rollback: git revert the commit found by git log --grep "cost plan"
- Tests: n/a (docs only)
- Review: pending
