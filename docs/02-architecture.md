# Architecture

## Architectural Style

- Microservices
- Event-Driven Architecture
- Clean Architecture
- Domain-Driven Design

---

## Communication Model

### Synchronous
REST APIs exposed by services

### Asynchronous
Kafka topics used for domain events

---

## Data Ownership

Each service owns its database.

- Order Service → PostgreSQL
- Payment Service → PostgreSQL
- Inventory Service → DynamoDB
- Notification Service → NoSQL or in-memory

---

## Reliability

- Outbox Pattern for event publishing
- Idempotency via processed events table
- Retry and DLQ strategy (future AWS integration)

---

## Observability

- Structured JSON logs
- Prometheus metrics
- OpenTelemetry tracing
- Grafana dashboards
