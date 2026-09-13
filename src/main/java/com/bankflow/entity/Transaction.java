package com.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Transaction Entity representing an immutable audit record of balance modifications.
 * 
 * Key Interview Concepts:
 * - Immutability: Financial transactions should only be inserted, never updated or deleted.
 *   This ensures an accurate, verifiable audit trail (double-entry bookkeeping principle).
 * - Balance After Transaction: Storing the snapshot balance at the moment of the transaction
 *   allows generating point-in-time account statements without recalculating history from day zero.
 * - Indexes on timestamp & account_id: Optimizes transaction history pagination and date-range queries.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_reference", columnList = "transaction_reference", unique = true),
    @Index(name = "idx_tx_account_id", columnList = "account_id"),
    @Index(name = "idx_tx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_tx_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 36)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount account;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after_transaction", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfterTransaction;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.SUCCESS;

    @Column(name = "target_account_number", length = 16)
    private String targetAccountNumber;
}
