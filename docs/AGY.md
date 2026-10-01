# Antigravity CLI (`agy`) – setup and usage notes

Researched 2026-10-01. Read this instead of re-investigating.

## Why `agy` and not `gemini`
- The old Gemini CLI (`@google/gemini-cli` 0.62.0, installed globally via npm) fails Google sign-in with:
  "This client is no longer supported for Gemini Code Assist for individuals ... migrate to the Antigravity suite".
  Do not try to fix that login. Use `agy` instead.
- `agy` is the Antigravity CLI and is the planned **Executor** (see `AGENTS.md` Roles).

## Where it lives
- Binary: `C:\Users\Compro\AppData\Local\agy\bin\agy.exe` (v1.2.14). **Not on PATH** – call by full path
  (Git Bash: `/c/Users/Compro/AppData/Local/agy/bin/agy.exe`).
- Config/state: `~/.gemini/antigravity-cli/` (`settings.json`, `cli.log`, `history.jsonl`, conversations).
- `antigravity` on PATH is the IDE launcher (VS Code style flags only). It cannot take tasks. Ignore it.
- Login is already done. Verified headless: `agy.exe -p "Reply with exactly: pong. Do not use any tools."` -> `pong`.
- `~/.gemini/antigravity-cli/settings.json` only pre-allows `command(Get-ChildItem)`; any other shell command prompts
  unless a permission flag/mode is passed.

## Options
Run a prompt:
- `-p` / `--print` / `--prompt "<text>"` – one prompt, non-interactive, prints the answer (the loop uses this).
- `-i` / `--prompt-interactive` – start with a prompt then stay interactive.
- `--print-timeout <dur>` – time limit in print mode (default 0 = wait for the turn to finish).
- `--output-format text|json|stream-json` (default text).
- `--input-format stream-json` – NDJSON per line on stdin, one turn each; needs `--output-format stream-json`.
- `--json-schema <schema|file>` – force structured output (e.g. a done/blocked/files-changed report).

Continue work:
- `-c` / `--continue` – continue the most recent conversation (use to send review feedback back).
- `--conversation <id>` – resume a specific conversation (safer than `-c` if other sessions exist).

Control and safety:
- `--mode accept-edits|plan` – accept-edits auto-approves file edits only; plan is read-only.
- `--dangerously-skip-permissions` – auto-approve every tool call. Only on a throwaway `gemini/<task-id>` branch.
- `--sandbox` – terminal restrictions on.
- `--model <id>`, `--effort low|medium|high|max`, `--add-dir <dir>` (repeatable).
- Others: `--agent`, `--project`, `--new-project`, `--log-file`, `--disable-slash-commands`, `--remote-control`.

Subcommands: `models`, `agents`, `mcp` (add/remove/list/enable/disable), `plugin`, `update`, `install`,
`changelog`, `remote-control`, `mic-serve`, `help`.

## Models (from `agy models`)
`gemini-3.8-flash-{high,medium,low}`, `gemini-3.7-flash-{high,medium,low}`, `gemini-3.6-flash-{high,medium,low}`,
`gemini-3.1-pro-{high,low}`, `claude-sonnet-4-6`, `claude-opus-4-6-thinking`, `gpt-oss-120b-medium`.
Prefer a Gemini model for the Executor so the Claude review stays independent.

## Recommended executor command
```bash
cd <repo root>   # run from the repo so AGENTS.md is in scope
/c/Users/Compro/AppData/Local/agy/bin/agy.exe -p "<task prompt: follow AGENTS.md, do task <id>>" \
  --mode accept-edits --model gemini-3.1-pro-high --output-format json --print-timeout 20m
```
If it hangs on shell approvals (e.g. `mvn test`), rerun with `--dangerously-skip-permissions` on the task branch.

## Automated loop (Claude drives it)
1. Read the next open task in `TASKS.md`; create branch `gemini/<task-id>`.
2. Run `agy -p` with the task prompt (above).
3. Run `cd service && mvn test`, `scripts/check-context-sync.sh`, and read the diff.
4. On failure or rule breaks (tests not first, missing HISTORY entry, stale CONTEXT.md), send findings back with
   `agy -c -p "..."` (or `--conversation <id>`). Max ~3 rounds.
5. On success, write the review verdict in `HISTORY.md` and **stop**. Never merge: only the owner saying
   "merge" / "approved" authorizes it (AGENTS.md rule 9).

## Prompt preamble (always start the agy prompt with this)
"Read AGENTS.md (all rules 1-15), docs/LEARNING.md, TASKS.md task <id>, and for each module you change its CONTEXT.md and recent HISTORY.md, before doing anything. Follow every rule. Do not commit unless told; do not touch files outside the task."
`agy` is not known to load `AGENTS.md` by itself, so the preamble is mandatory.

## Permissions
- `--mode accept-edits` auto-denies shell commands in headless mode (so `mvn test` fails).
- `--dangerously-skip-permissions` works but Claude Code's safety classifier blocked launching it (2026-10-01).
  Needs an explicit owner decision; alternative is allow-rules in `~/.gemini/antigravity-cli/settings.json`
  `permissions.allow`, e.g. `command(mvn)`, `command(git)` (format seen: `command(Get-ChildItem)`).

## Verified behaviour (T-001 / T-002 trial, 2026-10-01)
- Working flow: agy edits files; Claude (or the driver script) runs `mvn test` and feeds failures back with `--continue`. Headless agy can run a command only if its EXACT string is in `permissions.allow` AND agy was started from a shell with the refreshed PATH (otherwise it prefixes a PATH-refresh line and the string no longer matches).
- Launch from PowerShell: refresh `$env:Path` from Machine+User, set `JAVA_HOME`, `Set-Location` to the repo, then `agy.exe -p <prompt> --mode accept-edits --model gemini-3.1-pro-high --print-timeout 25m` (add `--continue` for follow-ups).
- Prompt gotchas: no double quotes and no non-ASCII (en dash) in prompts, Windows mangles the argument ("unexpected argument"). Put the prompt in a file and read it with `Get-Content -Raw`.
- agy will sometimes call the run-command tool just to explore; a denied command aborts the whole run with no output. Start every prompt with: use only file view/search/write tools, never run-command, no subagents.
- agy may start a research subagent and return early ("I have sent a subagent..."). Resend with `--continue`: do the research yourself, in this turn.
- agy follows AGENTS.md when told to read it, writes tests first and comments each step, and keeps hexagonal layers. It misses things a reviewer must catch (wrong method name in a test, tautological assertions on a stubbed result, missing @NonNull field in a builder). Two of those tests were corrected by Claude, one by agy in the loop with an explicit rule 8 note.
- agy forgot the CONTEXT/HISTORY paperwork when the first prompt failed; check `git status` for those files after each chunk.
- Driver loop idea: implement, `mvn test`, send failures back, repeat until green (no retry limit; stop only if the same failure repeats 5 times).

## Unverified (check on the first real task)
- Whether `agy` automatically reads `AGENTS.md` / `GEMINI.md`. If not, put "read AGENTS.md first" in the prompt
  or add the rules file format it expects.
- Whether `--mode accept-edits` blocks `mvn test` and other shell commands.
