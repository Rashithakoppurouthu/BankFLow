/**
 * Controller Layer:
 * 
 * - Handles incoming HTTP requests and routes them to appropriate service methods.
 * - Validates incoming request payloads using Jakarta Bean Validation (@Valid).
 * - Maps service domain outputs into client-friendly Response DTOs.
 * - Adheres strictly to Separation of Concerns: CONTROLLERS MUST NEVER CONTAIN BUSINESS LOGIC.
 * 
 * Key Interview Concepts:
 * - @RestController = @Controller + @ResponseBody.
 * - HTTP Status Codes: 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict.
 */
package com.bankflow.controller;
