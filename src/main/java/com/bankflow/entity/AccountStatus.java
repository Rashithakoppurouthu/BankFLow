package com.bankflow.entity;

/**
 * Lifecycle states of a bank account.
 * Only ACTIVE accounts may participate in deposits, withdrawals, or transfers.
 */
public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED
}
