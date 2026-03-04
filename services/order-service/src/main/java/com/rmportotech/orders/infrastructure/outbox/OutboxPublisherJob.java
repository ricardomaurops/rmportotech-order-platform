package com.rmportotech.orders.infrastructure.outbox;

import com.rmportotech.orders.adapters.out.persistence.OutboxJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisherJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherJob.class);

    private final OutboxJpaRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${rmpt.kafka.topic.orders:orders.events}")
    private String ordersTopic;

    public OutboxPublisherJob(OutboxJpaRepository repo, KafkaTemplate<String, String> kafkaTemplate) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "2000")
    @Transactional
    public void publishPending() {
        var pending = repo.findPending(); // depois melhoramos com SKIP LOCKED (RMPT-12.2)
        for (var e : pending) {
            try {
                var key = e.getAggregateId().toString();

                // Envia e espera ack (sincrono) para garantir "SENT" só quando publicou
                kafkaTemplate.send(ordersTopic, key, e.getPayload()).get();

                log.info("OUTBOX published eventId={} topic={} key={} type={}",
                        e.getId(), ordersTopic, key, e.getEventType());

                e.markSent();
            } catch (Exception ex) {
                log.error("OUTBOX publish failed eventId={} type={} aggregateId={}",
                        e.getId(), e.getEventType(), e.getAggregateId(), ex);
                // não marca SENT -> vai tentar novamente no próximo ciclo
            }
        }
    }
}