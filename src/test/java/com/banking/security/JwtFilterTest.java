package com.banking.security;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.banking.controller.AccountController;
import com.banking.service.AccountService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, JwtFilter.class, JwtService.class,UserDetailsServiceImpl.class})
public class JwtFilterTest {

    @Autowired 
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void requestWithoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/account/some-id"))
        .andExpect(status().isUnauthorized());
    }
    
    @Test
    void requestWithInvalidToken__shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/accounts/some-id")
        .header("Authorization","Bearer invalid.token.here"))
        .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithValidToken_shouldReturn200OrOther() throws Exception {
        // We'll complete this test after AuthController is built
        // For now just verify the filter doesn't crash on valid token
    }
}
