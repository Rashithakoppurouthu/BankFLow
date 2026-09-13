package com.bankflow.controller;

import com.bankflow.dto.request.TransferRequest;
import com.bankflow.dto.response.ApiResponse;
import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.entity.RoleType;
import com.bankflow.security.UserDetailsImpl;
import com.bankflow.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling funds transfers between bank accounts.
 */
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Transfers", description = "Endpoints for atomic funds transfers with concurrency locking and idempotency")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Transfer money between accounts with idempotency support")
    public ResponseEntity<ApiResponse<TransactionResponse>> transferMoney(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleType.ROLE_ADMIN.name()));

        TransactionResponse response = transferService.transferMoney(
                userPrincipal.getId(),
                request,
                idempotencyKey,
                isAdmin
        );

        return ResponseEntity.ok(ApiResponse.success("Funds transferred successfully", response));
    }
}
