# RM Porto Tech – Order Platform

Plataforma de processamento de pedidos baseada em microsserviços, arquitetura event-driven e práticas de engenharia modernas.

---

## Visão Geral

O **RM Porto Tech Order Platform** é um projeto de portfólio técnico que demonstra padrões de arquitetura distribuída em ambiente de produção, incluindo:

- Microsserviços com Clean Architecture + DDD
- Comunicação assíncrona via Apache Kafka
- Outbox Pattern para publicação confiável de eventos
- Consumidores idempotentes
- Observabilidade completa: logs estruturados, métricas e rastreamento distribuído
- Testes de integração com Testcontainers

---

## Serviços

| Serviço             | Porta  | Descrição                                       | Banco         |
|---------------------|--------|-------------------------------------------------|---------------|
| `order-service`     | 8081   | Criação e consulta de pedidos via REST          | PostgreSQL    |
| `payment-service`   | 8083   | Processamento de pagamentos via eventos Kafka   | PostgreSQL    |
| `inventory-service` | -      | Reserva de estoque (planejado)                  | DynamoDB      |
| `notification-service` | -   | Notificações de status (planejado)              | -             |

---

## Stack Tecnológica

| Categoria        | Tecnologias                                                  |
|------------------|--------------------------------------------------------------|
| Linguagem        | Java 21                                                      |
| Framework        | Spring Boot 3.5.11                                           |
| Mensageria       | Apache Kafka 3.7.1 (modo KRaft, sem Zookeeper)               |
| Banco de dados   | PostgreSQL 16, DynamoDB Local                                |
| Build            | Apache Maven                                                 |
| Observabilidade  | Micrometer, Prometheus, OpenTelemetry, Grafana, Loki, Tempo  |
| Testes           | JUnit 5, Mockito, Testcontainers                             |
| Utilitários      | Lombok, springdoc-openapi 2.6.0 (Swagger)                    |
| Infraestrutura   | Docker Compose                                               |

---

## Fluxo de Eventos

```
POST /orders
     │
     ▼
[order-service]
  Salva pedido no banco
  Enfileira evento OrderCreated na tabela outbox_event
     │
     ▼ (OutboxPublisherJob – a cada 2s)
[Kafka] tópico: orders.events
     │
     ▼
[payment-service]
  Verifica idempotência (processed_events)
  Cria pagamento (status: APPROVED)
  Enfileira evento PaymentApproved na tabela outbox_event
     │
     ▼ (OutboxPublisherJob – a cada 2s)
[Kafka] tópico: payments.events
```

---

## Padrões de Engenharia

### Outbox Pattern
Garante que eventos de domínio nunca se percam, mesmo em caso de falha da aplicação. O evento é persistido na mesma transação do negócio e publicado no Kafka por um job agendado.

### Consumidores Idempotentes
A tabela `processed_events` com constraint `UNIQUE(event_id)` impede o processamento duplicado de mensagens reentregues pelo Kafka.

### Hexagonal Architecture (Ports & Adapters)
Cada serviço é organizado em:
- `adapters/in` — entradas (REST, Kafka consumer)
- `adapters/out` — saídas (JPA, Kafka producer)
- `domain` — entidades e interfaces de negócio
- `application` — casos de uso
- `infrastructure` — configurações e jobs

---

## Rodando Localmente

### Pré-requisitos

- Docker e Docker Compose instalados
- Java 21
- Maven

### 1. Subir a infraestrutura

```bash
docker compose -f infra/local/docker-compose.yml up -d
```

Isso inicializa: Kafka, PostgreSQL, DynamoDB, OpenTelemetry Collector, Prometheus, Grafana, Tempo e Loki.

### 2. Criar os bancos de dados

```bash
# Conectar ao PostgreSQL e criar o banco do payment-service
docker exec -it rmpt-postgres psql -U rmpt -c "CREATE DATABASE rmpt_payment;"
```

### 3. Rodar os serviços

```bash
# Terminal 1 – Order Service
cd services/order-service
mvn spring-boot:run

# Terminal 2 – Payment Service
cd services/payment-service
mvn spring-boot:run
```

### 4. Testar

```bash
# Criar um pedido
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"totalAmount": 99.99}'

# Consultar o pedido
curl http://localhost:8081/orders/{id}
```

---

## Endpoints e Documentação

| Serviço          | URL                                      |
|------------------|------------------------------------------|
| Swagger (orders) | http://localhost:8081/swagger-ui         |
| Actuator (orders)| http://localhost:8081/actuator/health    |
| Grafana          | http://localhost:3000 (admin/admin)      |
| Prometheus       | http://localhost:9090                    |
| Kafka            | localhost:9092                           |
| PostgreSQL       | localhost:5432 (rmpt/rmpt)               |

---

## Documentação Detalhada

| Arquivo                          | Conteúdo                                        |
|----------------------------------|-------------------------------------------------|
| [docs/00-vision.md](docs/00-vision.md)           | Visão e objetivos do projeto          |
| [docs/01-requirements.md](docs/01-requirements.md) | Requisitos funcionais e não-funcionais |
| [docs/02-architecture.md](docs/02-architecture.md) | Decisões arquiteturais                |
| [docs/03-events.md](docs/03-events.md)           | Contratos de eventos Kafka            |
| [docs/04-services.md](docs/04-services.md)       | Guia técnico dos serviços             |
| [docs/05-local-dev.md](docs/05-local-dev.md)     | Guia completo de desenvolvimento local|
