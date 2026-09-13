package com.bankflow.service;

import com.bankflow.dto.response.BankAccountResponse;
import com.bankflow.dto.response.DashboardStatsResponse;
import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.dto.response.UserResponse;
import com.bankflow.entity.AccountStatus;
import com.bankflow.entity.TransactionType;
import com.bankflow.mapper.BankAccountMapper;
import com.bankflow.mapper.TransactionMapper;
import com.bankflow.mapper.UserMapper;
import com.bankflow.repository.BankAccountRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service providing administrative oversight, account freezing, and system-wide metrics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final BankAccountService bankAccountService;
    private final UserMapper userMapper;
    private final BankAccountMapper bankAccountMapper;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BankAccountResponse> getAllAccounts() {
        return bankAccountRepository.findAll().stream()
                .map(bankAccountMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return transactionRepository.findAll(pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional
    public BankAccountResponse freezeAccount(Long accountId) {
        return bankAccountService.freezeAccount(accountId);
    }

    @Transactional
    public BankAccountResponse unfreezeAccount(Long accountId) {
        return bankAccountService.unfreezeAccount(accountId);
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStatistics() {
        long totalCustomers = userRepository.count();
        long totalAccounts = bankAccountRepository.count();
        long activeAccounts = bankAccountRepository.countByStatus(AccountStatus.ACTIVE);
        long frozenAccounts = bankAccountRepository.countByStatus(AccountStatus.FROZEN);

        long totalTransactions = transactionRepository.count();
        BigDecimal totalDeposits = transactionRepository.sumAmountByType(TransactionType.DEPOSIT);
        BigDecimal totalWithdrawals = transactionRepository.sumAmountByType(TransactionType.WITHDRAWAL);
        BigDecimal totalTransfers = transactionRepository.sumAmountByType(TransactionType.TRANSFER);

        return DashboardStatsResponse.builder()
                .totalCustomers(totalCustomers)
                .totalAccounts(totalAccounts)
                .activeAccounts(activeAccounts)
                .frozenAccounts(frozenAccounts)
                .totalTransactions(totalTransactions)
                .totalDeposits(totalDeposits != null ? totalDeposits : BigDecimal.ZERO)
                .totalWithdrawals(totalWithdrawals != null ? totalWithdrawals : BigDecimal.ZERO)
                .totalTransfers(totalTransfers != null ? totalTransfers : BigDecimal.ZERO)
                .build();
    }
}
