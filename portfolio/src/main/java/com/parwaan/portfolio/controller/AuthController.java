package com.parwaan.portfolio.controller;

import com.parwaan.portfolio.dto.AuthResponse;
import com.parwaan.portfolio.dto.LoginRequest;
import com.parwaan.portfolio.dto.RegisterRequest;
import com.parwaan.portfolio.dto.UserDto;
import com.parwaan.portfolio.model.User;
import com.parwaan.portfolio.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        User registeredUser = authService.register(request);
        return new ResponseEntity<>(registeredUser.toDto(), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}