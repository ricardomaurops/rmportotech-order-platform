package com.rmportotech.orders.infrastructure.outbox;

import com.rmportotech.orders.adapters.out.persistence.OutboxJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisherJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherJob.class);

    private final OutboxJpaRepository repo;

    public OutboxPublisherJob(OutboxJpaRepository repo) {
        this.repo = repo;
    }

    @Scheduled(fixedDelayString = "2000")
    @Transactional
    public void publishPending() {
        var pending = repo.findPendingForUpdateSkipLocked();
        for (var e : pending) {
            log.info("OUTBOX publish eventId={} type={} aggregateId={} payload={}",
                    e.getId(), e.getEventType(), e.getAggregateId(), e.getPayload()
            );
            e.markSent(); // como está em transação, o JPA faz update
        }
    }
}