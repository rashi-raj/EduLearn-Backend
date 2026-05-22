package com.edulearn.auth.security;

import com.edulearn.auth.config.CustomOAuth2AuthorizationRequestResolver;
import com.edulearn.auth.entity.User;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import com.edulearn.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${application.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found from Google");
            return;
        }

        HttpSession session = request.getSession(false);

        String mode = "login";
        if (session != null) {
            Object modeObj = session.getAttribute(CustomOAuth2AuthorizationRequestResolver.GOOGLE_AUTH_MODE);
            if (modeObj != null) {
                mode = modeObj.toString();
            }
        }

        Role selectedRole = Role.STUDENT;
        if (session != null) {
            Object roleFromSession = session.getAttribute(CustomOAuth2AuthorizationRequestResolver.GOOGLE_SELECTED_ROLE);
            if (roleFromSession != null) {
                try {
                    selectedRole = Role.valueOf(roleFromSession.toString().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    selectedRole = Role.STUDENT;
                }
            }
        }

        if (selectedRole == Role.ADMIN) {
            response.sendRedirect(
                    redirectUri + "?error=" + URLEncoder.encode(
                            "Admin role is not allowed for Google authentication.",
                            StandardCharsets.UTF_8
                    )
            );
            clearSession(session);
            return;
        }

        User existingUser = userRepository.findByEmail(email).orElse(null);

        // Existing user flow
        if (existingUser != null) {
            updateGoogleProfile(existingUser, name, picture);

            if (existingUser.getRole() == Role.INSTRUCTOR &&
                    existingUser.getApprovalStatus() != ApprovalStatus.APPROVED) {

                String message = existingUser.getApprovalStatus() == ApprovalStatus.REJECTED
                        ? "Your instructor account has been rejected by admin."
                        : "Your instructor account is pending admin approval.";

                response.sendRedirect(
                        redirectUri + "?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8)
                );
                clearSession(session);
                return;
            }

            sendSuccessResponse(existingUser, response);
            clearSession(session);
            return;
        }

        // New user flow:
        // Student -> create APPROVED and log in
        // Instructor -> create PENDING and return to login with message
        ApprovalStatus approvalStatus =
                selectedRole == Role.INSTRUCTOR
                        ? ApprovalStatus.PENDING
                        : ApprovalStatus.APPROVED;

        User newUser = userRepository.save(
                User.builder()
                        .fullName(name != null && !name.isBlank() ? name : "Google User")
                        .email(email)
                        .passwordHash("OAUTH2_USER")
                        .role(selectedRole)
                        .provider(AuthProvider.GOOGLE)
                        .approvalStatus(approvalStatus)
                        .profilePicUrl(picture)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        clearSession(session);

        if (newUser.getRole() == Role.INSTRUCTOR && newUser.getApprovalStatus() != ApprovalStatus.APPROVED) {
            response.sendRedirect(
                    redirectUri + "?error=" + URLEncoder.encode(
                            "Your instructor account is pending admin approval.",
                            StandardCharsets.UTF_8
                    )
            );
            return;
        }

        sendSuccessResponse(newUser, response);
    }

    private void updateGoogleProfile(User user, String name, String picture) {
        boolean updated = false;

        if (user.getProvider() != AuthProvider.GOOGLE) {
            user.setProvider(AuthProvider.GOOGLE);
            updated = true;
        }

        if (picture != null && !picture.isBlank()) {
            user.setProfilePicUrl(picture);
            updated = true;
        }

        if (name != null && !name.isBlank()) {
            user.setFullName(name);
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
        }
    }

    private void sendSuccessResponse(User user, HttpServletResponse response) throws IOException {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        String token = jwtService.generateToken(userDetails);

        String targetUrl = redirectUri
                + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&userId=" + URLEncoder.encode(String.valueOf(user.getUserId()), StandardCharsets.UTF_8)
                + "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                + "&fullName=" + URLEncoder.encode(user.getFullName(), StandardCharsets.UTF_8)
                + "&role=" + URLEncoder.encode(user.getRole().name(), StandardCharsets.UTF_8)
                + "&approvalStatus=" + URLEncoder.encode(user.getApprovalStatus().name(), StandardCharsets.UTF_8);

        response.sendRedirect(targetUrl);
    }

    private void clearSession(HttpSession session) {
        if (session != null) {
            session.removeAttribute(CustomOAuth2AuthorizationRequestResolver.GOOGLE_SELECTED_ROLE);
            session.removeAttribute(CustomOAuth2AuthorizationRequestResolver.GOOGLE_AUTH_MODE);
        }
    }
}