package com.bankflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for returning Beneficiary data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryResponse {

    private Long id;
    private String beneficiaryName;
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private Instant createdAt;
}
