package com.rmportotech.payment.adapters.in.messaging;

import com.rmportotech.payment.adapters.out.persistence.*;
import com.rmportotech.payment.domain.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class OrderEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsConsumer.class);

    private static final String CONSUMER_NAME = "payment-service";

    private final PaymentJpaRepository paymentRepository;
    private final ProcessedEventJpaRepository processedEventRepository;
    private final OutboxJpaRepository outboxRepository;

    public OrderEventsConsumer(PaymentJpaRepository paymentRepository,
                               ProcessedEventJpaRepository processedEventRepository, OutboxJpaRepository outboxRepository) {
        this.paymentRepository = paymentRepository;
        this.processedEventRepository = processedEventRepository;
        this.outboxRepository = outboxRepository;
    }

    @KafkaListener(
            topics = "${rmpt.kafka.topic.orders:orders.events}",
            groupId = "payment-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(String payload,
                          @Header(name = "eventId", required = false) byte[] eventIdHeader,
                          @Header(name = "kafka_receivedMessageKey", required = false) String key,
                          Acknowledgment ack) {

        UUID eventId = resolveEventId(eventIdHeader);
        UUID orderId = (key != null) ? UUID.fromString(key) : extractOrderIdFallback(payload);

        try {
            processOnce(eventId, orderId, payload);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("PAYMENT failed processing eventId={} orderId={}", eventId, orderId, ex);
            throw ex; // sem ack -> reprocessa
        }
    }

    @Transactional
    protected void processOnce(UUID eventId, UUID orderId, String payload) {
        try {
            // 1) tenta “marcar” o evento como processado (idempotência)
            processedEventRepository.save(new ProcessedEventEntity(
                    UUID.randomUUID(),
                    eventId,
                    CONSUMER_NAME,
                    Instant.now()
            ));
        } catch (DataIntegrityViolationException dup) {
            // unique constraint estourou -> já processado
            log.info("PAYMENT duplicate event ignored eventId={} orderId={}", eventId, orderId);
            return;
        }

        // 2) processa “de verdade”
        // (por enquanto aprova sempre; depois você fará regra/integração)
        PaymentEntity payment = new PaymentEntity(
                UUID.randomUUID(),
                orderId,
                new BigDecimal("100.00"),
                PaymentStatus.APPROVED,
                Instant.now()
        );

        paymentRepository.save(payment);

        String eventType = "PaymentApproved";

        outboxRepository.save(new OutboxEventEntity(
                UUID.randomUUID(),
                "PAYMENT",
                payment.getId(),
                eventType,
                payload,
                "PENDING",
                Instant.now()
        ));

        log.info("PAYMENT processed eventId={} saved paymentId={} orderId={}",
                eventId, payment.getId(), orderId);

    }

    private UUID resolveEventId(byte[] header) {
        if (header == null || header.length == 0) {
            // fallback: gera um UUID novo (não ideal) — mas evita crash
            // O ideal é SEMPRE vir do header.
            return UUID.randomUUID();
        }
        return UUID.fromString(new String(header, StandardCharsets.UTF_8));
    }

    private UUID extractOrderIdFallback(String payload) {
        // fallback “best effort” (não é o caminho ideal)
        // Você pode remover depois que estiver tudo com key/header ok.
        String maybeUuid = payload.replaceAll("[^0-9a-fA-F-]", "");
        return UUID.fromString(maybeUuid);
    }
}