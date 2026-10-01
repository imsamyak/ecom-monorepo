package com.ecom.inbox.adapter.out.persistence;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "inbox_last_event")
@IdClass(InboxEntity.InboxId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboxEntity implements Persistable<InboxEntity.InboxId> {

    @Id
    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Id
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "last_event_id", nullable = false)
    private String lastEventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @Transient
    private boolean isNew = true;

    public InboxEntity(String aggregateType, String aggregateId) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
    }

    @Override
    public InboxId getId() {
        return new InboxId(aggregateType, aggregateId);
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InboxId implements Serializable {
        private String aggregateType;
        private String aggregateId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InboxId inboxId = (InboxId) o;
            return Objects.equals(aggregateType, inboxId.aggregateType) && Objects.equals(aggregateId, inboxId.aggregateId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(aggregateType, aggregateId);
        }
    }
}
