package com.edulearn.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Gateway-level JWT validation service.
 *
 * Validates tokens issued by the Auth Service using the shared secret key.
 * Does NOT issue tokens — that is exclusively the Auth Service's responsibility.
 *
 * The secret key MUST match the one configured in edulearn-auth-service
 * (both read from the same JWT_SECRET_KEY environment variable).
 */
@Slf4j
@Service
public class GatewayJwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Extract the subject (user email) from a JWT token.
     *
     * @param token raw JWT string (without "Bearer " prefix)
     * @return the email address embedded as the token subject
     * @throws JwtException if the token is malformed, expired, or tampered with
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract a specific claim from the token payload.
     *
     * @param token  raw JWT string
     * @param claim  the claim key to look up (e.g. "role")
     * @return the claim value as a String, or null if not present
     */
    public String extractClaim(String token, String claim) {
        Object value = extractAllClaims(token).get(claim);
        return value != null ? value.toString() : null;
    }

    /**
     * Validate a token without loading UserDetails from DB.
     * The gateway only needs to check:
     *   1. Signature is valid (not tampered)
     *   2. Token is not expired
     *
     * Role/permission checks happen inside each downstream service.
     *
     * @param token raw JWT string
     * @return true if token is structurally valid and not expired
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            boolean notExpired = claims.getExpiration().after(new Date());
            if (!notExpired) {
                log.warn("[Gateway] JWT rejected: token has expired");
            }
            return notExpired;
        } catch (ExpiredJwtException e) {
            log.warn("[Gateway] JWT rejected: expired — {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("[Gateway] JWT rejected: invalid signature or malformed — {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[Gateway] JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
