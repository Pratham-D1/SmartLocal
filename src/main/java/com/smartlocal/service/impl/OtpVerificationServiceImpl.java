package com.smartlocal.service.impl;

import com.smartlocal.entity.EmailOtp;
import com.smartlocal.exception.EmailDeliveryException;
import com.smartlocal.repository.EmailOtpRepository;
import com.smartlocal.service.EmailService;
import com.smartlocal.service.OtpVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Thread-safe, database-backed implementation of OtpVerificationService.
 * Stores BCrypt-hashed OTPs, enforces 5-minute expiration, 60-second resend cooldown,
 * attempt throttling, and ensures OTPs are never logged.
 */
@Service
public class OtpVerificationServiceImpl implements OtpVerificationService {

    private static final Logger log = LoggerFactory.getLogger(OtpVerificationServiceImpl.class);

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_RESENDS = 5;

    private final EmailOtpRepository emailOtpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public OtpVerificationServiceImpl(EmailOtpRepository emailOtpRepository,
                                      EmailService emailService,
                                      PasswordEncoder passwordEncoder) {
        this.emailOtpRepository = emailOtpRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    private String generateSixDigitOtp() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    private String normalize(String email) {
        return (email == null) ? "" : email.trim().toLowerCase();
    }

    @Override
    @Transactional
    public void generateAndSendOtp(String email, String recipientName) throws EmailDeliveryException {
        String cleanEmail = normalize(email);

        // Invalidate any previously active OTPs for this email
        emailOtpRepository.invalidateAllActiveForEmail(cleanEmail);

        // Generate 6-digit OTP using SecureRandom
        String rawOtp = generateSixDigitOtp();
        String hashedOtp = passwordEncoder.encode(rawOtp);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);
        LocalDateTime resendAvailableAt = now.plusSeconds(RESEND_COOLDOWN_SECONDS);

        EmailOtp otpEntity = new EmailOtp(cleanEmail, hashedOtp, expiresAt, resendAvailableAt, 0);
        emailOtpRepository.save(otpEntity);

        // Send real email via SMTP
        emailService.sendOtpEmail(cleanEmail, rawOtp, recipientName);

        log.info("New verification OTP generated and dispatched to [{}] (valid for {} minutes)", cleanEmail, OTP_EXPIRY_MINUTES);
    }

    @Override
    @Transactional
    public ValidationResult verifyOtp(String email, String enteredOtp) {
        String cleanEmail = normalize(email);

        Optional<EmailOtp> opt = emailOtpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(cleanEmail);
        if (opt.isEmpty()) {
            return new ValidationResult(ValidationStatus.NOT_FOUND,
                    "No pending verification code found. Please request a new code or register again.");
        }

        EmailOtp otpRecord = opt.get();

        // Check if expired (5 minutes)
        if (otpRecord.isExpired()) {
            otpRecord.setUsed(true);
            emailOtpRepository.save(otpRecord);
            return new ValidationResult(ValidationStatus.EXPIRED,
                    "This verification code has expired (5-minute validity). Please click 'Resend Code'.");
        }

        // Check maximum attempt count
        if (otpRecord.getAttemptCount() >= MAX_ATTEMPTS) {
            otpRecord.setUsed(true);
            emailOtpRepository.save(otpRecord);
            return new ValidationResult(ValidationStatus.TOO_MANY_ATTEMPTS,
                    "Maximum verification attempts exceeded. For security, this code has been invalidated. Please request a new code.");
        }

        String cleanedEntered = (enteredOtp == null) ? "" : enteredOtp.trim().replaceAll("\\s+", "");

        // Verify against BCrypt hash
        if (!passwordEncoder.matches(cleanedEntered, otpRecord.getOtpHash())) {
            otpRecord.incrementAttemptCount();
            int remaining = MAX_ATTEMPTS - otpRecord.getAttemptCount();
            emailOtpRepository.save(otpRecord);

            if (remaining <= 0) {
                otpRecord.setUsed(true);
                emailOtpRepository.save(otpRecord);
                return new ValidationResult(ValidationStatus.TOO_MANY_ATTEMPTS,
                        "Maximum verification attempts exceeded. This code has been invalidated. Please request a new code.");
            }

            return new ValidationResult(ValidationStatus.INVALID_OTP,
                    "Invalid OTP code. Please check and try again (" + remaining + " attempt(s) remaining).");
        }

        // Success - invalidate OTP so it cannot be reused
        otpRecord.setUsed(true);
        emailOtpRepository.save(otpRecord);

        log.info("Email [{}] verified successfully", cleanEmail);
        return new ValidationResult(ValidationStatus.SUCCESS, "Email verified successfully.");
    }

    @Override
    @Transactional
    public ResendResult resendOtp(String email, String recipientName) throws EmailDeliveryException {
        String cleanEmail = normalize(email);

        Optional<EmailOtp> opt = emailOtpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(cleanEmail);
        int currentResendCount = 0;

        if (opt.isPresent()) {
            EmailOtp active = opt.get();

            // Enforce 60-second cooldown
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(active.getResendAvailableAt())) {
                long remainingSec = Duration.between(now, active.getResendAvailableAt()).getSeconds() + 1;
                return new ResendResult(false,
                        "Please wait " + remainingSec + " second(s) before requesting another code.", remainingSec);
            }

            // Enforce resend limit
            if (active.getResendCount() >= MAX_RESENDS) {
                return new ResendResult(false,
                        "Maximum resend limit reached. Please restart registration.", 0);
            }

            currentResendCount = active.getResendCount() + 1;
            active.setUsed(true);
            emailOtpRepository.save(active);
        }

        // Generate and hash new OTP
        String rawOtp = generateSixDigitOtp();
        String hashedOtp = passwordEncoder.encode(rawOtp);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);
        LocalDateTime resendAvailableAt = now.plusSeconds(RESEND_COOLDOWN_SECONDS);

        EmailOtp newOtp = new EmailOtp(cleanEmail, hashedOtp, expiresAt, resendAvailableAt, currentResendCount);
        emailOtpRepository.save(newOtp);

        // Send real email via SMTP
        emailService.sendOtpEmail(cleanEmail, rawOtp, recipientName);

        log.info("Fresh OTP resent to [{}] (resend count: {})", cleanEmail, currentResendCount);
        return new ResendResult(true, "A fresh verification code has been sent to your email.", RESEND_COOLDOWN_SECONDS);
    }

    @Override
    public long getResendCooldownSeconds(String email) {
        String cleanEmail = normalize(email);
        Optional<EmailOtp> opt = emailOtpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(cleanEmail);
        if (opt.isEmpty()) {
            return 0;
        }
        EmailOtp otp = opt.get();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(otp.getResendAvailableAt())) {
            return Duration.between(now, otp.getResendAvailableAt()).getSeconds() + 1;
        }
        return 0;
    }

    @Override
    public boolean hasActiveOtp(String email) {
        String cleanEmail = normalize(email);
        Optional<EmailOtp> opt = emailOtpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(cleanEmail);
        return opt.isPresent() && !opt.get().isExpired();
    }
}
