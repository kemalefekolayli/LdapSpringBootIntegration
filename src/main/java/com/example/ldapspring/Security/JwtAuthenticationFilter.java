package com.example.ldapspring.Security;

import com.example.ldapspring.Authorization.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authService;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 1) Token geçerli mi?
        if (!authService.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) Kullanıcı adı + roller
        String username = authService.extractUsernameFromToken(token);

        // AuthenticationService'den roller
        List<String> roles = authService.getUserRoles(username);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .filter(Objects::nonNull)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r) // Spring standardı
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

}
