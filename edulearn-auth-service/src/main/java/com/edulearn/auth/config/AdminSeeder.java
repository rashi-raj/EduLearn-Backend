package com.edulearn.auth.config;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import com.edulearn.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminProperties.getEmail())) {
            return;
        }

        User admin = User.builder()
                .fullName(adminProperties.getFullName())
                .email(adminProperties.getEmail())
                .passwordHash(passwordEncoder.encode(adminProperties.getPassword()))
                .role(Role.ADMIN)
                .provider(AuthProvider.LOCAL)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();

        userRepository.save(admin);
    }
}