package com.bankflow.service;

import com.bankflow.dto.request.DepositRequest;
import com.bankflow.dto.request.WithdrawRequest;
import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.entity.*;
import com.bankflow.exception.AccountFrozenException;
import com.bankflow.exception.InsufficientBalanceException;
import com.bankflow.mapper.BankAccountMapper;
import com.bankflow.mapper.TransactionMapper;
import com.bankflow.repository.BankAccountRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BankAccountService deposit and withdrawal operations.
 */
@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Spy
    private BankAccountMapper bankAccountMapper = new BankAccountMapper();

    @Spy
    private TransactionMapper transactionMapper = new TransactionMapper();

    @InjectMocks
    private BankAccountService bankAccountService;

    private User testUser;
    private BankAccount testAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(10L).email("user@test.com").firstName("Test").lastName("User").build();
        testAccount = BankAccount.builder()
                .id(100L)
                .accountNumber("100999999999")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .status(AccountStatus.ACTIVE)
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("Deposit Success: Adds amount to balance and creates deposit transaction")
    void deposit_Success() {
        DepositRequest request = DepositRequest.builder()
                .amount(new BigDecimal("500.00"))
                .description("Paycheck")
                .build();

        when(bankAccountRepository.findByIdWithLock(100L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction tx = i.getArgument(0);
            tx.setId(1L);
            return tx;
        });

        TransactionResponse response = bankAccountService.deposit(100L, 10L, request, false);

        assertThat(response).isNotNull();
        assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(response.getBalanceAfterTransaction()).isEqualByComparingTo(new BigDecimal("1500.00"));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Withdrawal Success: Deducts amount from balance and creates withdrawal transaction")
    void withdraw_Success() {
        WithdrawRequest request = WithdrawRequest.builder()
                .amount(new BigDecimal("400.00"))
                .description("ATM Cash")
                .build();

        when(bankAccountRepository.findByIdWithLock(100L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction tx = i.getArgument(0);
            tx.setId(2L);
            return tx;
        });

        TransactionResponse response = bankAccountService.withdraw(100L, 10L, request, false);

        assertThat(response).isNotNull();
        assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(response.getBalanceAfterTransaction()).isEqualByComparingTo(new BigDecimal("600.00"));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Withdrawal Rejection: Insufficient balance throws InsufficientBalanceException")
    void withdraw_InsufficientBalance_ThrowsException() {
        WithdrawRequest request = WithdrawRequest.builder()
                .amount(new BigDecimal("2500.00")) // Exceeds 1000.00 balance
                .build();

        when(bankAccountRepository.findByIdWithLock(100L)).thenReturn(Optional.of(testAccount));

        assertThatThrownBy(() -> bankAccountService.withdraw(100L, 10L, request, false))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient account balance");

        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    @DisplayName("Deposit Rejection: Frozen account throws AccountFrozenException")
    void deposit_FrozenAccount_ThrowsException() {
        testAccount.setStatus(AccountStatus.FROZEN);
        DepositRequest request = DepositRequest.builder().amount(new BigDecimal("100.00")).build();

        when(bankAccountRepository.findByIdWithLock(100L)).thenReturn(Optional.of(testAccount));

        assertThatThrownBy(() -> bankAccountService.deposit(100L, 10L, request, false))
                .isInstanceOf(AccountFrozenException.class)
                .hasMessageContaining("FROZEN");
    }
}
