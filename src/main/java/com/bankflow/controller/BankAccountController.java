package com.bankflow.controller;

import com.bankflow.dto.request.AccountCreateRequest;
import com.bankflow.dto.request.DepositRequest;
import com.bankflow.dto.request.WithdrawRequest;
import com.bankflow.dto.response.ApiResponse;
import com.bankflow.dto.response.BankAccountResponse;
import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.entity.RoleType;
import com.bankflow.security.UserDetailsImpl;
import com.bankflow.service.BankAccountService;
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
 * Controller handling Bank Account operations, deposits, and withdrawals.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Bank Accounts", description = "Endpoints for bank account creation, balances, deposits, and withdrawals")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    @Operation(summary = "Create a new bank account (SAVINGS or CURRENT)")
    public ResponseEntity<ApiResponse<BankAccountResponse>> createAccount(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @Valid @RequestBody AccountCreateRequest request) {
        BankAccountResponse response = bankAccountService.createAccount(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Account created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all bank accounts belonging to the authenticated user")
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getUserAccounts(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        List<BankAccountResponse> accounts = bankAccountService.getUserAccounts(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get bank account details by account ID")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getAccountById(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @PathVariable Long accountId) {
        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleType.ROLE_ADMIN.name()));

        BankAccountResponse account = bankAccountService.getAccountById(accountId, userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @PostMapping("/{accountId}/deposit")
    @Operation(summary = "Deposit money into bank account")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @PathVariable Long accountId,
            @Valid @RequestBody DepositRequest request) {
        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleType.ROLE_ADMIN.name()));

        TransactionResponse response = bankAccountService.deposit(accountId, userPrincipal.getId(), request, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Deposit successful", response));
    }

    @PostMapping("/{accountId}/withdraw")
    @Operation(summary = "Withdraw money from bank account")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @PathVariable Long accountId,
            @Valid @RequestBody WithdrawRequest request) {
        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleType.ROLE_ADMIN.name()));

        TransactionResponse response = bankAccountService.withdraw(accountId, userPrincipal.getId(), request, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Withdrawal successful", response));
    }
}
