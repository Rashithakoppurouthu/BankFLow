package com.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * BankAccount Entity representing financial depository accounts.
 * 
 * Key Interview Concepts:
 * - BigDecimal for Money: Crucial interview talking point! Float and Double use IEEE 754
 *   binary floating-point representation, which causes inexact fractions (e.g. 0.1 + 0.2 = 0.30000000000000004).
 *   BigDecimal provides arbitrary-precision signed decimal numbers, guaranteeing zero rounding drift.
 * - @Version: Enables JPA Optimistic Locking. Hibernate tracks version number changes.
 *   If two transactions attempt to update the same account concurrently, the second will throw
 *   OptimisticLockException, preventing lost updates.
 * - Unique constraint on accountNumber: Guarantees account uniqueness in the database.
 */
@Entity
@Table(name = "bank_accounts", indexes = {
    @Index(name = "idx_account_number", columnList = "account_number", unique = true),
    @Index(name = "idx_account_user_id", columnList = "user_id"),
    @Index(name = "idx_account_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 16)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO.setScale(2);

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();
}
