# SWGT - see when got time

Ideas the owner parked for later (AGENTS.md rule 19). These are NOT tasks: nothing here is built until the owner picks an entry, it is discussed again, moved to TASKS.md, and the owner says go ahead.

Entry format: date, idea, why parked, open questions with Claude's recommendation at the time.

## 2026-10-01 ARCHIVED product status
Idea: a third `ProductStatus` value `ARCHIVED` ("hide but keep"), next to `ACTIVE` and `INACTIVE`.
Why parked: owner wants to think about it; T-003 ships only ACTIVE and INACTIVE.
Open questions (recommendation in brackets):
- Can an archived product come back? (No: ARCHIVED is final.)
- Can an archived product still be edited (title, price, ...)? (No: any PATCH on it is rejected.)
- Response for a forbidden change, e.g. ARCHIVED -> ACTIVE or editing an archived product? (409 Conflict with a clear message; needs a shared exception such as InvalidStateException mapped to 409 in GlobalExceptionHandler.)
- How is a product archived? (Same PATCH /products/{id} with "status": "ARCHIVED".)
- What happens to DELETE /products/{id}? (Keep it as a real delete emitting DELETE; archive is the soft alternative.)
Proposed transitions at the time: INACTIVE <-> ACTIVE; INACTIVE -> ARCHIVED; ACTIVE -> ARCHIVED; ARCHIVED -> nothing.