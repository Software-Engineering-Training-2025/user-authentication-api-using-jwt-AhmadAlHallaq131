package com.mainprofile.takesrc.controller;

import com.mainprofile.takesrc.DTOs.LoginRequest;
import com.mainprofile.takesrc.DTOs.RefreshRequest;
import com.mainprofile.takesrc.DTOs.SignUpRequest;
import com.mainprofile.takesrc.DTOs.TokenResponse;
import com.mainprofile.takesrc.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest signUpRequest) {
        try {
            authService.signup(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email or username already exists");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login attempt for: " + loginRequest.getEmail());
            Map<String,String> tokens = authService.login(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );
            System.out.println("Login successful!");

            return ResponseEntity.ok(
                    new TokenResponse(
                            tokens.get("accessToken"),
                            tokens.get("refreshToken")
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest refreshRequest) {
        try {
            String newAccess = authService.refresh(refreshRequest.getRefreshToken());
            return ResponseEntity.ok(Map.of("accessToken", newAccess));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest refreshRequest) {
        try {
            System.out.println("Logout attempt");

            if (refreshRequest.getRefreshToken() == null ||
                    refreshRequest.getRefreshToken().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Refresh token is required"));
            }

            authService.logout(refreshRequest.getRefreshToken());
            System.out.println("Logout successful");

            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));

        } catch (Exception e) {
            System.out.println("Logout error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("message", "Logged out"));
        }
    }

}
