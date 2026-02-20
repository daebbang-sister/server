package com.daebbang.daebbangcore.infra.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    private final SecretKey key;
    private final Long accessTokenExpirationTime;
    private final Long refreshTokenExpirationTime;

    public JwtUtils(
        @Value("${spring.jwt.secret}")String secret,
        @Value("${spring.jwt.access}")Long accessExpirationTime,
        @Value("${spring.jwt.refresh}")Long refreshExpirationTime) {
        this.key = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
            SIG.HS256.key().build().getAlgorithm()
        );
        this.accessTokenExpirationTime = accessExpirationTime;
        this.refreshTokenExpirationTime = refreshExpirationTime;
    }

    public String getUserName(String token) {
        return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("name", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);
    }

    public boolean isExpired(String token) {
        return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
    }

    public String createAccessToken(String name, String role) {
        return Jwts.builder()
                    .claim("name", name)
                    .claim("role", role)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))
                    .signWith(key)
                    .compact();
    }

    public String createRefreshToken(String name, String role) {
        return Jwts.builder()
            .claim("name", name)
            .claim("role", role)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
            .signWith(key)
            .compact();
    }
}
