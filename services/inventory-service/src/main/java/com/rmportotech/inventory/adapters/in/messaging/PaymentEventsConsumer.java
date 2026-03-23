package com.rmportotech.inventory.adapters.in.messaging;

import com.rmportotech.inventory.adapters.out.persistence.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentEventsConsumer {

    private final StockReservationRepository reservationRepository;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentEventsConsumer(
            StockReservationRepository reservationRepository,
            ProcessedEventRepository processedEventRepository) {
        this.reservationRepository = reservationRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(
            topics = "${rmpt.kafka.topic.payments}",
            groupId = "inventory-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(
            String payload,
            @Header(name = "eventId") byte[] eventIdHeader,
            @Header(name = "kafka_receivedMessageKey") String key,
            Acknowledgment ack) {

        UUID eventId = UUID.fromString(new String(eventIdHeader, StandardCharsets.UTF_8));
        UUID orderId = UUID.fromString(key);

        processOnce(eventId, orderId);

        ack.acknowledge();
    }

    @Transactional
    protected void processOnce(UUID eventId, UUID orderId) {

        if (processedEventRepository.existsByEventId(eventId)) {
            return;
        }

        processedEventRepository.save(
                new ProcessedEventEntity(
                        UUID.randomUUID(),
                        eventId,
                        "inventory-service",
                        Instant.now()
                )
        );

        reservationRepository.save(
                new StockReservationEntity(
                        UUID.randomUUID(),
                        orderId,
                        Instant.now()
                )
        );
    }
}