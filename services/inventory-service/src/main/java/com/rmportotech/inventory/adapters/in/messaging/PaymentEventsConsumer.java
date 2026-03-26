package com.rmportotech.inventory.adapters.in.messaging;

import com.rmportotech.inventory.adapters.out.persistence.ProcessedEventEntity;
import com.rmportotech.inventory.adapters.out.persistence.ProcessedEventRepository;
import com.rmportotech.inventory.domain.model.StockReservation;
import com.rmportotech.inventory.domain.ports.StockReservationStore;
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

    private final StockReservationStore stockReservationStore;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentEventsConsumer(StockReservationStore stockReservationStore,
                                 ProcessedEventRepository processedEventRepository) {
        this.stockReservationStore = stockReservationStore;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(
            topics = "${rmpt.kafka.topic.payments}",
            groupId = "inventory-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(String payload,
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

        stockReservationStore.save(StockReservation.create(orderId));
    }
}