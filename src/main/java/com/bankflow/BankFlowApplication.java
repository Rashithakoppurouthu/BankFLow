package com.bankflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ============================================================================
 * BankFlow - Enterprise Banking & Transaction Management System
 * ============================================================================
 * 
 * Main Spring Boot Application Entry Point.
 * 
 * Annotations explained for Interviews:
 * - @SpringBootApplication: Meta-annotation combining:
 *   1. @Configuration: Tags the class as a source of bean definitions.
 *   2. @EnableAutoConfiguration: Tells Spring Boot to automatically configure beans
 *      based on classpath dependencies (e.g. DataSource, Jackson, Tomcat).
 *   3. @ComponentScan: Scans for Spring components (@Component, @Service,
 *      @Repository, @RestController) starting from this package (com.bankflow) downwards.
 * 
 * - @EnableJpaAuditing: Activates auditing support for JPA entities, automatically
 *   populating @CreatedDate and @LastModifiedDate fields on insert/update.
 * 
 * - @EnableTransactionManagement: Enables Spring's declarative transaction management
 *   via @Transactional proxies.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class BankFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankFlowApplication.class, args);
    }
}
