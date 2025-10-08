package com.example.S_PACE.utils;

import com.example.S_PACE.pojo.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;

    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(User user) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

            return Jwts.builder()
                    .subject(user.getUserId().toString())
                    .claim("email", user.getEmail())
                    .claim("role", user.getRole() != null ? user.getRole().getRoleName() : "USER")
                    .claim("userId", user.getUserId())
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(getSigningKey(), Jwts.SIG.HS512)
                    .compact();
        } catch (Exception ex) {
            logger.error("Error generating JWT token: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to generate JWT token", ex);
        }
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT token has expired: " + ex.getMessage());
            return false;
        } catch (UnsupportedJwtException ex) {
            logger.warn("Unsupported JWT token: " + ex.getMessage());
            return false;
        } catch (MalformedJwtException ex) {
            logger.warn("Malformed JWT token: " + ex.getMessage());
            return false;
        } catch (SecurityException ex) {
            logger.warn("Invalid JWT signature: " + ex.getMessage());
            return false;
        } catch (IllegalArgumentException ex) {
            logger.warn("JWT token compact of handler are invalid: " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            logger.error("JWT validation error: " + ex.getMessage(), ex);
            return false;
        }
    }

    public String getUserEmailFromJWT(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("email", String.class);
        } catch (Exception ex) {
            logger.error("Error extracting email from JWT: " + ex.getMessage(), ex);
            return null;
        }
    }

    public String getUserIdFromJWT(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception ex) {
            logger.error("Error extracting user ID from JWT: " + ex.getMessage(), ex);
            return null;
        }
    }

    public String getRoleFromJWT(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("role", String.class);
        } catch (Exception ex) {
            logger.error("Error extracting role from JWT: " + ex.getMessage(), ex);
            return null;
        }
    }

    public Date getExpirationDateFromJWT(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration();
        } catch (Exception ex) {
            logger.error("Error extracting expiration date from JWT: " + ex.getMessage(), ex);
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromJWT(token);
        return expiration != null && expiration.before(new Date());
    }
}
