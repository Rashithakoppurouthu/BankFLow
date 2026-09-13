package com.bankflow.controller;

import com.bankflow.dto.response.ApiResponse;
import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.entity.RoleType;
import com.bankflow.entity.TransactionType;
import com.bankflow.security.UserDetailsImpl;
import com.bankflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller handling Transaction History queries, pagination, and date filtering.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Transactions", description = "Endpoints for querying and filtering transaction ledger history")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get paginated, filtered transaction history for an account")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @RequestParam Long accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleType.ROLE_ADMIN.name()));

        Page<TransactionResponse> transactions = transactionService.getTransactions(
                accountId,
                userPrincipal.getId(),
                type,
                fromDate,
                toDate,
                page,
                size,
                sortBy,
                sortDir,
                isAdmin
        );

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get most recent transactions across all accounts of the logged-in user")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getRecentTransactions(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @RequestParam(defaultValue = "5") int limit) {

        List<TransactionResponse> recent = transactionService.getRecentTransactions(userPrincipal.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(recent));
    }
}
