package com.smartlocal.service;

import com.smartlocal.exception.EmailDeliveryException;

/**
 * Service managing database-backed OTP generation, hashing, verification,
 * and rate-limited resend policies.
 */
public interface OtpVerificationService {

    enum ValidationStatus {
        SUCCESS,
        INVALID_OTP,
        EXPIRED,
        ALREADY_USED,
        TOO_MANY_ATTEMPTS,
        NOT_FOUND
    }

    class ValidationResult {
        private final ValidationStatus status;
        private final String message;

        public ValidationResult(ValidationStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public ValidationStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return status == ValidationStatus.SUCCESS;
        }
    }

    class ResendResult {
        private final boolean allowed;
        private final String message;
        private final long cooldownRemainingSeconds;

        public ResendResult(boolean allowed, String message, long cooldownRemainingSeconds) {
            this.allowed = allowed;
            this.message = message;
            this.cooldownRemainingSeconds = cooldownRemainingSeconds;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getMessage() {
            return message;
        }

        public long getCooldownRemainingSeconds() {
            return cooldownRemainingSeconds;
        }
    }

    /**
     * Generates a cryptographically secure 6-digit OTP, stores its BCrypt hash
     * in the database, and sends it via EmailService.
     *
     * @param email target email address
     * @param recipientName recipient display name
     * @throws EmailDeliveryException if email delivery fails
     */
    void generateAndSendOtp(String email, String recipientName) throws EmailDeliveryException;

    /**
     * Verifies the user-submitted OTP against the stored hash.
     *
     * @param email target email address
     * @param enteredOtp 6-digit OTP code submitted by user
     * @return ValidationResult indicating outcome
     */
    ValidationResult verifyOtp(String email, String enteredOtp);

    /**
     * Resends a fresh OTP code if the 60-second cooldown and retry limits permit.
     *
     * @param email target email address
     * @param recipientName recipient display name
     * @return ResendResult indicating success or remaining cooldown
     * @throws EmailDeliveryException if email delivery fails
     */
    ResendResult resendOtp(String email, String recipientName) throws EmailDeliveryException;

    /**
     * Returns the remaining cooldown in seconds before another OTP can be requested.
     *
     * @param email target email address
     * @return remaining seconds, or 0 if cooldown has passed
     */
    long getResendCooldownSeconds(String email);

    /**
     * Checks if a pending registration OTP exists for this email.
     */
    boolean hasActiveOtp(String email);
}
