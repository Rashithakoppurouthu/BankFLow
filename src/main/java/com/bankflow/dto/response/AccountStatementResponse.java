package com.bankflow.dto.response;

import com.bankflow.entity.AccountStatus;
import com.bankflow.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO for account statements with transaction ledger.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementResponse {

    private String accountNumber;
    private String customerName;
    private AccountType accountType;
    private AccountStatus status;
    private BigDecimal currentBalance;
    private Instant periodStart;
    private Instant periodEnd;
    private List<TransactionResponse> transactions;
}
