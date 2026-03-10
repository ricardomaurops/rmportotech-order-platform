package com.rmportotech.payment.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query(value = """
        select * from outbox_event
        where status = 'PENDING'
        order by created_at
        asc for update skip locked limit 50
        """, nativeQuery = true)
    List<OutboxEventEntity> findPending();
}