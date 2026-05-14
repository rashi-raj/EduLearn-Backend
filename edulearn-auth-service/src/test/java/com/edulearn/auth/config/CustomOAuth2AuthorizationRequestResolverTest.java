package com.edulearn.auth.config;

import com.edulearn.auth.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2AuthorizationRequestResolver Unit Tests")
class CustomOAuth2AuthorizationRequestResolverTest {

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private CustomOAuth2AuthorizationRequestResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
        lenient().when(request.getSession(anyBoolean())).thenReturn(session);
    }

    private OAuth2AuthorizationRequest.Builder createBuilder() {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("id")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .state("state123")
                .additionalParameters(Map.of("old", "param"));
    }

    @Test
    @DisplayName("customize: returns null when authorizationRequest is null")
    void customize_nullRequest_returnsNull() {
        assertNull(resolver.customize(request, null));
    }

    @Test
    @DisplayName("customize: handles mode and role parameters correctly")
    void customize_handlesParameters() {
        OAuth2AuthorizationRequest authRequest = createBuilder().build();

        lenient().when(request.getParameter("mode")).thenReturn("signup");
        lenient().when(request.getParameter("role")).thenReturn("STUDENT");

        OAuth2AuthorizationRequest result = resolver.customize(request, authRequest);

        assertNotNull(result);
        assertEquals("select_account", result.getAdditionalParameters().get("prompt"));
        assertEquals("offline", result.getAdditionalParameters().get("access_type"));
        
        verify(session).setAttribute(CustomOAuth2AuthorizationRequestResolver.GOOGLE_AUTH_MODE, "signup");
        verify(session).setAttribute(CustomOAuth2AuthorizationRequestResolver.GOOGLE_SELECTED_ROLE, "STUDENT");
    }

    @Test
    @DisplayName("customize: defaults to login mode when mode is missing")
    void customize_missingMode_defaultsToLogin() {
        OAuth2AuthorizationRequest authRequest = createBuilder().build();

        lenient().when(request.getParameter("mode")).thenReturn(null);

        resolver.customize(request, authRequest);

        verify(session).setAttribute(CustomOAuth2AuthorizationRequestResolver.GOOGLE_AUTH_MODE, "login");
    }

    @Test
    @DisplayName("customize: throws exception for invalid role")
    void customize_invalidRole_throwsException() {
        OAuth2AuthorizationRequest authRequest = createBuilder().build();

        lenient().when(request.getParameter("role")).thenReturn("INVALID_ROLE");

        assertThrows(IllegalArgumentException.class, () -> resolver.customize(request, authRequest));
    }

    @Test
    @DisplayName("customize: throws exception for ADMIN role")
    void customize_adminRole_throwsException() {
        OAuth2AuthorizationRequest authRequest = createBuilder().build();

        lenient().when(request.getParameter("role")).thenReturn("ADMIN");

        assertThrows(IllegalArgumentException.class, () -> resolver.customize(request, authRequest));
    }
}
