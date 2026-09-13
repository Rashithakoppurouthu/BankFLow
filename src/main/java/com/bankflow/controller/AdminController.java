package com.bankflow.controller;

import com.bankflow.dto.response.*;
import com.bankflow.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller providing administrative operations.
 * All endpoints are strictly secured using @PreAuthorize("hasRole('ADMIN')").
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin Operations", description = "Privileged endpoints for system administrators")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "View all registered users and customers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/accounts")
    @Operation(summary = "View all bank accounts across the entire system")
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getAllAccounts() {
        List<BankAccountResponse> accounts = adminService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/transactions")
    @Operation(summary = "View all transactions system-wide (paginated)")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Page<TransactionResponse> transactions = adminService.getAllTransactions(page, size);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @PutMapping("/accounts/{id}/freeze")
    @Operation(summary = "Freeze a bank account (suspends deposits, withdrawals, and transfers)")
    public ResponseEntity<ApiResponse<BankAccountResponse>> freezeAccount(@PathVariable Long id) {
        BankAccountResponse response = adminService.freezeAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Account frozen successfully", response));
    }

    @PutMapping("/accounts/{id}/unfreeze")
    @Operation(summary = "Unfreeze a previously suspended bank account")
    public ResponseEntity<ApiResponse<BankAccountResponse>> unfreezeAccount(@PathVariable Long id) {
        BankAccountResponse response = adminService.unfreezeAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Account unfrozen successfully", response));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Retrieve system-wide analytics and financial metrics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = adminService.getDashboardStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
