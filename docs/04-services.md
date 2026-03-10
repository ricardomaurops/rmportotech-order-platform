# Guia Técnico dos Serviços

---

## order-service

**Porta:** 8081
**Banco:** PostgreSQL – database `rmpt`
**Pacote base:** `com.rmportotech.orders`

### Responsabilidade

Gerencia o ciclo de vida dos pedidos. Expõe API REST para criação e consulta, e publica eventos de domínio no Kafka via Outbox Pattern.

---

### Estrutura de Pacotes

```
adapters/
  in/
    web/
      OrderController.java          → POST /orders, GET /orders/{id}
      ApiExceptionHandler.java      → tratamento global de exceções
      dto/
        CreateOrderRequest.java     → { totalAmount: BigDecimal }
        CreateOrderResponse.java    → { id, totalAmount, status, createdAt }
        OrderResponse.java
  out/
    persistence/
      OrderEntity.java              → @Entity para tabela orders
      OrderJpaRepository.java       → Spring Data JPA
      OrderRepositoryAdapter.java   → implementa OrderRepository (porta)
      OutboxEventEntity.java        → @Entity para tabela outbox_event
      OutboxJpaRepository.java      → query findByStatus
      OutboxServiceAdapter.java     → implementa OutboxService (porta)

application/
  usecase/
    CreateOrderUseCase.java         → salva pedido + enfileira evento
    GetOrderByIdUseCase.java        → consulta por ID
    OutboxService.java              → interface (porta de saída)

domain/
  model/
    Order.java                      → entidade imutável de domínio
    OrderStatus.java                → enum: CREATED
  ports/
    OrderRepository.java            → interface (porta de saída)

infrastructure/
  config/
    KafkaProducerConfig.java        → bean KafkaTemplate
    OpenApiConfig.java              → configuração Swagger
    UseCaseConfig.java              → injeção de dependência dos use cases
  outbox/
    OutboxPublisherJob.java         → @Scheduled a cada 2s, publica eventos PENDING
```

---

### Modelo de Domínio

```
Order
├── id: UUID
├── totalAmount: BigDecimal
├── status: OrderStatus (CREATED)
└── createdAt: Instant
```

---

### Tabelas

**orders**
| Coluna        | Tipo             | Restrição   |
|---------------|------------------|-------------|
| id            | UUID             | PRIMARY KEY |
| total_amount  | DECIMAL(19,2)    | NOT NULL    |
| status        | VARCHAR          | NOT NULL    |
| created_at    | TIMESTAMP        | NOT NULL    |

**outbox_event**
| Coluna         | Tipo          | Restrição   |
|----------------|---------------|-------------|
| id             | UUID          | PRIMARY KEY |
| aggregate_type | VARCHAR       | NOT NULL    |
| aggregate_id   | UUID          | NOT NULL    |
| event_type     | VARCHAR       | NOT NULL    |
| payload        | TEXT (JSON)   | NOT NULL    |
| status         | VARCHAR       | NOT NULL    |
| created_at     | TIMESTAMP     | NOT NULL    |
| sent_at        | TIMESTAMP     | nullable    |

> `status` transita de `PENDING` para `SENT` após publicação no Kafka.

---

### Fluxo: Criar Pedido

```
POST /orders { totalAmount }
  └─ CreateOrderUseCase.execute(totalAmount)
       ├─ Order order = new Order(UUID.randomUUID(), totalAmount, CREATED, now())
       ├─ orderRepository.save(order)              → INSERT em orders
       └─ outboxService.enqueueOrderCreated(order) → INSERT em outbox_event (PENDING)

OutboxPublisherJob (a cada 2s)
  ├─ SELECT * FROM outbox_event WHERE status='PENDING'
  └─ Para cada evento:
       ├─ kafkaTemplate.send(topic, key, payload).get()  → aguarda ACK do broker
       └─ UPDATE outbox_event SET status='SENT', sent_at=now()
```

---

### Configuração Kafka (Produtor)

```yaml
spring.kafka:
  bootstrap-servers: 127.0.0.1:9092
  producer:
    key-serializer: StringSerializer
    value-serializer: StringSerializer
    acks: all
    properties:
      enable.idempotence: true
      max.in.flight.requests.per.connection: 1
```

- `acks=all` — aguarda confirmação de todas as réplicas
- `enable.idempotence=true` — garante exatamente uma vez na entrega ao broker
- `max.in.flight.requests.per.connection=1` — preserva ordem das mensagens

---

### API REST

