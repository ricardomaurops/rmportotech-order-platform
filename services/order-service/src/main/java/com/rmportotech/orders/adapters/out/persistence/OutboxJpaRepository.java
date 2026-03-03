package com.rmportotech.orders.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query("select e from OutboxEventEntity e where e.status = 'PENDING' order by e.createdAt asc")
    List<OutboxEventEntity> findPending();

    @Query(value = "select * from outbox_event where status = 'PENDING' order by created_at asc for update skip locked", nativeQuery = true)
    List<OutboxEventEntity> findPendingForUpdateSkipLocked();
}