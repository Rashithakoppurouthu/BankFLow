package com.bankflow.service;

import com.bankflow.dto.request.TransferRequest;
import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.entity.*;
import com.bankflow.exception.*;
import com.bankflow.mapper.TransactionMapper;
import com.bankflow.repository.BankAccountRepository;
import com.bankflow.repository.IdempotencyRecordRepository;
import com.bankflow.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransferService business logic, validations, and concurrency orchestration.
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;

    @Spy
    private TransactionMapper transactionMapper = new TransactionMapper();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @InjectMocks
    private TransferService transferService;

    private User senderUser;
    private User recipientUser;
    private BankAccount senderAccount;
    private BankAccount recipientAccount;

    @BeforeEach
    void setUp() {
        senderUser = User.builder().id(1L).email("sender@test.com").firstName("Sender").lastName("User").build();
        recipientUser = User.builder().id(2L).email("recipient@test.com").firstName("Recipient").lastName("User").build();

        senderAccount = BankAccount.builder()
                .id(101L)
                .accountNumber("100111111111")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .status(AccountStatus.ACTIVE)
                .user(senderUser)
                .build();

        recipientAccount = BankAccount.builder()
                .id(102L)
                .accountNumber("100222222222")
                .accountType(AccountType.CURRENT)
                .balance(new BigDecimal("2000.00"))
                .status(AccountStatus.ACTIVE)
                .user(recipientUser)
                .build();
    }

    @Test
    @DisplayName("Successful Transfer: Deducts sender, credits recipient, records audit transactions")
    void transferMoney_Success() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(101L)
                .toAccountId(102L)
                .amount(new BigDecimal("1500.00"))
                .description("Rent Payment")
                .build();

        when(idempotencyRecordRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(bankAccountRepository.findByIdWithLock(101L)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findByIdWithLock(102L)).thenReturn(Optional.of(recipientAccount));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction tx = i.getArgument(0);
            tx.setId(999L);
            return tx;
        });

        TransactionResponse response = transferService.transferMoney(1L, request, "idemp-key-123", false);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(senderAccount.getBalance()).isEqualByComparingTo(new BigDecimal("3500.00"));
        assertThat(recipientAccount.getBalance()).isEqualByComparingTo(new BigDecimal("3500.00"));

        verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(idempotencyRecordRepository, times(1)).save(any(IdempotencyRecord.class));
    }

    @Test
    @DisplayName("Transfer Rejection: Sender and recipient account cannot be the same")
    void transferMoney_SameAccount_ThrowsException() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(101L)
                .toAccountId(101L)
                .amount(new BigDecimal("500.00"))
                .build();

        assertThatThrownBy(() -> transferService.transferMoney(1L, request, null, false))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("cannot be the same");
    }

    @Test
    @DisplayName("Transfer Rejection: Insufficient sender balance")
    void transferMoney_InsufficientBalance_ThrowsException() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(101L)
                .toAccountId(102L)
                .amount(new BigDecimal("10000.00")) // Exceeds 5000.00 balance
                .build();

        when(bankAccountRepository.findByIdWithLock(101L)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findByIdWithLock(102L)).thenReturn(Optional.of(recipientAccount));

        assertThatThrownBy(() -> transferService.transferMoney(1L, request, null, false))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");

        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }

    @Test
    @DisplayName("Transfer Rejection: Sender account is FROZEN")
    void transferMoney_SenderFrozen_ThrowsException() {
        senderAccount.setStatus(AccountStatus.FROZEN);

        TransferRequest request = TransferRequest.builder()
                .fromAccountId(101L)
                .toAccountId(102L)
                .amount(new BigDecimal("500.00"))
                .build();

        when(bankAccountRepository.findByIdWithLock(101L)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findByIdWithLock(102L)).thenReturn(Optional.of(recipientAccount));

        assertThatThrownBy(() -> transferService.transferMoney(1L, request, null, false))
                .isInstanceOf(AccountFrozenException.class)
                .hasMessageContaining("FROZEN");
    }

    @Test
    @DisplayName("Transfer Rejection: Recipient account is FROZEN")
    void transferMoney_RecipientFrozen_ThrowsException() {
        recipientAccount.setStatus(AccountStatus.FROZEN);

        TransferRequest request = TransferRequest.builder()
                .fromAccountId(101L)
                .toAccountId(102L)
                .amount(new BigDecimal("500.00"))
                .build();

        when(bankAccountRepository.findByIdWithLock(101L)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findByIdWithLock(102L)).thenReturn(Optional.of(recipientAccount));

        assertThatThrownBy(() -> transferService.transferMoney(1L, request, null, false))
                .isInstanceOf(AccountFrozenException.class)
                .hasMessageContaining("FROZEN");
    }

    @Test
    @DisplayName("Transfer Rejection: User is not authorized to transfer from another user's account")
    void transferMoney_UnauthorizedSender_ThrowsException() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(101L) // Owned by user 1L
                .toAccountId(102L)
                .amount(new BigDecimal("500.00"))
                .build();

        when(bankAccountRepository.findByIdWithLock(101L)).thenReturn(Optional.of(senderAccount));
        when(bankAccountRepository.findByIdWithLock(102L)).thenReturn(Optional.of(recipientAccount));

        // User 999L attempts to transfer from User 1L's account
        assertThatThrownBy(() -> transferService.transferMoney(999L, request, null, false))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("Idempotency: Re-submitting the same key returns cached response without duplicate execution")
    void transferMoney_Idempotency_ReturnsCachedResponse() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(101L)
                .toAccountId(102L)
                .amount(new BigDecimal("500.00"))
                .build();

        TransactionResponse cachedResponse = TransactionResponse.builder()
                .id(888L)
                .transactionReference("TXN-CACHED-123456")
                .amount(new BigDecimal("500.00"))
                .balanceAfterTransaction(new BigDecimal("4500.00"))
                .timestamp(Instant.now())
                .status(TransactionStatus.SUCCESS)
                .build();

        String cachedJson = objectMapper.writeValueAsString(cachedResponse);
        IdempotencyRecord cachedRecord = IdempotencyRecord.builder()
                .idempotencyKey("repeat-key-777")
                .userId(1L)
                .responseStatus(200)
                .responseBody(cachedJson)
                .build();

        when(idempotencyRecordRepository.findByIdempotencyKey("repeat-key-777"))
                .thenReturn(Optional.of(cachedRecord));

        TransactionResponse response = transferService.transferMoney(1L, request, "repeat-key-777", false);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionReference()).isEqualTo("TXN-CACHED-123456");
        // Verify database locks and state updates were never executed!
        verify(bankAccountRepository, never()).findByIdWithLock(anyLong());
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
    }
}
