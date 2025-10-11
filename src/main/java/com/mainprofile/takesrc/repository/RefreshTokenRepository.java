package com.mainprofile.takesrc.repository;

import com.mainprofile.takesrc.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Integer> {
    Optional<RefreshToken> findByToken(String refreshToken);
    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.token = :token")
    int revokedByToken (@Param("token") String token);
}
