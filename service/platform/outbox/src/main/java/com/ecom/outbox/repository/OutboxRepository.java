package com.ecom.outbox.repository;

import com.ecom.outbox.entity.OutboxEntity;
import com.ecom.outbox.enums.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    /**
     * Locks the oldest rows (no SKIP LOCKED on purpose): a second relay instance waits for the first to finish
     * instead of publishing later rows ahead of an earlier one, which would break ordering.
     */
    @Modifying
    @Query("delete from OutboxEntity e where e.status = :status and e.processedAt < :before")
    int deleteProcessedBefore(@Param("status") OutboxStatus status, @Param("before") LocalDateTime before);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEntity e where e.status = :status order by e.createdAt asc")
    List<OutboxEntity> lockPendingBatch(@Param("status") OutboxStatus status, Pageable pageable);
}
