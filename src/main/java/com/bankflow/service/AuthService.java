package com.bankflow.service;

import com.bankflow.dto.request.LoginRequest;
import com.bankflow.dto.request.RegisterRequest;
import com.bankflow.dto.request.UpdateProfileRequest;
import com.bankflow.dto.response.AuthResponse;
import com.bankflow.dto.response.UserResponse;
import com.bankflow.entity.*;
import com.bankflow.exception.DuplicateResourceException;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.mapper.UserMapper;
import com.bankflow.repository.BankAccountRepository;
import com.bankflow.repository.RoleRepository;
import com.bankflow.repository.UserRepository;
import com.bankflow.security.JwtUtils;
import com.bankflow.security.UserDetailsImpl;
import com.bankflow.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service managing user registration, login, JWT token issuance, and profile updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email " + normalizedEmail + " already exists");
        }

        if (userRepository.existsByPhone(request.getPhone().trim())) {
            throw new DuplicateResourceException("Phone number " + request.getPhone() + " is already registered");
        }

        Role customerRole = roleRepository.findByName(RoleType.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_CUSTOMER)));

        User user = userMapper.toEntity(request, passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(customerRole));
        User savedUser = userRepository.save(user);

        // Auto-provision an initial active Savings Account with zero initial balance
        BankAccount initialAccount = BankAccount.builder()
                .accountNumber(AccountNumberGenerator.generate())
                .accountType(AccountType.SAVINGS)
                .balance(BigDecimal.ZERO.setScale(2))
                .status(AccountStatus.ACTIVE)
                .user(savedUser)
                .build();
        bankAccountRepository.save(initialAccount);

        log.info("New customer registered successfully: {} with Account: {}", savedUser.getEmail(), initialAccount.getAccountNumber());

        List<String> roleNames = List.of(RoleType.ROLE_CUSTOMER.name());
        String token = jwtUtils.generateTokenFromEmail(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getFullName(),
                roleNames
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .name(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(RoleType.ROLE_CUSTOMER.name())
                .roles(roleNames)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String primaryRole = roles.contains(RoleType.ROLE_ADMIN.name())
                ? RoleType.ROLE_ADMIN.name()
                : RoleType.ROLE_CUSTOMER.name();

        log.info("User logged in successfully: {}", normalizedEmail);

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .userId(userPrincipal.getId())
                .name(userPrincipal.getFullName())
                .email(userPrincipal.getUsername())
                .role(primaryRole)
                .roles(roles)
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhone(request.getPhone().trim());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress().trim());

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", updatedUser.getEmail());
        return userMapper.toResponse(updatedUser);
    }
}
