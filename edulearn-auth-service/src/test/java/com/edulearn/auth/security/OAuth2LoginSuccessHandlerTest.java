package com.edulearn.auth.security;

import com.edulearn.auth.config.CustomOAuth2AuthorizationRequestResolver;
import com.edulearn.auth.entity.User;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import com.edulearn.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2LoginSuccessHandler Unit Tests")
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private HttpSession session;

    @InjectMocks
    private OAuth2LoginSuccessHandler successHandler;

    private final String REDIRECT_URI = "http://localhost:4200/login";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(successHandler, "redirectUri", REDIRECT_URI);
        lenient().when(authentication.getPrincipal()).thenReturn(oAuth2User);
        lenient().when(request.getSession(false)).thenReturn(session);
    }

    @Nested
    @DisplayName("onAuthenticationSuccess Flow")
    class OnAuthenticationSuccess {

        @Test
        @DisplayName("Returns 400 if email is missing from Google")
        void emailMissing_returnsError() throws Exception {
            when(oAuth2User.getAttribute("email")).thenReturn(null);

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
        }

        @Test
        @DisplayName("Redirects with error if selected role is ADMIN")
        void adminRole_redirectsWithError() throws Exception {
            when(oAuth2User.getAttribute("email")).thenReturn("admin@gmail.com");
            lenient().when(session.getAttribute(CustomOAuth2AuthorizationRequestResolver.GOOGLE_SELECTED_ROLE)).thenReturn("ADMIN");

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(response).sendRedirect(contains("error="));
            verify(session, times(2)).removeAttribute(anyString());
        }

        @Test
        @DisplayName("New Student: creates APPROVED user and redirects with token")
        void newStudent_createsUserAndRedirects() throws Exception {
            when(oAuth2User.getAttribute("email")).thenReturn("new@gmail.com");
            when(oAuth2User.getAttribute("name")).thenReturn("New User");
            lenient().when(session.getAttribute(CustomOAuth2AuthorizationRequestResolver.GOOGLE_SELECTED_ROLE)).thenReturn("STUDENT");
            when(userRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());
            
            User savedUser = User.builder()
                    .userId(UUID.randomUUID())
                    .email("new@gmail.com")
                    .fullName("New User")
                    .passwordHash("OAUTH2_USER")
                    .role(Role.STUDENT)
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .build();
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtService.generateToken(any())).thenReturn("mock-token");

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(userRepository).save(any(User.class));
            verify(response).sendRedirect(contains("token=mock-token"));
        }

        @Test
        @DisplayName("New Instructor: creates PENDING user and redirects with error message")
        void newInstructor_createsPendingAndRedirectsWithError() throws Exception {
            when(oAuth2User.getAttribute("email")).thenReturn("new-inst@gmail.com");
            lenient().when(session.getAttribute(CustomOAuth2AuthorizationRequestResolver.GOOGLE_SELECTED_ROLE)).thenReturn("INSTRUCTOR");
            when(userRepository.findByEmail("new-inst@gmail.com")).thenReturn(Optional.empty());

            User savedUser = User.builder()
                    .userId(UUID.randomUUID())
                    .email("new-inst@gmail.com")
                    .passwordHash("OAUTH2_USER")
                    .role(Role.INSTRUCTOR)
                    .approvalStatus(ApprovalStatus.PENDING)
                    .build();
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(response).sendRedirect(contains("error="));
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Existing Instructor (PENDING): redirects with error message")
        void existingInstructorPending_redirectsWithError() throws Exception {
            when(oAuth2User.getAttribute("email")).thenReturn("pending@gmail.com");
            
            User existingUser = User.builder()
                    .userId(UUID.randomUUID())
                    .email("pending@gmail.com")
                    .passwordHash("encoded-pwd")
                    .role(Role.INSTRUCTOR)
                    .approvalStatus(ApprovalStatus.PENDING)
                    .provider(AuthProvider.LOCAL)
                    .build();
            when(userRepository.findByEmail("pending@gmail.com")).thenReturn(Optional.of(existingUser));

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(response).sendRedirect(contains("error="));
        }

        @Test
        @DisplayName("Existing User: updates profile and redirects with token")
        void existingUser_updatesAndRedirects() throws Exception {
            when(oAuth2User.getAttribute("email")).thenReturn("existing@gmail.com");
            when(oAuth2User.getAttribute("name")).thenReturn("Updated Name");
            when(oAuth2User.getAttribute("picture")).thenReturn("new-pic.jpg");

            User existingUser = User.builder()
                    .userId(UUID.randomUUID())
                    .email("existing@gmail.com")
                    .fullName("Old Name")
                    .passwordHash("encoded-pwd")
                    .role(Role.STUDENT)
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .provider(AuthProvider.LOCAL)
                    .build();
            when(userRepository.findByEmail("existing@gmail.com")).thenReturn(Optional.of(existingUser));
            when(jwtService.generateToken(any())).thenReturn("token-abc");

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(userRepository).save(existingUser);
            verify(response).sendRedirect(contains("token=token-abc"));
        }
    }
}
