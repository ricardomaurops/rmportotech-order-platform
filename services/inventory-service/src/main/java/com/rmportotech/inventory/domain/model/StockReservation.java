package com.rmportotech.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;

public class StockReservation {

    private final UUID reservationId;
    private final UUID orderId;
    private final Instant createdAt;

    public StockReservation(UUID reservationId, UUID orderId, Instant createdAt) {
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.createdAt = createdAt;
    }

    public static StockReservation create(UUID orderId) {
        return new StockReservation(UUID.randomUUID(), orderId, Instant.now());
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}