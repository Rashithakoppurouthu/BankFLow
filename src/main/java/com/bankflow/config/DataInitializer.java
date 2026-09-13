package com.bankflow.config;

import com.bankflow.entity.*;
import com.bankflow.repository.BankAccountRepository;
import com.bankflow.repository.RoleRepository;
import com.bankflow.repository.UserRepository;
import com.bankflow.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Seeds default roles, sample admin, and initial customer with an active bank account on startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and initializing system roles and default seed data...");

        Role customerRole = roleRepository.findByName(RoleType.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_CUSTOMER)));

        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_ADMIN)));

        // Create Default System Admin if not exists
        if (!userRepository.existsByEmail("admin@bankflow.com")) {
            User admin = User.builder()
                    .firstName("System")
                    .lastName("Administrator")
                    .email("admin@bankflow.com")
                    .phone("+1234567890")
                    .password(passwordEncoder.encode("Admin@123"))
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .address("100 Wall Street, New York, NY")
                    .roles(Set.of(adminRole, customerRole))
                    .build();
            userRepository.save(admin);
            log.info("Default Admin created: admin@bankflow.com / Admin@123");
        }

        // Create Demo Customer if not exists
        if (!userRepository.existsByEmail("john.doe@bankflow.com")) {
            User customer = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@bankflow.com")
                    .phone("+1987654321")
                    .password(passwordEncoder.encode("Customer@123"))
                    .dateOfBirth(LocalDate.of(1995, 5, 15))
                    .address("221B Baker Street, London")
                    .roles(Set.of(customerRole))
                    .build();
            User savedCustomer = userRepository.save(customer);

            // Create initial active savings account with $10,000.00
            BankAccount account = BankAccount.builder()
                    .accountNumber(AccountNumberGenerator.generate())
                    .accountType(AccountType.SAVINGS)
                    .balance(new BigDecimal("10000.00"))
                    .status(AccountStatus.ACTIVE)
                    .user(savedCustomer)
                    .build();
            bankAccountRepository.save(account);

            log.info("Demo Customer created: john.doe@bankflow.com / Customer@123 with Account: {}", account.getAccountNumber());
        }

        // Create Second Demo Customer (for transfer testing) if not exists
        if (!userRepository.existsByEmail("sarah.smith@bankflow.com")) {
            User customer2 = User.builder()
                    .firstName("Sarah")
                    .lastName("Smith")
                    .email("sarah.smith@bankflow.com")
                    .phone("+1555123456")
                    .password(passwordEncoder.encode("Customer@123"))
                    .dateOfBirth(LocalDate.of(1998, 8, 20))
                    .address("456 Elm Street, Springfield")
                    .roles(Set.of(customerRole))
                    .build();
            User savedCustomer2 = userRepository.save(customer2);

            BankAccount account2 = BankAccount.builder()
                    .accountNumber(AccountNumberGenerator.generate())
                    .accountType(AccountType.CURRENT)
                    .balance(new BigDecimal("5000.00"))
                    .status(AccountStatus.ACTIVE)
                    .user(savedCustomer2)
                    .build();
            bankAccountRepository.save(account2);

            log.info("Demo Customer 2 created: sarah.smith@bankflow.com / Customer@123 with Account: {}", account2.getAccountNumber());
        }
    }
}
