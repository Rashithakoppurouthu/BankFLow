/**
 * DTO Layer (Data Transfer Objects):
 * 
 * - Decouples internal database entities from external API request/response contracts.
 * - Prevents over-posting vulnerabilities (e.g. clients attempting to send "balance" directly in requests).
 * - Avoids infinite JSON recursion issues with bidirectional JPA relationships.
 * - Enforces Jakarta Bean Validation rules (@NotNull, @Positive, @Email) on incoming payloads.
 * 
 * Key Interview Concepts:
 * - Why DTOs over Entities? Security (hide password hashes), performance (return only required columns),
 *   versioning flexibility, and domain isolation.
 */
package com.bankflow.dto;
