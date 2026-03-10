# Visão do Projeto

## Objetivo

O **RM Porto Tech Order Platform** demonstra uma arquitetura de microsserviços de nível produção baseada em práticas modernas de engenharia de software.

O sistema simula um fluxo de processamento de pedidos distribuído, onde cada etapa é tratada por um serviço independente que se comunica via eventos.

---

## Motivações

Este projeto foi criado como:

- **Portfólio técnico** — evidência prática de domínio de arquitetura distribuída
- **Referência arquitetural** — modelo de como aplicar padrões como Outbox e Idempotência
- **Laboratório de engenharia** — ambiente para experimentar tecnologias e padrões

---

## O que o projeto demonstra

| Área               | Prática                                                       |
|--------------------|---------------------------------------------------------------|
| Arquitetura        | Clean Architecture, Hexagonal (Ports & Adapters), DDD        |
| Comunicação        | Event-Driven com Apache Kafka, REST com OpenAPI/Swagger       |
| Confiabilidade     | Outbox Pattern, consumidores idempotentes, ACK manual         |
| Persistência       | Database por serviço (PostgreSQL), DynamoDB para inventário   |
| Observabilidade    | Logs JSON, métricas Prometheus, rastreamento OpenTelemetry    |
| Testes             | JUnit 5, Mockito, Testcontainers (integração real)            |
| Cloud-ready        | Containers Docker, target de deploy: AWS ECS                  |

---

## Escopo atual (implementado)

- order-service: criação de pedidos via REST + publicação de eventos via Outbox
- payment-service: consumo idempotente de eventos + criação de pagamentos + publicação de PaymentApproved

## Roadmap

- inventory-service: reserva de estoque com DynamoDB
- notification-service: notificações de status do pedido
- Saga pattern (orquestração ou coreografia) para o fluxo completo
- Testes de integração com Testcontainers
- Deploy na AWS (ECS + MSK + RDS)
