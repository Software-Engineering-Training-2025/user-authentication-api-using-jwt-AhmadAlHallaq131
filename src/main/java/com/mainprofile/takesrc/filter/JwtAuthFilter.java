package com.mainprofile.takesrc.filter;

import com.mainprofile.takesrc.model.MyUser;
import com.mainprofile.takesrc.repository.MyUserRepository;
import com.mainprofile.takesrc.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final MyUserRepository myUserRepository;

    @Autowired
    public JwtAuthFilter(JwtService jwt, MyUserRepository myUserRepository) {
        this.jwt = jwt;
        this.myUserRepository = myUserRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {

        String path = req.getRequestURI();
        if (path.startsWith("/auth/logout")) {
            chain.doFilter(req, res);
            return;
        }

        String header = req.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = jwt.parseClaims(token);
            Integer userId = Integer.valueOf(claims.getSubject());

            MyUser user = myUserRepository.findById(userId).orElse(null);
            if (user != null) {
                var auth = new UsernamePasswordAuthenticationToken(
                        user.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException e) {
            logger.error("JWT validation error: {" + e.getMessage()+" }" );
        }

        chain.doFilter(req, res);
    }
}
