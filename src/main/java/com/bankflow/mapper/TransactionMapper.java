package com.bankflow.mapper;

import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.entity.Transaction;
import org.springframework.stereotype.Component;

/**
 * Mapper for Transaction entity and DTO transformations.
 */
@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .accountId(transaction.getAccount() != null ? transaction.getAccount().getId() : null)
                .accountNumber(transaction.getAccount() != null ? transaction.getAccount().getAccountNumber() : null)
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .timestamp(transaction.getTimestamp())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .targetAccountNumber(transaction.getTargetAccountNumber())
                .build();
    }
}
