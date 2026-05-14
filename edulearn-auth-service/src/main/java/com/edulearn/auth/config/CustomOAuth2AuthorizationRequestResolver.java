package com.edulearn.auth.config;

import com.edulearn.auth.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.Map;

public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    public static final String GOOGLE_SELECTED_ROLE = "GOOGLE_SELECTED_ROLE";
    public static final String GOOGLE_AUTH_MODE = "GOOGLE_AUTH_MODE";

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/oauth2/authorization"
                );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customize(request, authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customize(request, authorizationRequest);
    }

    OAuth2AuthorizationRequest customize(
            HttpServletRequest request,
            OAuth2AuthorizationRequest authorizationRequest
    ) {
        if (authorizationRequest == null) {
            return null;
        }

        String mode = request.getParameter("mode");

        HttpSession session = request.getSession(true);

        if (mode != null && !mode.isBlank()) {
            session.setAttribute(GOOGLE_AUTH_MODE, mode.toLowerCase());
        } else {
            session.setAttribute(GOOGLE_AUTH_MODE, "login");
        }

        String roleParam = request.getParameter("role");

        if (roleParam != null && !roleParam.isBlank()) {
            try {
                Role selectedRole = Role.valueOf(roleParam.toUpperCase());

                if (selectedRole == Role.ADMIN) {
                    throw new IllegalArgumentException("Admin role is not allowed for Google signup.");
                }

                session.setAttribute(GOOGLE_SELECTED_ROLE, selectedRole.name());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid role selected for Google signup.");
            }
        }

        // Force Google to show account selection page every time
        Map<String, Object> additionalParameters = new java.util.LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
        additionalParameters.put("prompt", "select_account");
        additionalParameters.put("access_type", "offline");

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
}