/**
 * Service Layer:
 * 
 * - Encapsulates core business logic, domain rules, and workflow orchestration.
 * - Manages database transaction boundaries using @Transactional.
 * - Enforces business validations (e.g. sufficient balance, account state checks, duplicate transfer checks).
 * - Co-ordinates locking mechanisms (Pessimistic / Optimistic) during concurrent operations.
 * 
 * Key Interview Concepts:
 * - @Service registers the class as a Spring-managed singleton Bean.
 * - Constructor Injection is preferred over field injection (@Autowired) because:
 *   1. It ensures immutability (final fields).
 *   2. Prevents NullPointerExceptions by ensuring dependencies exist at instantiation.
 *   3. Simplifies unit testing without needing a Spring test context or reflection.
 */
package com.bankflow.service;
