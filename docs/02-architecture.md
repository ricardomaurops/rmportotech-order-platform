# Arquitetura

## Estilo Arquitetural

- **Microsserviços** — cada serviço tem responsabilidade única, banco próprio e deploy independente
- **Event-Driven Architecture** — comunicação assíncrona via Kafka como canal central
- **Clean Architecture** — separação estrita entre domínio, aplicação e infraestrutura
- **Domain-Driven Design (DDD)** — entidades de domínio imutáveis, use cases como orquestradores

---

## Modelo de Comunicação

### Síncrono (REST)
- Clientes externos interagem com `order-service` via HTTP/REST
- Documentação via OpenAPI/Swagger em `/swagger-ui`
- Respostas com códigos HTTP semânticos (201, 200, 404)

### Assíncrono (Kafka)
- Eventos de domínio trafegam entre serviços via tópicos Kafka
- Produtores usam `acks=all` + `enable.idempotence=true`
- Consumidores usam ACK manual para garantir at-least-once

---

## Topologia de Tópicos

| Tópico            | Produtor          | Consumidor(es)     | Evento               |
|-------------------|-------------------|--------------------|----------------------|
| `orders.events`   | order-service     | payment-service    | OrderCreated         |
| `payments.events` | payment-service   | (futuro)           | PaymentApproved      |

Cabeçalhos obrigatórios em todos os eventos:
- `eventId` — UUID único do evento (chave de idempotência)
- `eventType` — tipo do evento (ex: `"OrderCreated"`)

---

## Ownership de Dados

Cada serviço é dono exclusivo do seu banco. Não há acesso cruzado entre bancos.

| Serviço              | Banco              | Tecnologia   |
|----------------------|--------------------|--------------|
| order-service        | `rmpt`             | PostgreSQL 16|
| payment-service      | `rmpt_payment`     | PostgreSQL 16|
| inventory-service    | (planejado)        | DynamoDB     |
| notification-service | (planejado)        | -            |

---

## Padrões de Confiabilidade

### Outbox Pattern

**Problema:** publicar um evento Kafka e gravar no banco em duas operações separadas pode resultar em inconsistência se uma delas falhar.

**Solução:**
1. Evento gravado na tabela `outbox_event` **na mesma transação** da operação de negócio
2. Job agendado (`@Scheduled` a cada 2s) lê eventos com `status=PENDING` e publica no Kafka
3. Após confirmação do broker (`.get()` síncrono), status é atualizado para `SENT`

Ambos os serviços implementam esse padrão.

### Idempotência no Consumidor

**Problema:** Kafka garante at-least-once, podendo reenviar a mesma mensagem.

**Solução:**
- Tabela `processed_events` com `UNIQUE(event_id)`
- Ao receber mensagem, tenta INSERT com o `eventId` do header
- `DataIntegrityViolationException` → evento já processado → ack + return
- Sucesso → processa normalmente

---

## Estrutura Interna dos Serviços (Hexagonal)

```
adapters/
  in/         ← Entrada: REST controllers, Kafka consumers
  out/        ← Saída: JPA repositories, Kafka producers

application/
  usecase/    ← Casos de uso (orquestram domínio + portas)

domain/
  model/      ← Entidades imutáveis de domínio
  ports/      ← Interfaces (contratos) de saída

infrastructure/
  config/     ← Beans Spring, configurações externas
  outbox/     ← Jobs agendados (@Scheduled)
```

**Regra de dependência:** camadas externas dependem das internas. O domínio não conhece Spring, JPA ou Kafka.

---

## Observabilidade

| Pilar       | Ferramenta              | Endpoint / Porta          |
|-------------|-------------------------|---------------------------|
| Métricas    | Micrometer + Prometheus | `/actuator/prometheus`    |
| Logs        | Loki (JSON estruturado) | porta 3100                |
| Rastreamento| OpenTelemetry + Tempo   | porta 3200                |
| Dashboard   | Grafana                 | http://localhost:3000     |

Tag `service` presente em todas as métricas para filtro por serviço no Grafana.

---

## Diagrama de Fluxo

```
┌──────────┐   POST /orders    ┌───────────────┐
│  Client  │ ───────────────►  │ order-service │
└──────────┘                   └───────┬───────┘
                                       │ INSERT orders
                                       │ INSERT outbox_event (PENDING)
                                       │
                               ┌───────▼───────┐
                               │  PostgreSQL   │
                               │  (rmpt)       │
                               └───────────────┘
                                       │
                               OutboxPublisherJob
                               (a cada 2s)
                                       │
                               ┌───────▼───────┐
                               │     Kafka     │
                               │ orders.events │
                               └───────┬───────┘
                                       │
                               ┌───────▼────────┐
                               │payment-service │
                               └───────┬────────┘
                                       │ INSERT processed_events
                                       │ INSERT payments
                                       │ INSERT outbox_event (PENDING)
                                       │
                               ┌───────▼───────┐
                               │  PostgreSQL   │
                               │ (rmpt_payment)│
                               └───────────────┘
                                       │
                               OutboxPublisherJob
                               (a cada 2s)
                                       │
                               ┌───────▼────────┐
                               │     Kafka      │
                               │payments.events │
                               └────────────────┘
```
