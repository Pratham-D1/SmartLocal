package com.smartlocal.service;

import com.smartlocal.exception.EmailDeliveryException;

/**
 * Service for sending real email notifications and verification OTPs via SMTP.
 */
public interface EmailService {

    /**
     * Sends an HTML verification email with a 6-digit OTP to the recipient.
     *
     * @param toEmail target email address
     * @param otpCode 6-digit OTP
     * @param recipientName recipient full name
     * @throws EmailDeliveryException if SMTP credentials are missing or sending fails
     */
    void sendOtpEmail(String toEmail, String otpCode, String recipientName) throws EmailDeliveryException;
}
