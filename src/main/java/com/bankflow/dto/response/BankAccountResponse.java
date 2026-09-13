package com.bankflow.dto.response;

import com.bankflow.entity.AccountStatus;
import com.bankflow.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for returning BankAccount data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponse {

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private Long userId;
    private String ownerName;
    private String ownerEmail;
    private Instant createdAt;
}
