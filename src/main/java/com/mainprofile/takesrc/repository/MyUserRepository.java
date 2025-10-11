package com.mainprofile.takesrc.repository;

import com.mainprofile.takesrc.model.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MyUserRepository extends JpaRepository <MyUser,Integer> {
    Optional<MyUser> findByEmail(String email);
}
