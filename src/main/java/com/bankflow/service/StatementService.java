package com.bankflow.service;

import com.bankflow.dto.response.AccountStatementResponse;
import com.bankflow.dto.response.TransactionResponse;
import com.bankflow.entity.BankAccount;
import com.bankflow.entity.Transaction;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.exception.UnauthorizedException;
import com.bankflow.mapper.TransactionMapper;
import com.bankflow.repository.BankAccountRepository;
import com.bankflow.repository.TransactionRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service generating structured account statements and PDF export reports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatementService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    @Transactional(readOnly = true)
    public AccountStatementResponse getAccountStatement(
            Long accountId,
            Long userId,
            LocalDate fromDate,
            LocalDate toDate,
            boolean isAdmin
    ) {
        BankAccount account = getAuthorizedAccount(accountId, userId, isAdmin);

        Instant startInstant = (fromDate != null)
                ? fromDate.atStartOfDay().toInstant(ZoneOffset.UTC)
                : Instant.EPOCH;
        Instant endInstant = (toDate != null)
                ? toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                : Instant.now();

        List<Transaction> transactions = transactionRepository
                .findByAccountIdAndTimestampBetweenOrderByTimestampAsc(accountId, startInstant, endInstant);

        List<TransactionResponse> txResponses = transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        return AccountStatementResponse.builder()
                .accountNumber(account.getAccountNumber())
                .customerName(account.getUser().getFullName())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .currentBalance(account.getBalance())
                .periodStart(startInstant)
                .periodEnd(endInstant)
                .transactions(txResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] generatePdfStatement(
            Long accountId,
            Long userId,
            LocalDate fromDate,
            LocalDate toDate,
            boolean isAdmin
    ) {
        AccountStatementResponse statement = getAccountStatement(accountId, userId, fromDate, toDate, isAdmin);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Colors and Fonts
            Color primaryNavy = new Color(15, 23, 42); // slate-900
            Color blueAccent = new Color(37, 99, 235); // blue-600
            Color lightGray = new Color(241, 245, 249); // slate-100

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, primaryNavy);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, blueAccent);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            // Header Title
            Paragraph title = new Paragraph("BankFlow Financial Statement", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            document.add(title);

            Paragraph subtitle = new Paragraph("Official Banking Transaction Ledger", subTitleFont);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Account Information Table
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(15);

            addSummaryRow(summaryTable, "Account Holder:", statement.getCustomerName(), bodyFont);
            addSummaryRow(summaryTable, "Account Number:", statement.getAccountNumber(), bodyFont);
            addSummaryRow(summaryTable, "Account Type:", statement.getAccountType().name(), bodyFont);
            addSummaryRow(summaryTable, "Account Status:", statement.getStatus().name(), bodyFont);
            addSummaryRow(summaryTable, "Current Balance:", "$" + statement.getCurrentBalance().toString(), sectionFont);
            document.add(summaryTable);

            // Transactions Table
            Paragraph ledgerHeading = new Paragraph("Transaction Activity", sectionFont);
            ledgerHeading.setSpacingAfter(8);
            document.add(ledgerHeading);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.2f, 2.8f, 1.8f, 2.0f, 2.2f, 3.5f});

            // Table Headers
            String[] headers = {"Date & Time", "Reference", "Type", "Amount", "Balance After", "Description"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, tableHeaderFont));
                cell.setBackgroundColor(blueAccent);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Rows
            boolean alternate = false;
            for (TransactionResponse tx : statement.getTransactions()) {
                Color rowBg = alternate ? lightGray : Color.WHITE;

                table.addCell(createCell(DATE_FORMATTER.format(tx.getTimestamp().atZone(ZoneOffset.UTC)), bodyFont, rowBg, Element.ALIGN_LEFT));
                table.addCell(createCell(tx.getTransactionReference(), bodyFont, rowBg, Element.ALIGN_LEFT));
                table.addCell(createCell(tx.getType().name(), bodyFont, rowBg, Element.ALIGN_CENTER));
                table.addCell(createCell("$" + tx.getAmount().toString(), bodyFont, rowBg, Element.ALIGN_RIGHT));
                table.addCell(createCell("$" + tx.getBalanceAfterTransaction().toString(), bodyFont, rowBg, Element.ALIGN_RIGHT));
                table.addCell(createCell(tx.getDescription() != null ? tx.getDescription() : "-", bodyFont, rowBg, Element.ALIGN_LEFT));

                alternate = !alternate;
            }

            document.add(table);

            // Footer note
            Paragraph footer = new Paragraph(
                    "This is a system-generated bank statement from BankFlow. For inquiries, contact support@bankflow.com.",
                    subTitleFont
            );
            footer.setSpacingBefore(20);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF statement: ", e);
            throw new RuntimeException("Failed to generate PDF account statement: " + e.getMessage());
        }
    }

    private BankAccount getAuthorizedAccount(Long accountId, Long userId, boolean isAdmin) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));

        if (!isAdmin && !account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to access statements for this bank account");
        }

        return account;
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3);
        table.addCell(valueCell);
    }

    private PdfPCell createCell(String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        cell.setBorderColor(new Color(226, 232, 240)); // slate-200
        return cell;
    }
}
