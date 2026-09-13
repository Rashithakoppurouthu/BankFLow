package com.bankflow.controller;

import com.bankflow.dto.request.BeneficiaryRequest;
import com.bankflow.dto.response.ApiResponse;
import com.bankflow.dto.response.BeneficiaryResponse;
import com.bankflow.security.UserDetailsImpl;
import com.bankflow.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller handling beneficiary management operations.
 */
@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Beneficiaries", description = "Endpoints for managing saved payee accounts")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    @Operation(summary = "Add a new beneficiary payee")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> addBeneficiary(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @Valid @RequestBody BeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.addBeneficiary(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Beneficiary added successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all saved beneficiaries for authenticated user")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getUserBeneficiaries(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        List<BeneficiaryResponse> beneficiaries = beneficiaryService.getUserBeneficiaries(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(beneficiaries));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a beneficiary by ID")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @PathVariable Long id) {
        beneficiaryService.deleteBeneficiary(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary deleted successfully", null));
    }
}
