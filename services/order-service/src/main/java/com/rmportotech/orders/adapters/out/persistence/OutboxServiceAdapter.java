package com.rmportotech.orders.adapters.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmportotech.orders.application.usecase.OutboxService;
import com.rmportotech.orders.domain.model.Order;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxServiceAdapter implements OutboxService {

    private final OutboxJpaRepository repo;
    private final ObjectMapper objectMapper;

    public OutboxServiceAdapter(OutboxJpaRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @Override
    public void enqueueOrderCreated(Order order) {
        try {
            var payload = objectMapper.writeValueAsString(new OrderCreatedPayload(order.getId().toString(), order.getTotalAmount()));
            repo.save(new OutboxEventEntity(
                    UUID.randomUUID(),
                    "Order",
                    order.getId(),
                    "OrderCreated",
                    payload,
                    "PENDING",
                    Instant.now()
            ));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }

    public record OrderCreatedPayload(String orderId, java.math.BigDecimal totalAmount) {}
}