package com.edulearn.auth.security;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import com.edulearn.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User buildUser(Role role) {
        return User.builder()
                .userId(UUID.randomUUID())
                .fullName("Test User")
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .role(role)
                .provider(AuthProvider.LOCAL)
                .approvalStatus(ApprovalStatus.APPROVED)
                .mobile("9876543210")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Returns UserDetails for existing email")
    void loadUserByUsername_found_returnsUserDetails() {
        User user = buildUser(Role.STUDENT);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(details);
        assertEquals("test@example.com", details.getUsername());
        assertEquals("encodedPassword", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
    }

    @Test
    @DisplayName("Instructor role is mapped to ROLE_INSTRUCTOR authority")
    void loadUserByUsername_instructor_hasCorrectAuthority() {
        User user = buildUser(Role.INSTRUCTOR);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("test@example.com");

        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSTRUCTOR")));
    }

    @Test
    @DisplayName("Admin role is mapped to ROLE_ADMIN authority")
    void loadUserByUsername_admin_hasCorrectAuthority() {
        User user = buildUser(Role.ADMIN);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("test@example.com");

        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Throws UsernameNotFoundException when user is not found")
    void loadUserByUsername_notFound_throwsException() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("ghost@example.com"));
    }
}
