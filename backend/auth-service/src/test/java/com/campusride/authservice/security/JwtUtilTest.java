package com.campusride.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtUtilTest {

    private static final String SECRET = "Y2FtcHVzcmlkZS10ZXN0LXNlY3JldC1rZXktMzItYnl0ZXMhIQ==";

    @Test
    void generatesTokenWithUserIdEmailAndActiveRole() {
        String token = new JwtUtil(SECRET, 86_400_000)
                .generateToken(42L, "driver@campusride.test", "DRIVER");

        var claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("driver@campusride.test", claims.getSubject());
        assertEquals(42L, claims.get("userId", Number.class).longValue());
        assertEquals("DRIVER", claims.get("activeRole", String.class));
    }
}
