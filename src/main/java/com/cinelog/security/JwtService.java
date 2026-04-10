package com.cinelog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = buildKey(jwtProperties.secret());
    }

    public String generateToken(AppUserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .claim("username", userDetails.getEmail())
                .claim("displayUsername", userDetails.getUsername())
                .claim("role", userDetails.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.expirationMs())))
                .signWith(signingKey)
                .compact();
    }

    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        extractClaims(token);
        return true;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey buildKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (IllegalArgumentException ignored) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
