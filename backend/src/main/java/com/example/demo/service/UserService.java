package com.example.demo.service;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.dto.response.UserDTO;
import com.example.demo.exception.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepo;
import com.example.demo.util.CodeGenerator;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.PasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final int CODE_EXPIRY_MINUTES = 15;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CodeGenerator codeGenerator;

    @Autowired
    private PasswordValidator passwordValidator;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("An account with this email already exists");
        }

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidPasswordException("Passwords do not match");
        }

        // Validate password strength
        List<String> passwordErrors = passwordValidator.validate(request.getPassword());
        if (!passwordErrors.isEmpty()) {
            throw new InvalidPasswordException(passwordErrors);
        }

        // Generate verification code
        String verificationCode = codeGenerator.generateCode();
        LocalDateTime codeExpiry = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);

        // Create user with companyName and default role
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .companyName(request.getCompanyName())
                .role("USER") // Default role is USER
                .emailVerified(false)
                .enabled(false)
                .verificationCode(verificationCode)
                .verificationCodeExpiry(codeExpiry)
                .build();

        userRepository.save(user);
        logger.info("User registered successfully: {} for company: {}", user.getEmail(), user.getCompanyName());

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationCode);

        return AuthResponse.success("Registration successful! Please check your email for the verification code.");
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        // Check if already verified
        if (user.getEmailVerified()) {
            return AuthResponse.success("Email is already verified. You can login now.");
        }

        // Check verification code
        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode().toUpperCase())) {
            throw new InvalidCodeException("Invalid verification code");
        }

        // Check if code is expired
        if (user.getVerificationCodeExpiry() == null || LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
            throw new CodeExpiredException("Verification code has expired. Please request a new one.");
        }

        // Verify user
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        logger.info("Email verified successfully for user: {}", user.getEmail());

        return AuthResponse.success("Email verified successfully! You can now login.");
    }

    @Transactional
    public MessageResponse resendVerificationCode(ResendCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        // Check if already verified
        if (user.getEmailVerified()) {
            return MessageResponse.success("Email is already verified. You can login now.");
        }

        // Generate new verification code
        String verificationCode = codeGenerator.generateCode();
        LocalDateTime codeExpiry = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);

        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(codeExpiry);
        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationCode);

        logger.info("Verification code resent to: {}", user.getEmail());

        return MessageResponse.success("A new verification code has been sent to your email.");
    }

    public AuthResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check if email is verified
        if (!user.getEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in. Check your inbox for the verification code.");
        }

        // Authenticate user
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            UserDTO userDTO = mapToUserDTO(user);

            logger.info("User logged in successfully: {} (role: {}, company: {})",
                    user.getEmail(), user.getRole(), user.getCompanyName());

            return AuthResponse.success("Login successful", token, userDTO);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElse(null);

        // Don't reveal if email exists or not for security
        if (user == null) {
            return MessageResponse.success("If an account with that email exists, a password reset code has been sent.");
        }

        // Generate reset code
        String resetCode = codeGenerator.generateCode();
        LocalDateTime codeExpiry = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);

        user.setPasswordResetCode(resetCode);
        user.setPasswordResetCodeExpiry(codeExpiry);
        userRepository.save(user);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetCode);

        logger.info("Password reset code sent to: {}", user.getEmail());

        return MessageResponse.success("If an account with that email exists, a password reset code has been sent.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        // Check reset code
        if (user.getPasswordResetCode() == null || !user.getPasswordResetCode().equals(request.getCode().toUpperCase())) {
            throw new InvalidCodeException("Invalid password reset code");
        }

        // Check if code is expired
        if (user.getPasswordResetCodeExpiry() == null || LocalDateTime.now().isAfter(user.getPasswordResetCodeExpiry())) {
            throw new CodeExpiredException("Password reset code has expired. Please request a new one.");
        }

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidPasswordException("Passwords do not match");
        }

        // Validate password strength
        List<String> passwordErrors = passwordValidator.validate(request.getNewPassword());
        if (!passwordErrors.isEmpty()) {
            throw new InvalidPasswordException(passwordErrors);
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetCode(null);
        user.setPasswordResetCodeExpiry(null);
        userRepository.save(user);

        logger.info("Password reset successfully for user: {}", user.getEmail());

        return MessageResponse.success("Password has been reset successfully. You can now login with your new password.");
    }

    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return mapToUserDTO(user);
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .companyName(user.getCompanyName())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}