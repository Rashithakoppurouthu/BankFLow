package com.bankflow.controller;

import com.bankflow.dto.response.AccountStatementResponse;
import com.bankflow.dto.response.ApiResponse;
import com.bankflow.entity.RoleType;
import com.bankflow.security.UserDetailsImpl;
import com.bankflow.service.StatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller handling Account Statements and downloadable PDF statement exports.
 */
@RestController
@RequestMapping("/api/statements")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Account Statements", description = "Endpoints for viewing and downloading account statements in JSON and PDF formats")
public class StatementController {

    private final StatementService statementService;

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account statement in JSON format")
    public ResponseEntity<ApiResponse<AccountStatementResponse>> getStatement(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @PathVariable Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleType.ROLE_ADMIN.name()));

        AccountStatementResponse statement = statementService.getAccountStatement(
                accountId,
                userPrincipal.getId(),
                fromDate,
                toDate,
                isAdmin
        );

        return ResponseEntity.ok(ApiResponse.success(statement));
    }

    @GetMapping("/{accountId}/pdf")
    @Operation(summary = "Download official account statement as a formatted PDF")
    public ResponseEntity<byte[]> downloadPdfStatement(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal,
            @PathVariable Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleType.ROLE_ADMIN.name()));

        byte[] pdfBytes = statementService.generatePdfStatement(
                accountId,
                userPrincipal.getId(),
                fromDate,
                toDate,
                isAdmin
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"statement-" + accountId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
