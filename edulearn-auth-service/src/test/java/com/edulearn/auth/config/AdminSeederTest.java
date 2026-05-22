package com.edulearn.auth.config;

import com.edulearn.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminSeeder Unit Tests")
class AdminSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminProperties adminProperties;

    @InjectMocks
    private AdminSeeder adminSeeder;

    @Test
    @DisplayName("run: seeds admin user when not exists")
    void run_seedsUser_whenNotExists() {
        when(adminProperties.getEmail()).thenReturn("admin@edulearn.com");
        when(adminProperties.getFullName()).thenReturn("System Admin");
        when(adminProperties.getPassword()).thenReturn("admin123");
        when(userRepository.existsByEmail("admin@edulearn.com")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-pwd");

        adminSeeder.run();

        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("run: does nothing when admin user already exists")
    void run_doesNothing_whenExists() {
        when(adminProperties.getEmail()).thenReturn("admin@edulearn.com");
        when(userRepository.existsByEmail("admin@edulearn.com")).thenReturn(true);

        adminSeeder.run();

        verify(userRepository, never()).save(any());
    }
}