| Método | Endpoint       | Descrição         | Status HTTP          |
|--------|----------------|-------------------|----------------------|
| POST   | /orders        | Cria um pedido    | 201 Created          |
| GET    | /orders/{id}   | Consulta pedido   | 200 OK / 404         |

Swagger UI disponível em: `http://localhost:8081/swagger-ui`

---

---

## payment-service

**Porta:** 8083
**Banco:** PostgreSQL – database `rmpt_payment`
**Pacote base:** `com.rmportotech.payment`

### Responsabilidade

Processa pagamentos ao receber eventos `OrderCreated` do Kafka. Implementa idempotência e publica eventos `PaymentApproved` via Outbox Pattern.

---

### Estrutura de Pacotes

```
adapters/
  in/
    messaging/
      OrderEventsConsumer.java      → @KafkaListener no tópico orders.events
  out/
    persistence/
      PaymentEntity.java            → @Entity para tabela payments
      PaymentJpaRepository.java     → Spring Data JPA
      ProcessedEventEntity.java     → @Entity para tabela processed_events
      ProcessedEventJpaRepository.java
      OutboxEventEntity.java        → @Entity para tabela outbox_event
      OutboxJpaRepository.java

domain/
  model/
    Payment.java                    → entidade de domínio
    PaymentStatus.java              → enum: APPROVED, REJECTED

infrastructure/
  config/
    KafkaConsumerConfig.java        → factory com ACK manual
  outbox/
    OutboxPublisherJob.java         → publica PaymentApproved no Kafka
```

---

### Modelo de Domínio

```
Payment
├── id: UUID
├── orderId: UUID
├── amount: BigDecimal
├── status: PaymentStatus (APPROVED | REJECTED)
└── createdAt: Instant
```

---

### Tabelas

**payments**
| Coluna      | Tipo          | Restrição   |
|-------------|---------------|-------------|
| id          | UUID          | PRIMARY KEY |
| order_id    | UUID          | NOT NULL    |
| amount      | DECIMAL(19,2) | NOT NULL    |
| status      | VARCHAR       | NOT NULL    |
| created_at  | TIMESTAMP     | NOT NULL    |

**processed_events** (idempotência)
| Coluna       | Tipo      | Restrição         |
|--------------|-----------|-------------------|
| id           | UUID      | PRIMARY KEY       |
| event_id     | UUID      | NOT NULL, UNIQUE  |
| consumer     | VARCHAR   | NOT NULL          |
| processed_at | TIMESTAMP | NOT NULL          |

> A constraint `UNIQUE(event_id)` é o mecanismo central de idempotência.

**outbox_event** — mesma estrutura do order-service.

---

### Fluxo: Processar Evento OrderCreated

```
Mensagem chega no tópico orders.events
  └─ OrderEventsConsumer.onMessage(payload, headers, ack)
       │
       ├─ 1. Extrai eventId do header (fallback: UUID aleatório)
       ├─ 2. Extrai orderId da chave da mensagem ou do payload
       │
       ├─ 3. Tenta INSERT em processed_events(eventId)
       │       ├─ DataIntegrityViolationException → duplicata → ack + return
       │       └─ Sucesso → continua
       │
       ├─ 4. Cria PaymentEntity (status=APPROVED*)
       ├─ 5. Salva em payments
       ├─ 6. Enfileira PaymentApproved em outbox_event
       ├─ 7. ack.acknowledge()  ← offset confirmado no Kafka
       └─ 8. Log de sucesso

* Lógica de aprovação/rejeição a ser implementada futuramente

OutboxPublisherJob (a cada 2s)
  └─ Publica eventos PENDING no tópico payments.events
```

---

### Configuração Kafka (Consumidor)

```yaml
spring.kafka:
  bootstrap-servers: 127.0.0.1:9092
  consumer:
    group-id: payment-service
    auto-offset-reset: earliest
    enable-auto-commit: false
    key-deserializer: StringDeserializer
    value-deserializer: StringDeserializer
  listener:
    ack-mode: manual
```

- `enable-auto-commit: false` + `ack-mode: manual` — o offset só é confirmado após processamento bem-sucedido
- Em caso de erro (exceção não tratada), a mensagem é reentregue pelo Kafka

---

### Garantias de Confiabilidade

| Risco                              | Solução                                              |
|------------------------------------|------------------------------------------------------|
| Falha antes do ack                 | Kafka reenvia → idempotência via processed_events    |
| Falha após salvar pagamento        | INSERT em processed_events falha na reentrega        |
| Evento duplicado (at-least-once)   | UNIQUE constraint em event_id bloqueia duplicata     |
| Falha na publicação do PaymentApproved | Outbox Pattern garante reenvio pelo job          |
