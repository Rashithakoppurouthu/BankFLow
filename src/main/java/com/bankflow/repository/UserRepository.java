package com.bankflow.repository;

import com.bankflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity data access.
 * 
 * Key Interview Concepts:
 * - findByEmail: Spring Data JPA automatically derives the query
 *   "SELECT u FROM User u WHERE u.email = :email" from the method signature.
 * - existsByEmail: Generates a lightweight "SELECT COUNT(u) > 0" query without loading the whole entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByPhone(String phone);
}
