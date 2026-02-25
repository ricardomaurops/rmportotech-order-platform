# RM Porto Tech – Order Platform

Cloud Native & Event-Driven Engineering

The RM Porto Tech Order Platform is a cloud-native microservices-based system designed to demonstrate modern distributed architecture patterns, including event-driven communication, Clean Architecture, Domain-Driven Design (DDD), and observability best practices.

---

## 🚀 Purpose

This project demonstrates:

- Microservices architecture
- Event-Driven Architecture using Kafka
- Clean Architecture + DDD principles
- Outbox Pattern for reliable event publishing
- Idempotent consumers
- Observability (logs, metrics, tracing)
- Integration testing with Testcontainers
- Cloud-ready deployment (AWS ECS target)

---

## 🏗 Architecture Overview

### Services

- **Order Service**
- **Payment Service**
- **Inventory Service**
- **Notification Service**

### Communication

- Synchronous: REST (OpenAPI/Swagger)
- Asynchronous: Apache Kafka

### Persistence

- PostgreSQL (Orders, Payments)
- DynamoDB (Event history / Idempotency / Inventory)
- Outbox table for event reliability

---

## 📦 Tech Stack

- Java 21
- Spring Boot 3
- Apache Kafka
- PostgreSQL
- DynamoDB Local
- Docker & Docker Compose
- Micrometer + Prometheus
- OpenTelemetry
- Grafana
- JUnit 5 + Mockito
- Testcontainers

---

## 📊 Key Engineering Practices

- Clean Architecture
- Domain-Driven Design (DDD)
- Outbox Pattern
- Idempotent Event Consumers
- Structured Logging (JSON)
- Distributed Tracing
- Metrics & Monitoring
- CI/CD Ready

---

## 🛠 Running Locally

```bash
docker compose up
