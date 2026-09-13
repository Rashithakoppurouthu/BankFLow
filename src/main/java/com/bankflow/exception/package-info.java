/**
 * Exception Handling Layer:
 * 
 * - Defines domain-specific business exceptions (e.g. InsufficientBalanceException, AccountFrozenException).
 * - Implements GlobalExceptionHandler using @RestControllerAdvice.
 * - Standardizes API error responses into structured JSON payloads with timestamp, HTTP status, error code, message, and path.
 * 
 * Key Interview Concepts:
 * - @RestControllerAdvice: Combines @ControllerAdvice and @ResponseBody to intercept exceptions globally across all controllers.
 * - Clean status code mapping: Domain exceptions translate directly to 400, 401, 403, 404, or 409 responses.
 */
package com.bankflow.exception;
