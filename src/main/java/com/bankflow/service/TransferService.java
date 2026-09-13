package com.bankflow.service;

import com.bankflow.dto.request.TransferRequest;
import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.entity.*;
import com.bankflow.exception.*;
import com.bankflow.mapper.TransactionMapper;
import com.bankflow.repository.BankAccountRepository;
import com.bankflow.repository.IdempotencyRecordRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.util.TransactionReferenceGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Service managing atomic money transfers between bank accounts with concurrency locking and idempotency.
 * 
 * ====================================================================================================
 * HIGH-YIELD INTERVIEW TOPICS:
 * ====================================================================================================
 * 1. Why @Transactional is CRITICAL:
 *    Without @Transactional, if deducting money from the sender succeeds but crediting the recipient
 *    fails (e.g. database disconnect, power outage, constraint violation), the sender's money is LOST forever!
 *    With @Transactional(rollbackFor = Exception.class), Spring's TransactionInterceptor monitors the method.
 *    If any unchecked or checked exception is thrown, the entire operation is automatically rolled back,
 *    restoring both account balances to their exact original state (ACID Atomicity).
 * 
 * 2. Deadlock Prevention via Canonical Ordering:
 *    If User A transfers from Account 1 -> Account 2, and User B transfers from Account 2 -> Account 1 concurrently:
 *    Thread A locks Account 1 and asks for Account 2.
 *    Thread B locks Account 2 and asks for Account 1.
 *    Both threads block each other forever -> DEADLOCK!
 *    Solution: We always acquire locks ordered by account ID: min(ID_A, ID_B), then max(ID_A, ID_B).
 *    Both threads will now contend for Account 1 first, eliminating cyclical wait conditions completely!
 * 
 * 3. Idempotency Support:
 *    Guarantees that retry requests (e.g. caused by client-side network timeouts) do not double-debit the customer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final TransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TransactionResponse transferMoney(Long userId, TransferRequest request, String idempotencyKey, boolean isAdmin) {

        // 1. Idempotency check: If key already processed, return cached response
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existingRecord = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey.trim());
            if (existingRecord.isPresent()) {
                log.info("Idempotent transfer request detected for key: {}. Returning cached result.", idempotencyKey);
                try {
                    return objectMapper.readValue(existingRecord.get().getResponseBody(), TransactionResponse.class);
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse cached idempotent response: {}", e.getMessage());
                }
            }
        }

        // 2. Business validation: Sender cannot transfer to same account
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new InvalidTransactionException("Sender account and recipient account cannot be the same");
        }

        // 3. Acquire Pessimistic Locks in Canonical Sorted Order (Prevents Deadlocks!)
        Long firstLockId = Math.min(request.getFromAccountId(), request.getToAccountId());
        Long secondLockId = Math.max(request.getFromAccountId(), request.getToAccountId());

        BankAccount firstAccount = bankAccountRepository.findByIdWithLock(firstLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + firstLockId));
        BankAccount secondAccount = bankAccountRepository.findByIdWithLock(secondLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + secondLockId));

        BankAccount fromAccount = request.getFromAccountId().equals(firstLockId) ? firstAccount : secondAccount;
        BankAccount toAccount = request.getToAccountId().equals(firstLockId) ? firstAccount : secondAccount;

        // 4. Authorization check: Sender account must belong to authenticated user (unless admin)
        if (!isAdmin && !fromAccount.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to transfer funds from account: " + fromAccount.getAccountNumber());
        }

        // 5. Account state validation: Both accounts must be ACTIVE
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException("Sender account is in " + fromAccount.getStatus() + " status");
        }
        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException("Recipient account is in " + toAccount.getStatus() + " status");
        }

        // 6. Balance validation
        BigDecimal transferAmount = request.getAmount().setScale(2);
        if (fromAccount.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for transfer. Available: " + fromAccount.getBalance() + ", Required: " + transferAmount
            );
        }

        // 7. Atomic balance mutation
        BigDecimal fromNewBalance = fromAccount.getBalance().subtract(transferAmount);
        BigDecimal toNewBalance = toAccount.getBalance().add(transferAmount);

        fromAccount.setBalance(fromNewBalance);
        toAccount.setBalance(toNewBalance);

        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);

        Instant now = Instant.now();
        String userDescription = (request.getDescription() != null && !request.getDescription().isBlank())
                ? " - " + request.getDescription().trim()
                : "";

        // 8. Create Debit Transaction Record for Sender
        Transaction debitTx = Transaction.builder()
                .transactionReference(TransactionReferenceGenerator.generate())
                .account(fromAccount)
                .type(TransactionType.TRANSFER)
                .amount(transferAmount)
                .balanceAfterTransaction(fromNewBalance)
                .timestamp(now)
                .description("Transfer to A/C " + toAccount.getAccountNumber() + userDescription)
                .status(TransactionStatus.SUCCESS)
                .targetAccountNumber(toAccount.getAccountNumber())
                .build();
        Transaction savedDebitTx = transactionRepository.save(debitTx);

        // 9. Create Credit Transaction Record for Recipient
        Transaction creditTx = Transaction.builder()
                .transactionReference(TransactionReferenceGenerator.generate())
                .account(toAccount)
                .type(TransactionType.TRANSFER)
                .amount(transferAmount)
                .balanceAfterTransaction(toNewBalance)
                .timestamp(now)
                .description("Transfer from A/C " + fromAccount.getAccountNumber() + userDescription)
                .status(TransactionStatus.SUCCESS)
                .targetAccountNumber(fromAccount.getAccountNumber())
                .build();
        transactionRepository.save(creditTx);

        TransactionResponse response = transactionMapper.toResponse(savedDebitTx);

        // 10. Persist Idempotency Record if key was provided
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            try {
                String serializedResponse = objectMapper.writeValueAsString(response);
                IdempotencyRecord record = IdempotencyRecord.builder()
                        .idempotencyKey(idempotencyKey.trim())
                        .userId(userId)
                        .endpoint("/api/transfers")
                        .responseStatus(200)
                        .responseBody(serializedResponse)
                        .build();
                idempotencyRecordRepository.save(record);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize idempotency response: {}", e.getMessage());
            }
        }

        log.info("Transfer completed successfully: From={} To={} Amount={}",
                fromAccount.getAccountNumber(), toAccount.getAccountNumber(), transferAmount);

        return response;
    }
}
