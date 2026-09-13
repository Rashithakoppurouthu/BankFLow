package com.bankflow.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility for generating unique, trackable transaction reference identifiers.
 * Format: TXN-YYYYMMDD-XXXXXX
 */
public final class TransactionReferenceGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private TransactionReferenceGenerator() {
        // Prevent instantiation
    }

    public static String generate() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        int randomPart = 100_000 + RANDOM.nextInt(900_000);
        return "TXN-" + datePart + "-" + randomPart;
    }
}
