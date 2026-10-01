# Executor guide: the Antigravity CLI (`agy`)

Everything known about running `agy` as the Executor. Verified 2026-10-01 (T-001, T-002). Read this instead of re-investigating.

## 1. What it is and why not `gemini`
- `agy` is Google's Antigravity CLI (v1.2.14). It replaced the old Gemini CLI for individual users.
- The old `gemini` CLI (npm `@google/gemini-cli` 0.62.0) fails sign-in: "This client is no longer supported for Gemini Code Assist for individuals". Do not try to fix it.
- Binary: `C:\Users\Compro\AppData\Local\agy\bin\agy.exe` (NOT on PATH). State: `~/.gemini/antigravity-cli/` (`settings.json` holds permission grants, `cli.log` for debugging). `antigravity` on PATH is the IDE launcher; it cannot take tasks.
- Login is done. Models: `agy models` (Gemini 3.x Flash/Pro, Claude 4.6, GPT-OSS). We use `gemini-3.1-pro-high` so the Claude review stays independent.

## 2. How to run it (use the repo scripts)
- One prompt: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/agy-run.ps1 <promptfile> [--continue]`
  Refreshes PATH/JAVA_HOME, cds to the repo, runs `agy -p <prompt> --mode accept-edits --model gemini-3.1-pro-high --print-timeout 25m`. `--continue` continues the last conversation (use it to send review feedback).
- Implement-until-green loop: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/agy-impl-loop.ps1 <promptfile>`
  agy implements, the script runs `mvn test`, failures go back to agy with `--continue`, repeat. No retry limit; it stops only if the identical failure repeats 5 times in a row. Prints a short summary only.
- Prompt files live outside the repo (the session scratchpad). Write the prompt to a file; the scripts read it with `Get-Content -Raw`.

## 3. Prompt rules (each one fixed a real failure)
- ASCII only and NO double quotes in prompts (Windows splits the argument: "unexpected argument").
- Start every prompt with: use ONLY file view/search/write tools; NEVER the run-command tool; no subagents.
- Then: read AGENTS.md (all rules), docs/PROGRESS.md, docs/LEARNING.md, the task in TASKS.md, and the CONTEXT.md and last HISTORY entries of each module it changes. agy does not load AGENTS.md by itself.
- Say exactly which step it is on (tests only, or implement) and what it must NOT touch (production code in the tests step, tests in the implement step, no commit).
- Tell it to update CONTEXT.md and append a HISTORY.md entry (By: gemini (agy); Tests: pending; Review: pending). Check `git status` afterwards: it forgets them when a prompt fails.

## 4. Permissions (why it cannot run Maven by itself)
- Headless (`-p`) mode cannot ask for permission, so any command not pre-approved is denied and the whole run aborts with no output. `--mode accept-edits` only auto-approves file edits.
- Grants live in `~/.gemini/antigravity-cli/settings.json` `permissions.allow` and are EXACT command strings (`git status -sb` is allowed, `git status --short -b` is not). Wildcards and prefixes did not work headless. Some grants exist (for example `mvn test -pl product -am`); that one worked only when agy was started from a shell with the refreshed PATH.
- `--dangerously-skip-permissions` exists but Claude Code's safety classifier blocked launching it; it needs an explicit owner decision. We do not use it.
- Result: agy edits files; Claude (or the loop script) runs `mvn` and `git`.

## 5. Behaviour you will see
- Good: follows AGENTS.md when told to read it, writes tests first, comments every step, keeps hexagonal layers, follows the HISTORY format.
- It may call the run-command tool just to explore (aborts the run) - hence the prompt rule above.
- It may start a research subagent and return early ("I have sent a subagent..."). Resend with `--continue`: do the research yourself in this turn.
- Mistakes a reviewer must catch in its tests: wrong method name for a new API, assertions on a value the test itself stubbed (tautology), builders missing a `@NonNull` field (NPE), only testing the default value.
- In the loop it may edit a test it believes is wrong. That is allowed only if the test was provably wrong; the HISTORY entry must say so (rule 8). Review that diff.

## 6. Reference: agy options
- Run: `-p/--print/--prompt`, `-i/--prompt-interactive`, `--print-timeout`, `--output-format text|json|stream-json`, `--input-format stream-json`, `--json-schema`.
- Continue: `-c/--continue`, `--conversation <id>`.
- Control: `--mode accept-edits|plan`, `--dangerously-skip-permissions`, `--sandbox`, `--model`, `--effort low|medium|high|max`, `--add-dir`.
- Subcommands: `models`, `agents`, `mcp`, `plugin`, `update`, `install`, `changelog`, `remote-control`.

