# Inbox Module Context

This module receives broker events, deduplicates them, and dispatches them to local `@EventListener` methods.

## Delivery Rules
Based on the T-008 design:
1. Only one outbox relay runs on the publisher side.
2. The publisher never reorders or repeats on its own.
3. The inbox processes and acknowledges one message at a time.
Under these rules, a duplicate is always the immediate repeat of the last event of that aggregate.

## Deduplication and Storage
- Deduplication is based on the `last_event_id` per `aggregate_type` and `aggregate_id`.
- The storage is behind a port `InboxStore` so that different adapters (e.g., DynamoDB) can be added later.
- The default adapter uses JPA in the service's own database (`inbox_last_event` table).
- Deduplication rows are overwritten on each new event for the same aggregate.

## TTL Cleaner
- An hourly cleaner deletes rows whose `processed_at` is older than `inbox.dedupe.ttl` (defaults to 7 days).
