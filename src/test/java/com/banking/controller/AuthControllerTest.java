package com.banking.controller;

import com.banking.security.SecurityConfig;
import com.banking.dto.response.AuthResponse;
import com.banking.security.JwtFilter;
import com.banking.security.JwtService;
import com.banking.security.UserDetailsServiceImpl;
import com.banking.security.UserRepository;
import com.banking.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, JwtFilter.class,
        JwtService.class, UserDetailsServiceImpl.class })
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void register_shouldReturn201_andReturnToken() throws Exception {

        when(authService.register(any()))
                .thenReturn(new AuthResponse("fake-jwt-token"));

        String body = objectMapper.writeValueAsString(
                Map.of("email", "rahul@bank.com",
                        "password", "SecurePass123!"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_shouldReturn200_andReturnToken() throws Exception {

        when(authService.login(any()))
                .thenReturn(new AuthResponse("fake-jwt-token"));

        String body = objectMapper.writeValueAsString(
                Map.of("email", "rahul@bank.com",
                        "password", "SecurePass123!"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void register_withInvalidEmail_shouldReturn400() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "not-an-email",
                        "password", "SecurePass123!"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withBlankPassword_shouldReturn400() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "rahul@bank.com",
                        "password", ""));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }
}