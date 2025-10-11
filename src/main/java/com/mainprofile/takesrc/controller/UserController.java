package com.mainprofile.takesrc.controller;

import com.mainprofile.takesrc.model.MyUser;
import com.mainprofile.takesrc.repository.MyUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final MyUserRepository myUserRepository;

    public UserController(MyUserRepository myUserRepository) {
        this.myUserRepository = myUserRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String email = (String) authentication.getPrincipal();
        MyUser user = myUserRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }
}
