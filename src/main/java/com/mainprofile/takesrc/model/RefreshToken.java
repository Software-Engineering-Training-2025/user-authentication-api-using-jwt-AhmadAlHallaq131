package com.mainprofile.takesrc.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_token",
        indexes = {
                @Index(name = "refresh_user", columnList = "user_id"),
                @Index(name = "refresh_expires", columnList = "expires_at")
        })
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private MyUser user;

    @Column(nullable = false, unique = true, length = 256)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean revoked = false;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public RefreshToken() {
    }

    public RefreshToken(Long id, MyUser user, String token, Instant expiresAt, boolean revoked, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiryAt() {
        return expiresAt;
    }

    public void setExpiryAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
