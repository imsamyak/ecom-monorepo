# Task queue

Owner adds tasks here. Executor takes the first `open` task, sets it to `in progress`, then `review` when committed.
Reviewer sets `done` (approved) or back to `in progress` with notes in the module `HISTORY.md`.

Statuses: `open` | `in progress` | `review` | `done`

## Template
```
### T-000 <title>   [open]
Module(s): <paths>
Spec: <what to change and why>
Acceptance: <observable checks; tests that must exist or pass>
```

## Tasks
### T-001 Trim product title and description before saving   [in progress]
Module(s): service/product
Spec: `Product` must strip leading/trailing whitespace from `title` and `description` before it is persisted (insert and update), in the domain `sanitize()` step. A description that is blank after trimming is still stored as null. Chunk is self-contained: no API or schema change.
Acceptance: saving a product with title `"  Shoe  "` stores `"Shoe"`; description `"  nice  "` stores `"nice"`; description `"   "` stores null; updating a product re-applies the same trimming; `cd service && mvn test` passes.
