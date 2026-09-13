package com.bankflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic Spring Boot Context Load Test.
 * 
 * Key Interview Concepts:
 * - @SpringBootTest: Tells Spring Boot to look for a main configuration class (like @SpringBootApplication)
 *   and use it to start an application context for testing.
 * - Context loading test verifies that all beans, configurations, and component scans can be instantiated
 *   without dependency injection cycles or configuration errors.
 */
@SpringBootTest
@ActiveProfiles("test")
class BankFlowApplicationTests {

    @Test
    @DisplayName("Verify Spring Application Context loads successfully")
    void contextLoads() {
        // Test passes if application context initializes without throwing an exception
    }
}
