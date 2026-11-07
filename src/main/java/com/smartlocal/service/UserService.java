package com.smartlocal.service;

import com.smartlocal.dto.UserRegistrationDto;
import com.smartlocal.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

/**
 * Service managing user accounts, registration, and authentication details.
 */
public interface UserService extends UserDetailsService {

    /**
     * Register a new user (Customer or Retailer).
     * Prevents duplicate emails and forbids public Admin self-assignment.
     */
    User registerUser(UserRegistrationDto registrationDto);

    /**
     * Find a user by email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an email is already registered.
     */
    boolean existsByEmail(String email);

    /**
     * Check if an account exists and has completed email verification.
     */
    boolean isEmailVerified(String email);

    /**
     * Activates an account and marks the email as verified upon successful OTP entry.
     */
    void markEmailAsVerified(String email);

    /**
     * Initialize sample accounts for testing all three roles: Customer, Retailer, and Admin.
     */
    void initializeDefaultAccounts();
}
