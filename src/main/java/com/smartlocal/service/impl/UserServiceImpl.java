package com.smartlocal.service.impl;

import com.smartlocal.dto.UserRegistrationDto;
import com.smartlocal.entity.Role;
import com.smartlocal.entity.User;
import com.smartlocal.repository.UserRepository;
import com.smartlocal.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

/**
 * Implementation of UserService with BCrypt password hashing,
 * duplicate email checks, unverified account protection, and Spring Security integration.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        String normalizedEmail = registrationDto.getEmail().trim().toLowerCase();

        // 1. Check for duplicate email (if verified, cannot re-register)
        Optional<User> existingUserOpt = userRepository.findByEmail(normalizedEmail);
        if (existingUserOpt.isPresent() && existingUserOpt.get().isEmailVerified()) {
            throw new IllegalArgumentException("An account with email '" + normalizedEmail + "' is already registered and verified.");
        }

        // 2. Check password matching
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match.");
        }

        // 3. Strict role mapping - PUBLIC REGISTRATION NEVER ALLOWS ADMIN
        Role assignedRole;
        String requestedRole = registrationDto.getRole().trim().toUpperCase();
        if ("CUSTOMER".equals(requestedRole)) {
            assignedRole = Role.ROLE_CUSTOMER;
        } else if ("RETAILER".equals(requestedRole)) {
            assignedRole = Role.ROLE_RETAILER;
        } else {
            throw new IllegalArgumentException("Invalid role selected. Public registration only allows Customer or Retailer.");
        }

        // 4. Secure password hashing with BCrypt
        String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());

        // 5. If user was previously registered but unverified, update details; otherwise create new
        User user;
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            user.setFullName(registrationDto.getFullName().trim());
            user.setPassword(hashedPassword);
            user.setPhone(registrationDto.getPhone() != null ? registrationDto.getPhone().trim() : "");
            user.setRole(assignedRole);
            user.setEnabled(false);
            user.setEmailVerified(false);
        } else {
            user = new User(
                    registrationDto.getFullName().trim(),
                    normalizedEmail,
                    hashedPassword,
                    registrationDto.getPhone() != null ? registrationDto.getPhone().trim() : "",
                    assignedRole,
                    false // emailVerified = false until OTP verification
            );
            user.setEnabled(false);
        }

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    public boolean isEmailVerified(String email) {
        if (email == null) {
            return false;
        }
        return userRepository.findByEmail(email.trim().toLowerCase())
                .map(User::isEmailVerified)
                .orElse(false);
    }

    @Override
    @Transactional
    public void markEmailAsVerified(String email) {
        if (email == null) return;
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No account found with email: " + email));
        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No account found with email: " + email));

        // Enforce: Never allow login for an unverified account
        boolean activeAndVerified = user.isEnabled() && user.isEmailVerified();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                activeAndVerified,
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    /**
     * Seeds initial demo accounts for evaluation across all 3 roles:
     * - Administrator: admin@smartlocal.com
     * - Customers: customer@smartlocal.com, priya.customer@smartlocal.com, amit.customer@smartlocal.com
     * - Retailers: retailer@smartlocal.com, dailyneeds@smartlocal.com, greenbasket@smartlocal.com,
     *              citydairy@smartlocal.com, smartstore@smartlocal.com
     */
    @Override
    @PostConstruct
    @Transactional
    public void initializeDefaultAccounts() {
        // 1. Administrator
        seedAccountIfMissing("Demo Administrator", "admin@smartlocal.com", "Admin@123", "+91 98765 43203", Role.ROLE_ADMIN);

        // 2. Customers
        seedAccountIfMissing("Rahul Sharma", "customer@smartlocal.com", "Customer@123", "+91 98765 43201", Role.ROLE_CUSTOMER);
        seedAccountIfMissing("Priya Deshmukh", "priya.customer@smartlocal.com", "Customer@123", "+91 98765 43204", Role.ROLE_CUSTOMER);
        seedAccountIfMissing("Amit Verma", "amit.customer@smartlocal.com", "Customer@123", "+91 98765 43205", Role.ROLE_CUSTOMER);

        // 3. Retailers
        seedAccountIfMissing("Vikram Malhotra", "retailer@smartlocal.com", "Retailer@123", "+91 98765 43210", Role.ROLE_RETAILER);
        seedAccountIfMissing("Sunita Agarwal", "dailyneeds@smartlocal.com", "Retailer@123", "+91 98765 43211", Role.ROLE_RETAILER);
        seedAccountIfMissing("Rajesh Kulkarni", "greenbasket@smartlocal.com", "Retailer@123", "+91 98765 43212", Role.ROLE_RETAILER);
        seedAccountIfMissing("Mahesh Patil", "citydairy@smartlocal.com", "Retailer@123", "+91 98765 43213", Role.ROLE_RETAILER);
        seedAccountIfMissing("Sneha Joshi", "smartstore@smartlocal.com", "Retailer@123", "+91 98765 43214", Role.ROLE_RETAILER);

        // Ensure any existing demo accounts in database are verified
        userRepository.findAll().forEach(u -> {
            if (!u.isEmailVerified() && (u.getRole() == Role.ROLE_ADMIN || u.getEmail().endsWith("@smartlocal.com"))) {
                u.setEmailVerified(true);
                u.setEnabled(true);
                userRepository.save(u);
            }
        });
    }

    private void seedAccountIfMissing(String fullName, String email, String rawPassword, String phone, Role role) {
        Optional<User> existing = userRepository.findByEmail(email.toLowerCase());
        if (existing.isEmpty()) {
            User user = new User(
                    fullName,
                    email.toLowerCase(),
                    passwordEncoder.encode(rawPassword),
                    phone,
                    role,
                    true // Demo accounts are pre-verified
            );
            user.setEnabled(true);
            userRepository.save(user);
        } else {
            User u = existing.get();
            if (!u.isEmailVerified()) {
                u.setEmailVerified(true);
                u.setEnabled(true);
                userRepository.save(u);
            }
        }
    }
}
