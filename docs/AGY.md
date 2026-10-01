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
