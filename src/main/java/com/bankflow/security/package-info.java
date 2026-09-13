/**
 * Security Layer:
 * 
 * - Configures Spring Security 6 filter chains, stateless session management, and CORS/CSRF policies.
 * - Implements JWT (JSON Web Token) creation, signing (HMAC-SHA256), parsing, and expiration validation.
 * - Provides JwtAuthenticationFilter to intercept HTTP requests and populate SecurityContextHolder.
 * - Secures passwords using BCryptPasswordEncoder.
 * 
 * Key Interview Concepts:
 * - Stateless authentication: Server maintains no HTTP session state; clients authenticate via Bearer token in Authorization header.
 * - SecurityFilterChain: The core chain of servlet filters processing every incoming request.
 * - BCrypt: One-way salted hashing function resistant to rainbow table and brute-force attacks.
 */
package com.bankflow.security;
