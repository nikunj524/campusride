package com.campusride.rideservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "Y2FtcHVzcmlkZS10ZXN0LXNlY3JldC1rZXktMzItYnl0ZXMhIQ==";

    @Test
    void extractsSignedUserClaims() {
        String token = Jwts.builder()
                .subject("driver@campusride.test")
                .claim("userId", 42L)
                .claim("activeRole", "DRIVER")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        JwtUtil jwtUtil = new JwtUtil(SECRET);
        AuthenticatedUser user = jwtUtil.extractUser(token);

        assertEquals(42L, user.userId());
        assertEquals("driver@campusride.test", user.email());
        assertEquals("DRIVER", user.role());
        assertTrue(jwtUtil.isTokenValid(token, user));
    }
}
