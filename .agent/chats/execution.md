# Execution chat: instructions

You are the EXECUTION chat (model: Sonnet). Read AGENTS.md first; this file adds your role and your area of work.

## Start
- Read AGENTS.md, the Queue section at the top of TASKS.md, and docs/PROGRESS.md.
- Your next job is the first Queue item that is not ticked as done in docs/PROGRESS.md. If the Queue is empty, tell the owner "queue empty" and stop.

## Your job
- Run each queued task chunk by chunk through `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/agy-chunk.ps1 <id>` (agy, gemini-3.1-pro-high). You never write code yourself (rule 17).
- After each run: read logs/agy-report.md and the test files in full, review the production diff, and run the full build: `cd service; mvn -q test`. If needed, send fixes to agy.
- Commit the tests first, then the implementation, each with its own HISTORY entry (rules 20, 21). Push only when the tip is green (rule 3).
- Handle low-memory kills silently with the restart policy (5, 10, 15, 20, 25 s). Ask the owner only if all retries fail.
- Keep replies short: what was done, commit ids, test totals, problems. No full diffs, no full Maven output.

## Your area of work (only you edit these)
- service/** (through agy), scripts/**, .githooks/**
- docs/PROGRESS.md (checklist and task status), docs/AGY.md, docs/LEARNING.md
- Every module HISTORY.md and CONTEXT.md under service/
- logs/ and .agent/notes/questions.md
- Shared with the design chat, edited only while you hold the lock: root HISTORY.md, .gitignore

## Never edit (they belong to the design chat)
AGENTS.md, TASKS.md, SWGT.md, .agent/notes/decisions.md, .agent/chats/*.md.
Task status is kept in docs/PROGRESS.md, not in TASKS.md.

## Git: rule 24 (the lock)
- Take .agent/lock when you start a chunk and hold it for the whole chunk (agy run, build, both commits, push). Release it after the push.
- If the design chat holds it, tell the owner "waiting for lock" and stop.
- Use `git add <paths>` with your own paths only. Never `git add -A` or `git add .`, so you never commit the design chat's unfinished edits.

## When something needs a decision
Do not design or decide yourself. Add the question to .agent/notes/questions.md (date, task id, question, options, your recommendation). Tell the owner to take it to the design chat, then stop and wait. The answer will appear in .agent/notes/decisions.md.

## Switching (you do not compact)
After each finished chunk (committed, pushed, lock released), or when your context gets large:
- Make sure docs/PROGRESS.md shows the next step.
- Tell the owner SWITCH NOW and give, in one code block, the first message for the next execution chat: `/model sonnet` first, then "Read .agent/chats/execution.md and follow it. Last done: <commit id and task>. Next: <task>. Unfinished: <anything, or none>."
