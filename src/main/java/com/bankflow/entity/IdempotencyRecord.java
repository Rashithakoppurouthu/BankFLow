package com.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * IdempotencyRecord Entity for preventing duplicate transaction submissions.
 * 
 * Key Interview Concepts:
 * - What is Idempotency? An idempotent HTTP API endpoint can be called multiple times
 *   with the same effect as calling it once.
 * - Why needed in Banking? If a user clicks "Transfer" and their network hangs after the server
 *   debits their account, the client may retry the request. Without an Idempotency-Key check,
 *   the user would be charged twice!
 * - How it works:
 *   1. Client sends a unique UUID in the header: 'Idempotency-Key: 8b1f4b3e-...'.
 *   2. Server checks if this key exists in the idempotency table.
 *   3. If found, returns the cached previous response immediately without re-executing the transfer.
 *   4. If new, processes the transaction, caches the result, and commits.
 */
@Entity
@Table(name = "idempotency_records", indexes = {
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
    @Index(name = "idx_idempotency_created", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "endpoint", nullable = false, length = 100)
    private String endpoint;

    @Column(name = "request_hash", length = 64)
    private String requestHash;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
