package com.rmportotech.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;

public class StockReservation {

    private final UUID id;
    private final UUID orderId;
    private final Instant createdAt;

    public StockReservation(UUID id, UUID orderId, Instant createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}