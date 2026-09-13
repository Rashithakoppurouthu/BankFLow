package com.bankflow.service;

import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.entity.BankAccount;
import com.bankflow.entity.Transaction;
import com.bankflow.entity.TransactionType;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.exception.UnauthorizedException;
import com.bankflow.mapper.TransactionMapper;
import com.bankflow.repository.BankAccountRepository;
import com.bankflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing transaction querying, filtering, and Spring Data pagination.
 * 
 * Key Interview Concepts:
 * - Pageable & Sort: Eliminates manual SQL LIMIT/OFFSET logic.
 * - Dynamic filtering: Combines optional query parameters (type, date intervals).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            Long accountId,
            Long userId,
            TransactionType type,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size,
            String sortBy,
            String sortDir,
            boolean isAdmin
    ) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));

        if (!isAdmin && !account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view transactions for this account");
        }

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Instant startInstant = (fromDate != null)
                ? fromDate.atStartOfDay().toInstant(ZoneOffset.UTC)
                : null;
        Instant endInstant = (toDate != null)
                ? toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                : null;

        Page<Transaction> transactionPage;

        if (type != null && startInstant != null && endInstant != null) {
            transactionPage = transactionRepository.findByAccountIdAndTypeAndTimestampBetween(
                    accountId, type, startInstant, endInstant, pageable);
        } else if (type != null) {
            transactionPage = transactionRepository.findByAccountIdAndType(accountId, type, pageable);
        } else if (startInstant != null && endInstant != null) {
            transactionPage = transactionRepository.findByAccountIdAndTimestampBetween(
                    accountId, startInstant, endInstant, pageable);
        } else {
            transactionPage = transactionRepository.findByAccountId(accountId, pageable);
        }

        return transactionPage.map(transactionMapper::toResponse);
    }

    /**
     * Fetches recent transactions across all accounts owned by the user (for dashboard summary).
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions(Long userId, int limit) {
        List<BankAccount> accounts = bankAccountRepository.findByUserId(userId);
        List<TransactionResponse> allTransactions = new ArrayList<>();

        for (BankAccount account : accounts) {
            List<Transaction> txs = transactionRepository.findByAccountIdOrderByTimestampDesc(account.getId());
            for (Transaction tx : txs) {
                allTransactions.add(transactionMapper.toResponse(tx));
            }
        }

        return allTransactions.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
