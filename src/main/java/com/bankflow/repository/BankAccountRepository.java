package com.bankflow.repository;

import com.bankflow.entity.AccountStatus;
import com.bankflow.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BankAccount entity data access and pessimistic locking.
 * 
 * Key Interview Concepts:
 * - Pessimistic Locking (@Lock(LockModeType.PESSIMISTIC_WRITE)):
 *   Translates in MySQL to "SELECT * FROM bank_accounts WHERE id = ? FOR UPDATE".
 *   This acquires an exclusive row-level lock on the account record inside the database transaction.
 *   Any other concurrent transaction attempting to read or write to this account must wait until
 *   the current transaction commits or rolls back, completely eliminating race conditions and double-spending.
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findByUserId(Long userId);

    Optional<BankAccount> findByIdAndUserId(Long id, Long userId);

    Boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankAccount b WHERE b.id = :id")
    Optional<BankAccount> findByIdWithLock(@Param("id") Long id);

    long countByStatus(AccountStatus status);
}
