package com.rmportotech.payment.adapters.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "processed_events",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_processed_events_event_id", columnNames = "event_id")
        }
)
public class ProcessedEventEntity {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String consumer; // ex: "payment-service"

    @Column(nullable = false)
    private Instant processedAt;

    protected ProcessedEventEntity() {
    }

    public ProcessedEventEntity(UUID id, UUID eventId, String consumer, Instant processedAt) {
        this.id = id;
        this.eventId = eventId;
        this.consumer = consumer;
        this.processedAt = processedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getConsumer() {
        return consumer;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}