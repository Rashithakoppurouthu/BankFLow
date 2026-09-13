package com.bankflow.dto.response;

import com.bankflow.entity.TransactionStatus;
import com.bankflow.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for returning Transaction data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String transactionReference;
    private Long accountId;
    private String accountNumber;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private Instant timestamp;
    private String description;
    private TransactionStatus status;
    private String targetAccountNumber;
}
