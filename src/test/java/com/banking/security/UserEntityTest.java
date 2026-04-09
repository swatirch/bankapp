package com.banking.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void shouldHaveRequiredFields(){
        UserEntity user = new UserEntity();
        user.setId("user-123");
        user.setEmail("rahul@bank.com");
        user.setPassword("hashed_password");
        user.setRole(Role.CUSTOMER);

        assertThat(user.getId()).isEqualTo("user-123");
        assertThat(user.getEmail()).isEqualTo("rahul@bank.com");
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void shouldImplementUserDetails(){
        UserEntity user = new UserEntity();
        user.setEmail("rahul@bank.com");
        user.setPassword("hashed_password");
        user.setRole(Role.CUSTOMER);

        // UserDetails contract
        assertThat(user.getUsername()).isEqualTo("rahul@bank.com");
        assertThat(user.getPassword()).isEqualTo("hashed_password");
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).isNotEmpty();
    }

    @Test
    void userEntity_adminRole_shouldHaveAdminAuthority() {
        UserEntity user = new UserEntity();
        user.setRole(Role.ADMIN);

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }
    
    @Test
    void userEntity_customerRole_shouldHaveCustomerAuthority() {
        UserEntity user = new UserEntity();
        user.setRole(Role.CUSTOMER);

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CUSTOMER");
    }
}

