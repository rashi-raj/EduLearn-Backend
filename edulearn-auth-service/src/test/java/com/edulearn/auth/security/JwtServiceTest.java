package com.edulearn.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET_KEY =
            "bXktc3VwZXItc2VjdXJlLWtleS1mb3ItZWR1bGVhcm4tamF3dC1hdXRoLXNlcnZpY2UtMjAyNg==";
    private static final long EXPIRATION_MS = 86400000L; // 24 hours

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION_MS);

        userDetails = new User(
                "test@example.com",
                "encodedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("Generates a non-null token from UserDetails")
        void generateToken_returnsNonNull() {
            String token = jwtService.generateToken(userDetails);
            assertNotNull(token);
            assertFalse(token.isBlank());
        }

        @Test
        @DisplayName("Token generated with extra claims is non-null")
        void generateToken_withExtraClaims_returnsNonNull() {
            java.util.Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("role", "ROLE_STUDENT");

            String token = jwtService.generateToken(claims, userDetails);
            assertNotNull(token);
        }
    }

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsername {

        @Test
        @DisplayName("Extracts correct email as username from token")
        void extractUsername_returnsEmail() {
            String token = jwtService.generateToken(userDetails);
            String username = jwtService.extractUsername(token);
            assertEquals("test@example.com", username);
        }
    }

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValid {

        @Test
        @DisplayName("Returns true for a freshly generated token and matching user")
        void isTokenValid_validToken_returnsTrue() {
            String token = jwtService.generateToken(userDetails);
            assertTrue(jwtService.isTokenValid(token, userDetails));
        }

        @Test
        @DisplayName("Returns false when username does not match")
        void isTokenValid_wrongUser_returnsFalse() {
            String token = jwtService.generateToken(userDetails);

            UserDetails otherUser = new User(
                    "other@example.com",
                    "password",
                    List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
            );

            assertFalse(jwtService.isTokenValid(token, otherUser));
        }

        @Test
        @DisplayName("Throws ExpiredJwtException for an expired token")
        void isTokenValid_expiredToken_throwsException() throws Exception {
            JwtService shortLivedService = new JwtService();
            ReflectionTestUtils.setField(shortLivedService, "secretKey", SECRET_KEY);
            ReflectionTestUtils.setField(shortLivedService, "jwtExpiration", 1L); // 1 ms

            String token = shortLivedService.generateToken(userDetails);
            Thread.sleep(10); // Ensure token expires

            // JJWT throws ExpiredJwtException for expired tokens during parsing,
            // rather than returning false from isTokenValid
            assertThrows(io.jsonwebtoken.ExpiredJwtException.class,
                    () -> shortLivedService.isTokenValid(token, userDetails));
        }
    }

    @Nested
    @DisplayName("extractClaim()")
    class ExtractClaim {

        @Test
        @DisplayName("Extracts subject claim correctly")
        void extractClaim_subject() {
            String token = jwtService.generateToken(userDetails);
            String subject = jwtService.extractClaim(token, io.jsonwebtoken.Claims::getSubject);
            assertEquals("test@example.com", subject);
        }

        @Test
        @DisplayName("Extracts expiration claim that is in the future")
        void extractClaim_expiration_isFuture() {
            String token = jwtService.generateToken(userDetails);
            java.util.Date expiration = jwtService.extractClaim(token, io.jsonwebtoken.Claims::getExpiration);
            assertTrue(expiration.after(new java.util.Date()));
        }
    }
}
