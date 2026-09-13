package com.bankflow.repository;

import com.bankflow.entity.Transaction;
import com.bankflow.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Repository for Transaction history, pagination, filtering, and aggregation.
 * 
 * Key Interview Concepts:
 * - Spring Data Pagination: Returning Page<Transaction> calculates total pages, total elements,
 *   and applies SQL "LIMIT ? OFFSET ?" behind the scenes for optimal performance with large datasets.
 * - COALESCE in SQL: Guarantees that if there are zero transactions for a type, the query returns 0.00
 *   instead of SQL NULL, avoiding NullPointerExceptions in Java.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);

    Page<Transaction> findByAccountIdAndType(Long accountId, TransactionType type, Pageable pageable);

    Page<Transaction> findByAccountIdAndTimestampBetween(Long accountId, Instant fromDate, Instant toDate, Pageable pageable);

    Page<Transaction> findByAccountIdAndTypeAndTimestampBetween(
            Long accountId,
            TransactionType type,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    );

    List<Transaction> findByAccountIdOrderByTimestampDesc(Long accountId);

    List<Transaction> findByAccountIdAndTimestampBetweenOrderByTimestampAsc(
            Long accountId,
            Instant fromDate,
            Instant toDate
    );

    long countByType(TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0.00) FROM Transaction t WHERE t.type = :type AND t.status = com.bankflow.entity.TransactionStatus.SUCCESS")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);
}
