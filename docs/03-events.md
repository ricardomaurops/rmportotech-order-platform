# Contratos de Eventos

Todos os eventos Kafka do sistema seguem a mesma estrutura de cabeçalhos:

**Headers obrigatórios:**
```
eventId:   <UUID>           ← chave de idempotência
eventType: <string>         ← tipo do evento
```

**Chave da mensagem (message key):** ID do agregado de origem (ex: orderId, paymentId)

---

## OrderCreated

Publicado pelo `order-service` ao criar um novo pedido.

**Tópico:** `orders.events`
**Chave:** `orderId` (UUID)

**Headers:**
```
eventId:   550e8400-e29b-41d4-a716-446655440001
eventType: OrderCreated
```

**Payload (JSON):**
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "totalAmount": 150.00
}
```

**Consumidores:**
- `payment-service` (group-id: `payment-service`)
- `inventory-service` (planejado)

---

## PaymentApproved

Publicado pelo `payment-service` ao aprovar um pagamento.

**Tópico:** `payments.events`
**Chave:** `paymentId` (UUID)

**Headers:**
```
eventId:   550e8400-e29b-41d4-a716-446655440002
eventType: PaymentApproved
```

**Payload (JSON):**
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "paymentId": "550e8400-e29b-41d4-a716-446655440003",
  "approvedAt": "2025-01-10T12:00:05Z"
}
```

**Consumidores:**
- `inventory-service` (planejado)
- `notification-service` (planejado)

---

## PaymentRejected

Publicado pelo `payment-service` ao rejeitar um pagamento (a implementar).

**Tópico:** `payments.events`

**Payload (JSON):**
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "reason": "INSUFFICIENT_FUNDS"
}
```

---

## StockReserved

Publicado pelo `inventory-service` (planejado).

**Tópico:** `rmpt.inventory.v1.stock-reserved`

**Payload:**
```json
{
  "orderId": "...",
  "reservationId": "..."
}
```

---

## StockFailed

Publicado pelo `inventory-service` quando não há estoque disponível (planejado).

**Tópico:** `rmpt.inventory.v1.stock-failed`

**Payload:**
```json
{
  "orderId": "...",
  "reason": "OUT_OF_STOCK"
}
```

---

## Notas de Implementação

### Idempotência
O campo `eventId` do header é a chave de idempotência. Cada consumidor deve registrar o `eventId` em uma tabela `processed_events` antes de processar o evento. Se o INSERT falhar por `UNIQUE constraint violation`, a mensagem já foi processada e deve ser descartada com ack.

### Serialização
Todos os payloads são serializados como **JSON string** (StringSerializer/StringDeserializer). Não há uso de Avro ou Schema Registry no momento.

### Versionamento
Os tópicos do order-service e payment-service ainda não possuem versionamento no nome (`orders.events`, `payments.events`). Os tópicos de inventory e notification seguirão o padrão `rmpt.{domain}.v1.{event-type}`.
