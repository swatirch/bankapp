package com.banking.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // manually inject what @Value would inject in real Spring context
        ReflectionTestUtils.setField(jwtService, "secret",
                "test-secret-key-must-be-at-least-256-bits-long-for-hmac");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);

        testUser = new UserEntity();
        testUser.setId("user-123");
        testUser.setEmail("rahul@bank.com");
        testUser.setPassword("hashed_password");
        testUser.setRole(Role.CUSTOMER);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateToken_shouldContainThreeParts() {
        String token = jwtService.generateToken(testUser);
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateToken(testUser);
        String email = jwtService.extractEmail(token);
        assertThat(email).isEqualTo("rahul@bank.com");
    }

    @Test
    void isValidToken_withValidToken_shouldReturnTrue() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    void isTokenValid_withDifferentUser_shouldReturnFalse() {
        String token = jwtService.generateToken(testUser);

        UserEntity otherUser = new UserEntity();
        otherUser.setEmail("other@bank.com");
        otherUser.setRole(Role.CUSTOMER);
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }
}
