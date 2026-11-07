package com.smartlocal.controller;

import com.smartlocal.dto.UserRegistrationDto;
import com.smartlocal.entity.User;
import com.smartlocal.exception.EmailDeliveryException;
import com.smartlocal.service.OtpVerificationService;
import com.smartlocal.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller handling user authentication, registration with real email OTP verification,
 * and access control.
 */
@Controller
public class AuthController {

    private final UserService userService;
    private final OtpVerificationService otpVerificationService;

    @Autowired
    public AuthController(UserService userService,
                          OtpVerificationService otpVerificationService) {
        this.userService = userService;
        this.otpVerificationService = otpVerificationService;
    }

    /**
     * Renders login page with status feedback (errors, logout notice, registration/verification success).
     */
    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "verified", required = false) String verified,
            HttpServletRequest request,
            Model model) {

        if (error != null) {
            HttpSession session = request.getSession(false);
            Object lastException = (session != null) ? session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION") : null;
            if (lastException instanceof DisabledException) {
                model.addAttribute("errorMessage", "Your email address is not verified yet. Please complete verification before logging in.");
            } else {
                model.addAttribute("errorMessage", "Invalid email or password. Please check your credentials.");
            }
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (verified != null) {
            model.addAttribute("successMessage", "Email verified successfully! You can now log in to your account.");
        } else if (registered != null) {
            model.addAttribute("successMessage", "Registration complete! Please log in.");
        }

        return "login";
    }

    /**
     * Renders registration page for Customer or Retailer accounts.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("userDto")) {
            UserRegistrationDto userDto = new UserRegistrationDto();
            userDto.setRole("CUSTOMER"); // Default friendly selection
            model.addAttribute("userDto", userDto);
        }
        return "register";
    }

    /**
     * Processes registration form submission with strict server-side validation,
     * creates an unverified account, and dispatches a real email OTP.
     */
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("userDto") UserRegistrationDto userDto,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        // 1. Validate password confirmation match
        if (userDto.getPassword() != null && userDto.getConfirmPassword() != null
                && !userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "diff.password", "Passwords do not match.");
        }

        // 2. Prevent duplicate email registration (Each email can only register one verified account)
        String normalizedEmail = userDto.getEmail() != null ? userDto.getEmail().trim().toLowerCase() : "";
        if (!normalizedEmail.isBlank() && userService.isEmailVerified(normalizedEmail)) {
            bindingResult.rejectValue("email", "duplicate.email", "This email address is already registered. Each email can only be registered once.");
        }

        // 3. Strict role validation - reject any illegal role injection attempt
        if (userDto.getRole() != null) {
            String roleUpper = userDto.getRole().trim().toUpperCase();
            if (!"CUSTOMER".equals(roleUpper) && !"RETAILER".equals(roleUpper)) {
                bindingResult.rejectValue("role", "invalid.role", "Public registration only allows Customer or Retailer.");
            }
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        // 4. Save unverified account & dispatch real email OTP via SMTP
        try {
            userService.registerUser(userDto);
            otpVerificationService.generateAndSendOtp(normalizedEmail, userDto.getFullName());

            if (session != null) {
                session.setAttribute("PENDING_VERIFY_EMAIL", normalizedEmail);
            }

            return "redirect:/verify-otp?email=" + URLEncoder.encode(normalizedEmail, StandardCharsets.UTF_8);

        } catch (EmailDeliveryException ex) {
            // Real email sending failed - do NOT pretend it succeeded!
            model.addAttribute("errorMessage", "Email delivery error: " + ex.getMessage());
            return "register";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }

    /**
     * Displays the email OTP verification page.
     * Never displays the OTP on the website.
     */
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(
            @RequestParam(value = "email", required = false) String emailParam,
            @RequestParam(value = "resent", required = false) String resent,
            @RequestParam(value = "cooldownError", required = false) String cooldownError,
            @RequestParam(value = "deliveryError", required = false) String deliveryError,
            HttpSession session,
            Model model) {

        String email = (emailParam != null && !emailParam.isBlank())
                ? emailParam.trim().toLowerCase()
                : (session != null ? (String) session.getAttribute("PENDING_VERIFY_EMAIL") : null);

        if (email == null || email.isBlank()) {
            return "redirect:/register";
        }

        if (userService.isEmailVerified(email)) {
            return "redirect:/login?verified=true";
        }

        if (!userService.existsByEmail(email)) {
            model.addAttribute("errorMessage", "No registration record found for this email. Please register first.");
            return "redirect:/register";
        }

        long cooldownSeconds = otpVerificationService.getResendCooldownSeconds(email);

        model.addAttribute("email", email);
        model.addAttribute("cooldownSeconds", cooldownSeconds);

        if (resent != null) {
            model.addAttribute("successMessage", "A fresh verification code has been dispatched to your email address.");
        }
        if (cooldownError != null) {
            model.addAttribute("errorMessage", cooldownError);
        }
        if (deliveryError != null) {
            model.addAttribute("errorMessage", "Email delivery failed: " + deliveryError);
        }

        return "verify-otp";
    }

    /**
     * Validates submitted OTP and activates the user account upon success.
     */
    @PostMapping("/verify-otp")
    public String processVerifyOtp(
            @RequestParam("email") String emailParam,
            @RequestParam("otp") String otpParam,
            HttpSession session,
            Model model) {

        String email = (emailParam != null) ? emailParam.trim().toLowerCase() : "";

        if (userService.isEmailVerified(email)) {
            return "redirect:/login?verified=true";
        }

        OtpVerificationService.ValidationResult result = otpVerificationService.verifyOtp(email, otpParam);

        if (!result.isSuccess()) {
            long cooldownSeconds = otpVerificationService.getResendCooldownSeconds(email);
            model.addAttribute("email", email);
            model.addAttribute("cooldownSeconds", cooldownSeconds);
            model.addAttribute("errorMessage", result.getMessage());
            return "verify-otp";
        }

        // Successfully verified! Activate the user account
        try {
            userService.markEmailAsVerified(email);
            if (session != null) {
                session.removeAttribute("PENDING_VERIFY_EMAIL");
            }
            return "redirect:/login?verified=true";
        } catch (IllegalArgumentException ex) {
            long cooldownSeconds = otpVerificationService.getResendCooldownSeconds(email);
            model.addAttribute("email", email);
            model.addAttribute("cooldownSeconds", cooldownSeconds);
            model.addAttribute("errorMessage", ex.getMessage());
            return "verify-otp";
        }
    }

    /**
     * Resends a fresh OTP code with strict 60-second cooldown enforcement.
     */
    @RequestMapping(value = "/resend-otp", method = {RequestMethod.GET, RequestMethod.POST})
    public String resendOtp(
            @RequestParam(value = "email", required = false) String emailParam,
            HttpSession session,
            Model model) {

        String email = (emailParam != null && !emailParam.isBlank())
                ? emailParam.trim().toLowerCase()
                : (session != null ? (String) session.getAttribute("PENDING_VERIFY_EMAIL") : null);

        if (email == null || email.isBlank()) {
            return "redirect:/register";
        }

        if (userService.isEmailVerified(email)) {
            return "redirect:/login?verified=true";
        }

        String recipientName = userService.findByEmail(email).map(User::getFullName).orElse("Valued User");

        try {
            OtpVerificationService.ResendResult resendResult = otpVerificationService.resendOtp(email, recipientName);

            if (!resendResult.isAllowed()) {
                return "redirect:/verify-otp?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                        + "&cooldownError=" + URLEncoder.encode(resendResult.getMessage(), StandardCharsets.UTF_8);
            }

            return "redirect:/verify-otp?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&resent=true";

        } catch (EmailDeliveryException ex) {
            return "redirect:/verify-otp?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                    + "&deliveryError=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Displays a friendly 403 Forbidden / Access Denied screen.
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
