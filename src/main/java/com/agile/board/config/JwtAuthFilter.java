package com.agile.board.config;

import com.agile.board.domain.Role;
import com.agile.board.repo.UserRepository;
import com.agile.board.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                Integer tokenVer = claims.get("ver", Integer.class);
                String roleStr = claims.get("role", String.class);
                List<String> perms = claims.get("perms", List.class);

                if (username != null && tokenVer != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Minimal DB hit ONLY to validate tokenVersion (invalidate old tokens on role change)
                    var current = users.findByUsername(username).orElse(null);
                    if (current != null && current.getTokenVersion() == tokenVer) {
                        // Build authorities from token claims (self-contained role/permissions)
                        List<SimpleGrantedAuthority> auths = new ArrayList<>();
                        Role role = Role.from(roleStr);
                        auths.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
                        if (perms != null) perms.forEach(p -> auths.add(new SimpleGrantedAuthority(p)));

                        UserDetails ud = User.withUsername(username).password("").authorities(auths).build();
                        var authTok = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
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
