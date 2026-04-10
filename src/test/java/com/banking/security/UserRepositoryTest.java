package com.banking.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_andFindByEmail_shouldWork() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString());
        user.setEmail("rahul@bank.com");
        user.setPassword("hashed_password");
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);

        Optional<UserEntity> found = userRepository.findByEmail("rahul@bank.com");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("rahul@bank.com");
        assertThat(found.get().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void findByEmail_whenNotExists_shouldReturnEmpty() {
        Optional<UserEntity> found = userRepository.findByEmail("ghost@bank.com");
        assertThat(found).isEmpty();
    }

    @Test
    void email_shouldBeUnique() {
        UserEntity user1 = new UserEntity();
        user1.setId(UUID.randomUUID().toString());
        user1.setEmail("duplicate@bank.com");
        user1.setPassword("pass1");
        user1.setRole(Role.CUSTOMER);

        UserEntity user2 = new UserEntity();
        user2.setId(UUID.randomUUID().toString());
        user2.setEmail("duplicate@bank.com"); // same email
        user2.setPassword("pass2");
        user2.setRole(Role.CUSTOMER);

        userRepository.save(user1);

        // second save with same email must throw
        assertThrows(
                Exception.class,
                () -> userRepository.saveAndFlush(user2)
        );
    }
}   