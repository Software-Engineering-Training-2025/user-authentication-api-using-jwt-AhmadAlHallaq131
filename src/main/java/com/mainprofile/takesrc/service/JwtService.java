package com.mainprofile.takesrc.service;

import com.mainprofile.takesrc.model.MyUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access.minutes}")
    private int accessMinutes;

    @Value("${app.jwt.refresh.days}")
    private int refreshMinutes;

    private Key key(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(MyUser user){
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(Duration.ofMinutes(accessMinutes))))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public  String generateRefreshToken(MyUser user){
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("type","refresh")  // mark it as refresh token عشان
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(Duration.ofDays(refreshMinutes))))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // my decoder & verify
    public Claims parseClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(key()).build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Duration refreshTokenTtl(){
        return Duration.ofDays(refreshMinutes);
    }
}
