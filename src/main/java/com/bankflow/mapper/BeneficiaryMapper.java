package com.bankflow.mapper;

import com.bankflow.dto.request.BeneficiaryRequest;
import com.bankflow.dto.response.BeneficiaryResponse;
import com.bankflow.entity.Beneficiary;
import com.bankflow.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for Beneficiary entity and DTO transformations.
 */
@Component
public class BeneficiaryMapper {

    public Beneficiary toEntity(BeneficiaryRequest request, User user) {
        return Beneficiary.builder()
                .beneficiaryName(request.getBeneficiaryName().trim())
                .accountNumber(request.getAccountNumber().trim())
                .bankName(request.getBankName().trim())
                .ifscCode(request.getIfscCode().trim().toUpperCase())
                .user(user)
                .build();
    }

    public BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        if (beneficiary == null) {
            return null;
        }

        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .beneficiaryName(beneficiary.getBeneficiaryName())
                .accountNumber(beneficiary.getAccountNumber())
                .bankName(beneficiary.getBankName())
                .ifscCode(beneficiary.getIfscCode())
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }
}
