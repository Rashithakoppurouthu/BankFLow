package com.bankflow.util;

import java.security.SecureRandom;

/**
 * Utility for generating unique, bank-standard account numbers.
 */
public final class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String BANK_PREFIX = "100"; // BankFlow Routing Identifier

    private AccountNumberGenerator() {
        // Prevent instantiation
    }

    /**
     * Generates a 12-digit account number starting with standard bank prefix.
     */
    public static String generate() {
        long randomSuffix = 100_000_000L + (long) (RANDOM.nextDouble() * 900_000_000L);
        return BANK_PREFIX + randomSuffix;
    }
}
