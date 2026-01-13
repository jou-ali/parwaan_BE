package com.parwaan.portfolio.service;

import com.parwaan.portfolio.dto.AuthResponse;
import com.parwaan.portfolio.dto.LoginRequest;
import com.parwaan.portfolio.dto.RegisterRequest;
import com.parwaan.portfolio.exception.EmailAlreadyExistsException;
import com.parwaan.portfolio.model.User;
import com.parwaan.portfolio.repository.UserRepository;
import com.parwaan.portfolio.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

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
}