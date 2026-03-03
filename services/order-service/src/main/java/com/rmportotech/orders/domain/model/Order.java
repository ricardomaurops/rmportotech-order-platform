package com.rmportotech.orders.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Order {

    private final UUID id;
    private final BigDecimal totalAmount;
    private final OrderStatus status;
    private final Instant createdAt;

    public Order(UUID id, BigDecimal totalAmount, OrderStatus orderStatus, Instant createdAt) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.status = orderStatus;
        this.createdAt = createdAt;
    }

    public static Order create(BigDecimal totalAmount) {
        return new Order(UUID.randomUUID(), totalAmount, OrderStatus.CREATED, Instant.now());
    }

    public UUID getId() { return id; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
