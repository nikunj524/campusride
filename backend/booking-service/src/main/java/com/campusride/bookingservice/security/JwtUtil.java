package com.campusride.bookingservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey signingKey;

    public JwtUtil(@Value("${app.jwt.secret}") String base64Secret) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    public AuthenticatedUser extractUser(String token) {
        Claims claims = parseClaims(token);
        Number userId = claims.get("userId", Number.class);
        String role = claims.get("activeRole", String.class);
        if (userId == null || role == null) {
            throw new IllegalArgumentException("JWT is missing required user claims");
        }
        return new AuthenticatedUser(userId.longValue(), claims.getSubject(), role);
    }

    public boolean isTokenValid(String token, AuthenticatedUser user) {
        return user.email().equals(parseClaims(token).getSubject()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
