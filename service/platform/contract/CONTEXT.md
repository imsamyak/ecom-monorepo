# contract module

The contract for events published through the outbox. Dependency free on purpose.

## `DomainEvent`
One abstract method, `String aggregateId()`: the partition key. Events with the same aggregate id are consumed in order.

## Sealed event interfaces (`com.ecom.contract.event`)
- The **interface name is the aggregate type with an `Event` suffix** (`ProductEvent`, `VariantEvent`); each **nested record is an action** (`CREATE`, `UPDATE`, `DELETE` / `ADD`, `REMOVE`). The set of actions is closed (sealed).
- One aggregate id rule per aggregate, as a default method: `ProductEvent` events use the product id; `VariantEvent` events also use the **owning product id** so variant events stay ordered with that product's events.
- Event records carry the data a consumer needs (full snapshot for CREATE/UPDATE/ADD, identifiers or removed data for DELETE/REMOVE).

## `EventEnvelope(aggregate, action, data)`
The JSON shape of every outbox payload: `{"aggregate": "Product", "action": "CREATE", "data": {...event record components...}}`. Consumers parse the envelope first, then `data` by (aggregate, action). Product CREATE/UPDATE events carry the `status` as their last component.

## Rules for new events
Declare a record nested in an aggregate sealed interface; top-level records, anonymous classes and lambdas are rejected by `Outbox.of`.

## Test
`cd service && mvn test`.

## `EventCatalog`
Provides a central directory that resolves an `(aggregate, action)` pair (e.g., `"Product"`, `"CREATE"`) to the specific event record class (e.g., `ProductEvent.CREATE.class`). Used by the inbox to decode envelopes back into domain events.

## Enums
`UserRole` values are only added, never renamed or removed; only truly cross-service concepts go into contract.
