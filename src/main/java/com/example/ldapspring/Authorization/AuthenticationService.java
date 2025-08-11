package com.example.ldapspring.Authorization;

import com.example.ldapspring.Entity.LdapUser;
import com.example.ldapspring.Service.ReadService;
import com.example.ldapspring.Service.RoleService;
import com.example.ldapspring.Entity.Auth.Role;
import com.example.ldapspring.MonitoringService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final LdapTemplate ldapTemplate;
    private final ReadService readService;
    private final JwtConfig jwtConfig;
    private final RoleService roleService;
    private final MonitoringService monitoringService;

    public boolean validateCredentials(String username, String password) {
        try {
            boolean result = ldapTemplate.authenticate("ou=people", "(uid=" + username + ")", password);
            if (!result) {
                monitoringService.logLoginFailure(username);
            }
            return result;
        } catch (Exception e) {
            monitoringService.logLdapError("LDAP authentication failed for user: " + username, e);
            return false;
        }
    }

    public String generateJwtToken(LdapUser user, List<String> roles) {
        return Jwts.builder()
                .subject(user.getUid())
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getRefresh().getExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    public AuthenticationResponse authenticateUser(String username, String password) {
        // 1. LDAP credential validation
        if (!validateCredentials(username, password)) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // 2. Get user from LDAP
        Optional<LdapUser> ldapUser = readService.getUserByUid(username);
        if (ldapUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        // 3. Get user roles/permissions (future: from PostgreSQL)
        List<String> roles = getUserRoles(username);

        // 4. Generate tokens
        String accessToken = generateJwtToken(ldapUser.get(), roles);
        String refreshToken = generateRefreshToken(username);

        return new AuthenticationResponse(accessToken, refreshToken, ldapUser.get(), roles, jwtConfig.getExpiration());
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        // 1. Validate refresh token
        if (!validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        // 2. Extract username from refresh token
        String username = extractUsernameFromToken(refreshToken);

        // 3. Get user and generate new tokens
        Optional<LdapUser> user = readService.getUserByUid(username);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        List<String> roles = getUserRoles(username);

        String newAccessToken = generateJwtToken(user.get(), roles);
        String newRefreshToken = generateRefreshToken(username);

        return new AuthenticationResponse(newAccessToken, newRefreshToken, user.get(), roles, jwtConfig.getExpiration());
    }

    // JWT Utility Methods
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            monitoringService.logEvent("TOKEN_VALIDATION_FAILED", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            monitoringService.logEvent("REFRESH_TOKEN_VALIDATION_FAILED", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Role Management - PostgreSQL tabanlı yetkilendirme
    public List<String> getUserRoles(String username) {
        try {
            List<Role> roles = roleService.getUserRoles(username);
            if (roles.isEmpty()) {
                return Arrays.asList("USER");
            }
            return roles.stream().map(Role::getName).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            monitoringService.logLdapError("Error getting user roles for user: " + username, e);
            return Arrays.asList("USER"); // Default role
        }
    }

    private boolean isUserInGroup(String username, String groupName) {
        try {
            String userDn = "uid=" + username + ",ou=people,dc=example,dc=org";
            String base = "ou=groups,dc=example,dc=org";
            String filter = "(&(cn=" + groupName + ")(|(member=" + userDn + ")" +
                    "(uniqueMember=" + userDn + ")" +
                    "(memberUid=" + username + ")))";
            return !ldapTemplate.search(base, filter, (AttributesMapper<Object>) attrs -> null).isEmpty();
        } catch (Exception e) {
            monitoringService.logLdapError("Group membership check failed for user: " + username, e);
            return false;
        }
    }

    // Custom Exception Classes
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}