## 7. Progress log and how to spot a stuck run (T-004)
- Both scripts write to `logs/agy-progress.log` (git-ignored): a start line, a watcher line every 30 seconds (elapsed minutes, last step and last file written, read from `cli.log`; the loop also shows its round), and an end line with exit code and duration.
- Prompt rule: every prompt asks agy to append one plain-English line per step to `logs/agy-progress.log` (what it is doing, or what blocks it).
- Claude checks a running task with `Get-Content logs/agy-progress.log -Tail 10` and treats it as stuck or on a wrong path when: a tests-only step runs well over ~15 minutes or an implement loop over ~30 minutes without new progress; no new step appears for several minutes; the same file is rewritten again and again; files outside the step are touched (production code in a tests step); or the loop repeats the same failure (the loop itself stops after 5 identical rounds). Then Claude stops the run, discards its partial changes and resends a corrected prompt.

## 8. Cost pilots (rule 22)
Measures of Claude's own work per chunk, compared between processes. Counts are from the session transcript; "lines read" is the diff and file output Claude looked at during review.

### Baseline: T-003.3 on the old process (2026-10-01)
- Process: Claude writes a tests prompt, runs agy-run.ps1, reviews the test diff, commits; writes an implement prompt, runs agy-impl-loop.ps1, reviews the code diff, removes files agy cannot delete, rebuilds, commits.
- Claude tool calls: about 22 (2 prompts, 4 runs incl. retries, 3 progress checks, 6 review reads, 1 review-fix round trip, 1 rm+rebuild, 5 commit steps incl. 2 blocked by a tool safety check).
- Prompt text written by Claude: about 6,500 characters (tests prompt about 3,500, implement prompt about 3,000) plus a 600-character fix prompt.
- Lines read in review: about 200 (test names and expected statuses, outbox checks, the agy test change, the patch logic, history).
- Retries and incidents: 1 agy abort on a headless command, 1 run killed for low memory, 1 wrong test API caught in review (orExpect).
- Elapsed: about 40 minutes wall time for tests and implementation, mostly agy and Maven.
- Result: correct, green (product 73), two commits (5395aa3 tests, 840013b impl).

### Pilot A: T-005 on the chunk driver (2026-10-01)
- Process: one command `scripts/agy-chunk.ps1 T-005`; Claude read the report, checked the one flagged test change, ran the full build, committed tests and impl, pushed both together.
- Claude tool calls: about 7 (1 run, 1 report read, 1 flag check, 1 build, 1 commit script plus its 2 helper files) versus about 22 in the baseline.
- Prompt text written by Claude: none for the chunk (the templates and the TASKS.md spec were enough) versus about 6,500 characters.
- Lines read in review: about 70 (the report and the flagged test diff) versus about 200.
- Retries and incidents: 1 headless command abort, retried automatically by the driver; no manual retry; no memory kill.
- Elapsed: about 17 minutes (tests step 388 s, implement step 652 s, 2 fix rounds) versus about 40 minutes.
- Result: correct, green (product 78), separate tests and impl commits.
- Verdict: keep pilot A as the standard process. Claude's work per chunk fell to roughly a third.
- Driver issues found: the flag "tests changed in implement step" also counts HISTORY.md (any step-1 file) as a test; only src/test files should count. No stall/time-limit guard yet. Both are small follow-ups for agy.


## 9. Chunk driver (T-007, pilot A)
- Run one chunk with one command from the repo root: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/agy-chunk.ps1 T-005`.
- It reads the task's section from TASKS.md, builds the prompts from `scripts/prompts/tests.txt` and `implement.txt` (all standing rules live there; fix a lesson once in the template), runs the tests step (retries once on the headless command abort), stops if production code changed in the tests step, runs the implement-until-green loop, and writes `logs/agy-report.md`: step files split into tests and production, tests changed during implement, GREEN or STUCK, test summaries, fix rounds, files agy asks to remove, flags. Exit code 0 only when green with no flags.
- Claude then: reads the report, reads the test files in full (the owner reviews them), spot-checks flagged items, removes listed files with git, runs `cd service && mvn test`, commits tests first and implementation second (rule 20), pushes both only when green (rule 3).
- Known gap: no stall or overall time-limit guard yet; each agy call keeps its 25 minute timeout and the loop stops after 5 identical failures.
