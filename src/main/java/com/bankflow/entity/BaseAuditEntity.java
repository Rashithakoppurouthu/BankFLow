package com.bankflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Mapped superclass providing automatic JPA auditing timestamps for entities.
 * 
 * Key Interview Concepts:
 * - @MappedSuperclass: Inherits persistent properties (columns) without creating a separate table.
 * - @EntityListeners(AuditingEntityListener.class): Hooks into JPA lifecycle events
 *   (PrePersist, PreUpdate) to populate @CreatedDate and @LastModifiedDate automatically.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseAuditEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
