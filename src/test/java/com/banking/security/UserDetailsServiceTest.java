package com.banking.security;

import static org.mockito.Mockito.when;

import java.util.Optional;

import org.h2.engine.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
       UserEntity user = new UserEntity();
        user.setId("user-123");
        user.setEmail("rahul@bank.com");
        user.setPassword("hashed_password");
        user.setRole(Role.CUSTOMER);

        when(userRepository.findByEmail("rahul@bank.com"))
        .thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("rahul@bank.com");
    
        assertThat(result.getUsername()).isEqualTo("rahul@bank.com");
        assertThat(result.getPassword()).isEqualTo("hashed_password");
    }

    @Test
    void loadUserByUsername_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail("ghost@bank.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername("ghost@bank.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost@bank.com");
    }

}
