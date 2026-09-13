package com.bankflow.integration;

import com.bankflow.dto.request.TransferRequest;
import com.bankflow.entity.*;
import com.bankflow.repository.BankAccountRepository;
import com.bankflow.repository.RoleRepository;
import com.bankflow.repository.UserRepository;
import com.bankflow.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end Integration Test for Money Transfers, Security, and Error Handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private User sender;
    private User recipient;
    private BankAccount senderAccount;
    private BankAccount recipientAccount;
    private String senderJwt;

    @BeforeEach
    void setUp() {
        Role customerRole = roleRepository.findByName(RoleType.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_CUSTOMER)));

        sender = userRepository.findByEmail("sender.integration@test.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .firstName("Sender")
                        .lastName("Integration")
                        .email("sender.integration@test.com")
                        .phone("+1112223334")
                        .password("hashedPass")
                        .dateOfBirth(LocalDate.of(1992, 4, 10))
                        .roles(Set.of(customerRole))
                        .build()));

        recipient = userRepository.findByEmail("recipient.integration@test.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .firstName("Recipient")
                        .lastName("Integration")
                        .email("recipient.integration@test.com")
                        .phone("+1112223335")
                        .password("hashedPass")
                        .dateOfBirth(LocalDate.of(1994, 6, 12))
                        .roles(Set.of(customerRole))
                        .build()));

        senderAccount = bankAccountRepository.findByAccountNumber("100999000111")
                .orElseGet(() -> bankAccountRepository.save(BankAccount.builder()
                        .accountNumber("100999000111")
                        .accountType(AccountType.SAVINGS)
                        .balance(new BigDecimal("10000.00"))
                        .status(AccountStatus.ACTIVE)
                        .user(sender)
                        .build()));
        senderAccount.setBalance(new BigDecimal("10000.00"));
        senderAccount.setStatus(AccountStatus.ACTIVE);
        senderAccount = bankAccountRepository.save(senderAccount);

        recipientAccount = bankAccountRepository.findByAccountNumber("100999000222")
                .orElseGet(() -> bankAccountRepository.save(BankAccount.builder()
                        .accountNumber("100999000222")
                        .accountType(AccountType.CURRENT)
                        .balance(new BigDecimal("2000.00"))
                        .status(AccountStatus.ACTIVE)
                        .user(recipient)
                        .build()));
        recipientAccount.setBalance(new BigDecimal("2000.00"));
        recipientAccount.setStatus(AccountStatus.ACTIVE);
        recipientAccount = bankAccountRepository.save(recipientAccount);

        senderJwt = "Bearer " + jwtUtils.generateTokenFromEmail(
                sender.getEmail(),
                sender.getId(),
                sender.getFullName(),
                List.of(RoleType.ROLE_CUSTOMER.name())
        );
    }

    @Test
    @DisplayName("Integration: Successful Transfer updates balances and returns 200 OK")
    void testTransferMoney_Success() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(senderAccount.getId())
                .toAccountId(recipientAccount.getId())
                .amount(new BigDecimal("2500.00"))
                .description("Monthly Rent")
                .build();

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", senderJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.amount", is(2500.0)))
                .andExpect(jsonPath("$.data.balanceAfterTransaction", is(7500.0)));
    }

    @Test
    @DisplayName("Integration: Insufficient balance returns 400 with INSUFFICIENT_BALANCE error")
    void testTransferMoney_InsufficientBalance() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(senderAccount.getId())
                .toAccountId(recipientAccount.getId())
                .amount(new BigDecimal("50000.00")) // Exceeds 10,000.00
                .description("Too Much")
                .build();

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", senderJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INSUFFICIENT_BALANCE")));
    }

    @Test
    @DisplayName("Integration: Same sender and receiver accounts returns 400 with INVALID_TRANSACTION")
    void testTransferMoney_SameAccount_Rejected() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(senderAccount.getId())
                .toAccountId(senderAccount.getId())
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", senderJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_TRANSACTION")));
    }

    @Test
    @DisplayName("Integration: Frozen sender account returns 400 with ACCOUNT_FROZEN")
    void testTransferMoney_FrozenAccount_Rejected() throws Exception {
        BankAccount freshSender = bankAccountRepository.findById(senderAccount.getId()).orElseThrow();
        freshSender.setStatus(AccountStatus.FROZEN);
        senderAccount = bankAccountRepository.save(freshSender);

        TransferRequest request = TransferRequest.builder()
                .fromAccountId(senderAccount.getId())
                .toAccountId(recipientAccount.getId())
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", senderJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("ACCOUNT_FROZEN")));
    }

    @Test
    @DisplayName("Integration: Unauthenticated request returns 401 Unauthorized")
    void testTransferMoney_Unauthenticated_Rejected() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(senderAccount.getId())
                .toAccountId(recipientAccount.getId())
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
    }
}
