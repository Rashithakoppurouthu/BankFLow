package com.bankflow.service;

import com.bankflow.dto.request.BeneficiaryRequest;
import com.bankflow.dto.response.BeneficiaryResponse;
import com.bankflow.entity.Beneficiary;
import com.bankflow.entity.User;
import com.bankflow.exception.DuplicateResourceException;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.mapper.BeneficiaryMapper;
import com.bankflow.repository.BeneficiaryRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing payee beneficiaries for quick transfers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final BeneficiaryMapper beneficiaryMapper;

    @Transactional
    public BeneficiaryResponse addBeneficiary(Long userId, BeneficiaryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String accountNumber = request.getAccountNumber().trim();

        if (beneficiaryRepository.existsByUserIdAndAccountNumber(userId, accountNumber)) {
            throw new DuplicateResourceException("Beneficiary with account number " + accountNumber + " already exists in your payees");
        }

        Beneficiary beneficiary = beneficiaryMapper.toEntity(request, user);
        Beneficiary saved = beneficiaryRepository.save(beneficiary);

        log.info("Beneficiary added: {} (A/C: {}) for user: {}",
                saved.getBeneficiaryName(), saved.getAccountNumber(), user.getEmail());

        return beneficiaryMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getUserBeneficiaries(Long userId) {
        return beneficiaryRepository.findByUserId(userId).stream()
                .map(beneficiaryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBeneficiary(Long userId, Long beneficiaryId) {
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUserId(beneficiaryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found with id: " + beneficiaryId));

        beneficiaryRepository.delete(beneficiary);
        log.info("Beneficiary id {} deleted by user id {}", beneficiaryId, userId);
    }
}
