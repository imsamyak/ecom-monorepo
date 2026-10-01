package com.ecom.inbox.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface InboxRepository extends JpaRepository<InboxEntity, InboxEntity.InboxId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InboxEntity i WHERE i.aggregateType = :aggregateType AND i.aggregateId = :aggregateId")
    Optional<InboxEntity> findByAggregateWithLock(@Param("aggregateType") String aggregateType, @Param("aggregateId") String aggregateId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM InboxEntity i WHERE i.processedAt < :threshold")
    int deleteOlderThan(@Param("threshold") Instant threshold);
}
