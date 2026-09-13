package com.bankflow.service;

import com.bankflow.dto.request.AccountCreateRequest;
import com.bankflow.dto.request.DepositRequest;
import com.bankflow.dto.request.WithdrawRequest;
import com.bankflow.dto.response.BankAccountResponse;
import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.entity.*;
import com.bankflow.exception.AccountFrozenException;
import com.bankflow.exception.InsufficientBalanceException;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.exception.UnauthorizedException;
import com.bankflow.mapper.BankAccountMapper;
import com.bankflow.mapper.TransactionMapper;
import com.bankflow.repository.BankAccountRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import com.bankflow.util.AccountNumberGenerator;
import com.bankflow.util.TransactionReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing Bank Account lifecycle, deposits, withdrawals, and account status updates.
 * 
 * Key Interview Concepts:
 * - Isolation Level READ_COMMITTED: Guarantees that dirty reads (reading uncommitted data) cannot happen.
 * - Atomic Balance Modification: Balances are strictly altered through business transactions,
 *   never exposed to direct client modification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BankAccountMapper bankAccountMapper;
    private final TransactionMapper transactionMapper;

    @Transactional
    public BankAccountResponse createAccount(Long userId, AccountCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String accountNumber;
        do {
            accountNumber = AccountNumberGenerator.generate();
        } while (bankAccountRepository.existsByAccountNumber(accountNumber));

        BigDecimal initialDeposit = request.getInitialDeposit() != null
                ? request.getInitialDeposit().setScale(2)
                : BigDecimal.ZERO.setScale(2);

        BankAccount account = BankAccount.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(initialDeposit)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();

        BankAccount savedAccount = bankAccountRepository.save(account);

        // If there was an initial deposit, record the transaction ledger entry
        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            Transaction depositTx = Transaction.builder()
                    .transactionReference(TransactionReferenceGenerator.generate())
                    .account(savedAccount)
                    .type(TransactionType.DEPOSIT)
                    .amount(initialDeposit)
                    .balanceAfterTransaction(initialDeposit)
                    .timestamp(Instant.now())
                    .description("Initial account opening deposit")
                    .status(TransactionStatus.SUCCESS)
                    .build();
            transactionRepository.save(depositTx);
        }

        log.info("Created new account: {} for user: {}", accountNumber, user.getEmail());
        return bankAccountMapper.toResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<BankAccountResponse> getUserAccounts(Long userId) {
        return bankAccountRepository.findByUserId(userId).stream()
                .map(bankAccountMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BankAccountResponse getAccountById(Long accountId, Long userId, boolean isAdmin) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));

        if (!isAdmin && !account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this bank account");
        }

        return bankAccountMapper.toResponse(account);
    }

    /**
     * Executes atomic cash deposit into an active account.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TransactionResponse deposit(Long accountId, Long userId, DepositRequest request, boolean isAdmin) {
        BankAccount account = bankAccountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));

        if (!isAdmin && !account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to deposit into this account");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException("Cannot deposit to account in " + account.getStatus() + " status");
        }

        BigDecimal depositAmount = request.getAmount().setScale(2);
        BigDecimal newBalance = account.getBalance().add(depositAmount);
        account.setBalance(newBalance);
        bankAccountRepository.save(account);

        String description = (request.getDescription() != null && !request.getDescription().isBlank())
                ? request.getDescription().trim()
                : "Cash deposit";

        Transaction transaction = Transaction.builder()
                .transactionReference(TransactionReferenceGenerator.generate())
                .account(account)
                .type(TransactionType.DEPOSIT)
                .amount(depositAmount)
                .balanceAfterTransaction(newBalance)
                .timestamp(Instant.now())
                .description(description)
                .status(TransactionStatus.SUCCESS)
                .build();

        Transaction savedTx = transactionRepository.save(transaction);
        log.info("Deposit completed: Account={}, Amount={}, NewBalance={}", account.getAccountNumber(), depositAmount, newBalance);

        return transactionMapper.toResponse(savedTx);
    }

    /**
     * Executes atomic cash withdrawal from an active account.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TransactionResponse withdraw(Long accountId, Long userId, WithdrawRequest request, boolean isAdmin) {
        BankAccount account = bankAccountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));

        if (!isAdmin && !account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to withdraw from this account");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException("Cannot withdraw from account in " + account.getStatus() + " status");
        }

        BigDecimal withdrawAmount = request.getAmount().setScale(2);

        if (account.getBalance().compareTo(withdrawAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient account balance. Available: " + account.getBalance() + ", Requested: " + withdrawAmount);
        }

        BigDecimal newBalance = account.getBalance().subtract(withdrawAmount);
        account.setBalance(newBalance);
        bankAccountRepository.save(account);

        String description = (request.getDescription() != null && !request.getDescription().isBlank())
                ? request.getDescription().trim()
                : "Cash withdrawal";

        Transaction transaction = Transaction.builder()
                .transactionReference(TransactionReferenceGenerator.generate())
                .account(account)
                .type(TransactionType.WITHDRAWAL)
                .amount(withdrawAmount)
                .balanceAfterTransaction(newBalance)
                .timestamp(Instant.now())
                .description(description)
                .status(TransactionStatus.SUCCESS)
                .build();

        Transaction savedTx = transactionRepository.save(transaction);
        log.info("Withdrawal completed: Account={}, Amount={}, NewBalance={}", account.getAccountNumber(), withdrawAmount, newBalance);

        return transactionMapper.toResponse(savedTx);
    }

    @Transactional
    public BankAccountResponse freezeAccount(Long accountId) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));
        account.setStatus(AccountStatus.FROZEN);
        BankAccount updated = bankAccountRepository.save(account);
        log.warn("Account {} frozen by Administrator", account.getAccountNumber());
        return bankAccountMapper.toResponse(updated);
    }

    @Transactional
    public BankAccountResponse unfreezeAccount(Long accountId) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));
        account.setStatus(AccountStatus.ACTIVE);
        BankAccount updated = bankAccountRepository.save(account);
        log.info("Account {} unfrozen by Administrator", account.getAccountNumber());
        return bankAccountMapper.toResponse(updated);
    }
}
