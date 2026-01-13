package com.parwaan.portfolio.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    // Example: set in application.properties or env:
    // app.jwt.secret=base64:Zm9vYmFy... (or a long plain secret)
    @Value("${app.jwt.secret:qw89e0asjdhkajshd9832y4hkasjhd8237y498123}")
    private String jwtSecret; // can be plain or base64

    @Value("${app.jwt.expiration-ms:3600000}")
    private long expirationMs;

    // Returns a Key suitable for signing/verifying HS256
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // If you store a base64 secret, decode it instead:
        // byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes); // recommended helper
    }

    public String generateToken(UUID userId, String email, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .addClaims(Map.of("role", role))
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // use Key + alg
                .compact();
    }

    public Jws<Claims> validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }

    public String getUserIdFromToken(String token) {
        return validateToken(token).getBody().getSubject();
    }
}
