package com.banking.service;

import com.banking.dto.request.LoginRequest;
import com.banking.dto.request.RegisterRequest;
import com.banking.dto.response.AuthResponse;
import com.banking.exception.BankingException;
import com.banking.exception.ErrorCode;
import com.banking.security.JwtService;
import com.banking.security.Role;
import com.banking.security.UserEntity;
import com.banking.security.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        // GUARD — email already exists
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BankingException(
                    ErrorCode.DUPLICATE_EMAIL,
                    "Email already registered: " + request.email());
        }

        // Create user
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);

        // Generate token
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        // Spring's AuthenticationManager handles verification
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        // If we reach here — credentials are valid
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BankingException(
                        ErrorCode.USER_NOT_FOUND,
                        "User not found"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}