package com.epam.rd.autocode.assessment.appliances.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:900000}") // 15m
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms:604800000}") // 7d
    private long refreshExpirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String email) {
        return build(email, accessExpirationMs, "access");
    }

    public String generateRefreshToken(String email) {
        return build(email, refreshExpirationMs, "refresh");
    }

    private String build(String subject, long ttl, String type) {
        return Jwts.builder()
                .subject(subject)
                .claim("type", type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttl))
                .signWith(key())
                .compact();
    }

    public String extractEmail(String token) {
        return parse(token).getPayload().getSubject();
    }

    public String extractType(String token) {
        return (String) parse(token).getPayload().get("type");
    }

    public boolean isValid(String token, UserDetails user) {
        try {
            String email = extractEmail(token);
            return email.equals(user.getUsername()) && !isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT] Invalid token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isExpired(String token) {
        return parse(token).getPayload().getExpiration().before(new Date());
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
    }

    public Cookie createJwtCookie(String token) {
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setHttpOnly(true); // XSS protection
        cookie.setSecure(false);  // HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) (accessExpirationMs / 1000));
        return cookie;
    }

    public Cookie deleteJwtCookie() {
        Cookie cookie = new Cookie("JWT_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
