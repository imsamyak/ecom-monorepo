# contract module

The contract for events published through the outbox. Dependency free on purpose.

## `DomainEvent`
One abstract method, `String aggregateId()`. An event is a record that implements it:
- the **record name is the aggregate type** (a record named `Product` describes the Product aggregate); nothing to configure;
- the record chooses which attribute identifies the aggregate and **converts it to a String itself** (`id.toString()`, `String.valueOf(number)`, or a composite such as `customerId + ":" + region`), so the compiler enforces the type;
- the aggregate id is the partition key: events with the same id must be consumed in order. Pick the entity whose events must stay ordered relative to each other (for product events, the product id).

`Outbox.of(DomainEvent)` in the outbox module builds the outbox row from it and rejects a null or blank id, anonymous classes and lambdas.

## Test
`cd service && mvn test` (DomainEventTest pins the one-method, String-returning contract).
