package com.rmportotech.inventory.adapters.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_reservations")
public class StockReservationEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private Instant createdAt;

    protected StockReservationEntity() {
    }

    public StockReservationEntity(UUID id, UUID orderId, Instant createdAt) {
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