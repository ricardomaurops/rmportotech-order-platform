package com.rmportotech.orders.adapters.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateType; // "Order"

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String eventType; // "OrderCreated"

    @Column(nullable = false, columnDefinition = "text")
    private String payload; // JSON

    @Column(nullable = false)
    private String status; // PENDING / SENT

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant sentAt;

    protected OutboxEventEntity() {}

    public OutboxEventEntity(UUID id, String aggregateType, UUID aggregateId, String eventType,
                             String payload, String status, Instant createdAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSentAt() { return sentAt; }

    public void markSent() {
        this.status = "SENT";
        this.sentAt = Instant.now();
    }
}