# Guia de Desenvolvimento Local

---

## Pré-requisitos

| Ferramenta        | Versão mínima  | Verificar com          |
|-------------------|----------------|------------------------|
| Java              | 21             | `java -version`        |
| Maven             | 3.9+           | `mvn -version`         |
| Docker            | 24+            | `docker --version`     |
| Docker Compose    | v2             | `docker compose version` |

---

## 1. Clonar o repositório

```bash
git clone https://github.com/rmportotech/order-platform.git
cd order-platform
```

---

## 2. Subir a infraestrutura

```bash
docker compose -f infra/local/docker-compose.yml up -d
```

Containers iniciados:

| Container              | Serviço            | Porta(s)           |
|------------------------|--------------------|--------------------|
| `rmpt-kafka`           | Apache Kafka       | 9092               |
| `rmpt-postgres`        | PostgreSQL 16      | 5432               |
| `rmpt-dynamodb`        | DynamoDB Local     | 8000               |
| `rmpt-otel-collector`  | OpenTelemetry      | 4317, 4318, 8889   |
| `rmpt-prometheus`      | Prometheus         | 9090               |
| `rmpt-grafana`         | Grafana            | 3000               |
| `rmpt-tempo`           | Tempo (tracing)    | 3200               |
| `rmpt-loki`            | Loki (logs)        | 3100               |

Aguardar todos os health checks passarem:

```bash
docker compose -f infra/local/docker-compose.yml ps
```

---

## 3. Criar os bancos de dados

O PostgreSQL já cria o database `rmpt` automaticamente. O `rmpt_payment` precisa ser criado manualmente:

```bash
docker exec -it rmpt-postgres psql -U rmpt -c "CREATE DATABASE rmpt_payment;"
```

Verificar:

```bash
docker exec -it rmpt-postgres psql -U rmpt -c "\l"
```

---

## 4. Rodar os serviços

### order-service

```bash
cd services/order-service
mvn spring-boot:run
```

O Spring vai criar as tabelas automaticamente (`hibernate.ddl-auto: update`) no database `rmpt`.

Verificar: `http://localhost:8081/actuator/health`

### payment-service

```bash
cd services/payment-service
mvn spring-boot:run
```

Tabelas criadas automaticamente no database `rmpt_payment`.

Verificar: `http://localhost:8083/actuator/health`

---

## 5. Testar o fluxo completo

### Criar um pedido

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"totalAmount": 150.00}'
```

Resposta esperada:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "totalAmount": 150.00,
  "status": "CREATED",
  "createdAt": "2025-01-10T12:00:00Z"
}
```

### Consultar o pedido

```bash
curl http://localhost:8081/orders/{id}
```

### Verificar pagamento criado no banco

```bash
docker exec -it rmpt-postgres psql -U rmpt -d rmpt_payment -c "SELECT * FROM payments;"
```

### Verificar evento outbox publicado

```bash
docker exec -it rmpt-postgres psql -U rmpt -c "SELECT id, event_type, status, sent_at FROM outbox_event;"
```

---

## 6. Swagger UI

Documentação interativa da API do order-service:

```
http://localhost:8081/swagger-ui
```

Spec OpenAPI:

```
http://localhost:8081/v3/api-docs
```

---

## 7. Observabilidade

### Grafana

```
http://localhost:3000
Usuário: admin
Senha:   admin
```

Datasources pré-configurados: Prometheus, Tempo, Loki.

### Prometheus (métricas brutas)

```
http://localhost:9090
```

Exemplo de query: `http_server_requests_seconds_count{service="order-service"}`

### Métricas dos serviços

```bash
curl http://localhost:8081/actuator/prometheus
curl http://localhost:8083/actuator/prometheus
```

---

## 8. Inspecionar o Kafka

Listar tópicos:

```bash
docker exec -it rmpt-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list
```

Consumir mensagens do tópico de pedidos:

```bash
docker exec -it rmpt-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic orders.events \
  --from-beginning \
  --property print.headers=true
```

Consumir mensagens do tópico de pagamentos:

```bash
docker exec -it rmpt-kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic payments.events \
  --from-beginning \
  --property print.headers=true
```

---

## 9. Conectar ao banco de dados

```bash
# order-service database
docker exec -it rmpt-postgres psql -U rmpt -d rmpt

# payment-service database
docker exec -it rmpt-postgres psql -U rmpt -d rmpt_payment
```

Conexão via cliente externo (DBeaver, IntelliJ, etc.):

```
Host:     localhost
Porta:    5432
Usuário:  rmpt
Senha:    rmpt
Database: rmpt (ou rmpt_payment)
```

---

## 10. Parar e limpar o ambiente

```bash
# Parar todos os containers (preserva volumes)
docker compose -f infra/local/docker-compose.yml down

# Parar e remover volumes (apaga dados do banco)
docker compose -f infra/local/docker-compose.yml down -v
```

---

## Problemas Comuns

### Porta 9092 já em uso

O `docker-compose.yml` define tanto o container `kafka` quanto o `redpanda` mapeando a porta 9092. Para ambiente local use apenas um deles — comente o bloco do serviço que não for utilizar no `docker-compose.yml`.

### Tabelas não criadas

Verifique se o profile `local` está ativo:

```yaml
# application.yml
spring.profiles.active: local
```

E confirme que o banco `rmpt_payment` foi criado antes de iniciar o payment-service.

### Kafka consumer não recebendo mensagens

Verifique os logs do payment-service para confirmar que o consumer foi registrado:

```
Assigned partitions: [orders.events-0]
```

Se não aparecer, reinicie o payment-service após confirmar que o Kafka está healthy:

```bash
docker inspect rmpt-kafka --format='{{.State.Health.Status}}'
```
