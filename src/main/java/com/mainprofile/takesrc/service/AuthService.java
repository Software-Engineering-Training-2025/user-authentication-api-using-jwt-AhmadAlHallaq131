package com.mainprofile.takesrc.service;

import com.mainprofile.takesrc.DTOs.LoginRequest;
import com.mainprofile.takesrc.model.MyUser;
import com.mainprofile.takesrc.model.RefreshToken;
import com.mainprofile.takesrc.repository.MyUserRepository;
import com.mainprofile.takesrc.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthService {
    private final MyUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(MyUserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void signup(String username, String email, String rawPassword) {
        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase();

        if (normalizedUsername.trim().isEmpty() || normalizedEmail.trim().isEmpty() || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Username, email, and password are required");
        }
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        MyUser u = new MyUser();
        u.setUsername(normalizedUsername);
        u.setEmail(normalizedEmail);
        u.setPasswordHash(passwordEncoder.encode(rawPassword)); // هون هاش
        u.setRole("USER");
        userRepository.save(u);
        System.out.println("User saved successfully!");
    }

    @Transactional
    public Map<String,String> login(String email, String rawPassword) {

        String normalizedEmail = email.trim().toLowerCase();

        MyUser user = userRepository.findByEmail(normalizedEmail
                        .toLowerCase())
                .orElseThrow(() -> {
                    System.out.println("User not found!");
                    return new BadCredentialsException("Invalid credentials for user");
                });

        System.out.println("User found: " + user.getEmail());
        System.out.println("Checking password...");

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            System.out.println("Password mismatch!");
            throw new BadCredentialsException("Invalid credentials");
        }

        System.out.println("Login successful, generating tokens...");

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash()))
            throw new BadCredentialsException("Invalid credentials");

        // generate both tokens
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        // save refresh token to DB
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refresh);
        refreshToken.setExpiryAt(Instant.now().plus(jwtService.refreshTokenTtl()));
        refreshTokenRepository.save(refreshToken);

        return Map.of(
                "accessToken", access,
                "refreshToken", refresh
        );
    }

    public String refresh(String refreshToken) {
        Claims c = jwtService.parseClaims(refreshToken);
        if (!"refresh".equals(c.get("type")))
            throw new BadCredentialsException("Invalid token type");

        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Unknown token"));

        if (rt.isRevoked() || rt.getExpiryAt().isBefore(Instant.now()))
            throw new BadCredentialsException("Token revoked or expired :(");

        return jwtService.generateAccessToken(rt.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        try {
            int updated = refreshTokenRepository.revokedByToken(refreshToken);
            System.out.println("Tokens revoked: " + updated);
        } catch (Exception e) {
            System.out.println("Error during token revocation: " + e.getMessage());
            throw e;
        }
    }
}
