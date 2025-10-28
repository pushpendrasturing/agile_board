package com.example.agileboard.config;

import com.example.agileboard.repo.UserRepository;
import com.example.agileboard.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final UserRepository users;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(authService.key())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (users.findByUsername(username).isPresent()) {
                        UserDetails ud = User.withUsername(username)
                                .password("")
                                .authorities(Collections.emptyList())
                                .build();

                        UsernamePasswordAuthenticationToken authTok =
                                new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                        authTok.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authTok);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        chain.doFilter(request, response);
    }
}
