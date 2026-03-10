# Requisitos

## Requisitos Funcionais

| ID    | Descrição                                                               | Status         |
|-------|-------------------------------------------------------------------------|----------------|
| FR-01 | Criar pedido via API REST com valor total                               | Implementado   |
| FR-02 | Consultar pedido por ID                                                 | Implementado   |
| FR-03 | Processar pagamento ao receber evento OrderCreated                      | Implementado   |
| FR-04 | Reservar estoque ao receber evento OrderCreated                         | Planejado      |
| FR-05 | Publicar eventos de domínio no Kafka de forma confiável                 | Implementado   |
| FR-06 | Notificar status do pedido ao cliente                                   | Planejado      |

---

## Requisitos Não-Funcionais

| ID     | Descrição                                                               | Status         |
|--------|-------------------------------------------------------------------------|----------------|
| NFR-01 | Comunicação assíncrona entre serviços via Apache Kafka                  | Implementado   |
| NFR-02 | Consumo idempotente de eventos (sem processamento duplicado)            | Implementado   |
| NFR-03 | Outbox Pattern para publicação confiável de eventos                     | Implementado   |
| NFR-04 | Logs estruturados em JSON com correlação por eventId                    | Implementado   |
| NFR-05 | Rastreamento distribuído via OpenTelemetry                              | Configurado    |
| NFR-06 | Exposição de métricas no formato Prometheus                             | Implementado   |
| NFR-07 | Testes de integração com Testcontainers                                 | Planejado      |
| NFR-08 | Serviços containerizados e prontos para cloud (AWS ECS)                 | Em andamento   |

---

## Decisões de Design Relacionadas

- **NFR-02 → Outbox Pattern**: eventos são gravados na mesma transação do domínio, evitando perda em caso de falha
- **NFR-02 → processed_events**: constraint `UNIQUE(event_id)` impede que mensagens reentregues pelo Kafka gerem processamento duplo
- **NFR-01 → ACK Manual**: o offset do Kafka só é confirmado após processamento bem-sucedido, garantindo at-least-once
- **NFR-03 + NFR-04**: logs incluem eventId dos headers Kafka para correlação entre serviços
