package com.rmportotech.inventory.adapters.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events",
        uniqueConstraints = @UniqueConstraint(columnNames = "eventId"))
public class ProcessedEventEntity {

    @Id
    private UUID id;

    private UUID eventId;

    private String consumer;

    private Instant processedAt;

    protected ProcessedEventEntity() {}

    public ProcessedEventEntity(UUID id, UUID eventId, String consumer, Instant processedAt) {
        this.id = id;
        this.eventId = eventId;
        this.consumer = consumer;
        this.processedAt = processedAt;
    }
}