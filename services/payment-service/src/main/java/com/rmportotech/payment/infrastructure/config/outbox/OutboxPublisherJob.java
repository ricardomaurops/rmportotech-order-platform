package com.rmportotech.payment.infrastructure.outbox;

import com.rmportotech.payment.adapters.out.persistence.OutboxEventEntity;
import com.rmportotech.payment.adapters.out.persistence.OutboxJpaRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class OutboxPublisherJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherJob.class);

    private final OutboxJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${rmpt.kafka.topic.payments:payments.events}")
    private String paymentsTopic;

    public OutboxPublisherJob(OutboxJpaRepository outboxRepository,
                              KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publish() {
        List<OutboxEventEntity> pending = outboxRepository.findPending();

        for (OutboxEventEntity e : pending) {
            try {
                var headers = new RecordHeaders();
                headers.add("eventId", e.getId().toString().getBytes(StandardCharsets.UTF_8));
                headers.add("eventType", e.getEventType().getBytes(StandardCharsets.UTF_8));

                var key = e.getAggregateId().toString();
                var record = new ProducerRecord<String, String>(
                        paymentsTopic, null, key, e.getPayload(), headers
                );

                kafkaTemplate.send(record).get();

                e.markSent();
                log.info("PAYMENT outbox sent eventId={} type={}", e.getId(), e.getEventType());

            } catch (Exception ex) {
                log.error("PAYMENT outbox publish failed eventId={}", e.getId(), ex);
                // não marca sent; vai tentar de novo
            }
        }
    }
}