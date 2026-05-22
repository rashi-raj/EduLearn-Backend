
package com.edulearn.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	// Service used to extract data from JWT and validate JWT tokens
    private final JwtService jwtService;
    
    // Service used to load user details from the database by email*/\]
    private final CustomUserDetailsService userDetailsService;

    // this method runs automatically for every incoming HTTP request
    @Override
    protected void doFilterInternal(
    		// incoming request
            @NonNull HttpServletRequest request,
            // outgoing response
            @NonNull HttpServletResponse response,
            // allows request to continue to next filter/controller
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
    	
    	// gets the path of the request
        String path = request.getServletPath();

        if (path.startsWith("/api/v1/auth/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/")) {
        	// these paths are public and handled separately
        	
            filterChain.doFilter(request, response);
            //let the request continue without checking jwt
            
            return;
        }

        // Reads the authorization header from the HTTP request
        final String authHeader = request.getHeader("Authorization");
        
        // stores actual JWT token
        final String jwt;
        
        // stores email extracted from the JWT
        final String userEmail;

        // if authorization header is missing, or it does nt start with "Bearer",
        // then this filter cannot extract a JWT 
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        	
        	// Continue the request without setting authentication
        	// if the endpoint is protected , spring security will reject it later
            filterChain.doFilter(request, response);
            
            return;
        }

        // Removes "Bearer " from the beginning.
        // "Bearer " has 7 characters.
        // After this, only the raw JWT token remains.
        jwt = authHeader.substring(7);
        
        // Extracts the username/email from the JWT.
        // In your project, the JWT subject is the user's email.
        userEmail = jwtService.extractUsername(jwt);

        // This checks two things:
        // 1. The token actually contains an email.
        // 2. No user is already authenticated for this request.

        // SecurityContextHolder - temporary memory for the current request
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        	
        	// Loads user details from the database using the email.
            // This returns username/email, password hash, and authorities/roles.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Checks if the token is valid for this user.
            // Valid means:
            // - token username matches userDetails username
            // - token is not expired
            // - token signature is correct
            if (jwtService.isTokenValid(jwt, userDetails)) {
            	
            	// Creates an authentication object for Spring Security.
                //
                // First argument: userDetails
                // This tells Spring who the user is.
                //
                // Second argument: null
                // Credentials/password are null because we are not logging in with password here.
                // The JWT already proved identity.
                //
                // Third argument: authorities
                // These are the user's permissions/roles, like ROLE_ADMIN.
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Adds extra request details to the authentication object.
                // Example: remote IP address, session ID if present.
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // It tells Spring Security:
                // "This request belongs to an authenticated user."
                // After this, protected endpoints can be accessed.
                // Also, role checks like @PreAuthorize can work.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // continue to the next filter or controller
        filterChain.doFilter(request, response);
    }
}