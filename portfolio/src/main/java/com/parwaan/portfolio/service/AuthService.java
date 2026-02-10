package com.parwaan.portfolio.service;

import com.parwaan.portfolio.dto.AuthResponse;
import com.parwaan.portfolio.dto.ForgotPasswordRequest;
import com.parwaan.portfolio.dto.ForgotPasswordResponse;
import com.parwaan.portfolio.dto.LoginRequest;
import com.parwaan.portfolio.dto.RegisterRequest;
import com.parwaan.portfolio.dto.ResetPasswordRequest;
import com.parwaan.portfolio.exception.EmailAlreadyExistsException;
import com.parwaan.portfolio.model.PasswordResetToken;
import com.parwaan.portfolio.model.User;
import com.parwaan.portfolio.repository.PasswordResetTokenRepository;
import com.parwaan.portfolio.repository.UserRepository;
import com.parwaan.portfolio.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${app.reset-token.expiry-minutes:30}")
    private long resetTokenExpiryMinutes;

    @Value("${app.reset.base-url:https://example.com/reset-password}")
    private String resetBaseUrl;

    @Value("${app.reset.include-token-in-response:false}")
    private boolean includeResetTokenInResponse;

    public User register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new EmailAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        });

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .build();

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder().token(token).build();
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request == null ? null : request.getEmail();
        Optional<User> userOpt = email == null ? Optional.empty() : userRepository.findByEmail(email);

        String resetToken = null;
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            resetToken = UUID.randomUUID().toString();
            Instant expiresAt = Instant.now().plus(Duration.ofMinutes(resetTokenExpiryMinutes));
            PasswordResetToken token = PasswordResetToken.builder()
                    .token(resetToken)
                    .user(user)
                    .expiresAt(expiresAt)
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(token);
            String resetLink = resetBaseUrl + "?token=" + resetToken;
            emailService.sendPasswordResetLink(user.getEmail(), resetLink);
            log.info("Password reset link generated for {}", user.getEmail());
        }

        String message = "If that email exists, a reset token was generated.";
        String responseToken = includeResetTokenInResponse ? resetToken : null;
        return new ForgotPasswordResponse(message, responseToken);
    }

    public void resetPassword(ResetPasswordRequest request) {
        String tokenValue = request == null ? null : request.getToken();
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new IllegalArgumentException("Reset token is required");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (token.isUsed()) {
            throw new IllegalStateException("Reset token already used");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Reset token expired");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }
}
