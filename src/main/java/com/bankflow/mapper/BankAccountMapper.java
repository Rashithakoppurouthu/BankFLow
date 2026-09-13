package com.bankflow.mapper;

import com.bankflow.dto.response.BankAccountResponse;
import com.bankflow.entity.BankAccount;
import org.springframework.stereotype.Component;

/**
 * Mapper for BankAccount entity and DTO transformations.
 */
@Component
public class BankAccountMapper {

    public BankAccountResponse toResponse(BankAccount account) {
        if (account == null) {
            return null;
        }

        return BankAccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .userId(account.getUser() != null ? account.getUser().getId() : null)
                .ownerName(account.getUser() != null ? account.getUser().getFullName() : null)
                .ownerEmail(account.getUser() != null ? account.getUser().getEmail() : null)
                .createdAt(account.getCreatedAt())
                .build();
    }
}
