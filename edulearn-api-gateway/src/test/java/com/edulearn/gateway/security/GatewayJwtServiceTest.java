package com.edulearn.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class GatewayJwtServiceTest {

    private GatewayJwtService jwtService;
    private String secretKey;

    @BeforeEach
    void setUp() {
        jwtService = new GatewayJwtService();
        // Generate a valid 256-bit key for testing
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        secretKey = Encoders.BASE64.encode(key.getEncoded());
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
    }

    @Test
    void extractEmail_shouldReturnSubject() {
        String email = "test@example.com";
        String token = Jwts.builder()
                .setSubject(email)
                .signWith(Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey)))
                .compact();

        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = Jwts.builder()
                .setSubject("user")
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey)))
                .compact();

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        String token = Jwts.builder()
                .setSubject("user")
                .setExpiration(new Date(System.currentTimeMillis() - 100000))
                .signWith(Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey)))
                .compact();

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
    }
}
