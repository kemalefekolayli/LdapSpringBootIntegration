package com.example.ldapspring.Authorization;

import com.example.ldapspring.entity.LdapUser;
import com.example.ldapspring.service.ReadService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.ldap.core.LdapTemplate;
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
class AuthenticationService {

    private final LdapTemplate ldapTemplate;
    private final ReadService readService;
    private final JwtConfig jwtConfig;

    public boolean validateCredentials(String username, String password) {
        try {
            // LDAP bind operation using LdapQuery
            // Search for user in people OU and authenticate
            return ldapTemplate.authenticate("ou=people", "(uid=" + username + ")", password);
        } catch (Exception e) {
            System.err.println("LDAP authentication failed for user: " + username + " - " + e.getMessage());
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
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.refreshExpirationMs))
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

        return new AuthenticationResponse(accessToken, refreshToken, ldapUser.get(), roles, jwtConfig.jwtExpirationMs);
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

        return new AuthenticationResponse(newAccessToken, newRefreshToken, user.get(), roles, jwtConfig.jwtExpirationMs);
    }

    // JWT Utility Methods
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.jwtSecret.getBytes(StandardCharsets.UTF_8);
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
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("Refresh token validation failed: " + e.getMessage());
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

    // Role Management - Şimdilik basit implementation, gelecekte PostgreSQL'den gelecek
    private List<String> getUserRoles(String username) {
        // TODO: Bu method gelecekte PostgreSQL authorization servisinden gelecek
        // Şimdilik default roller dönüyoruz

        // LDAP grup bilgilerini kontrol edebiliriz (opsiyonel)
        try {
            // Bootstrap.ldif'te group membership var mı kontrol et
            if (isUserInGroup(username, "developers")) {
                return Arrays.asList("USER", "DEVELOPER");
            } else if (isUserInGroup(username, "scientists")) {
                return Arrays.asList("USER", "SCIENTIST");
            } else {
                return Arrays.asList("USER");
            }
        } catch (Exception e) {
            System.err.println("Error getting user roles: " + e.getMessage());
            return Arrays.asList("USER"); // Default role
        }
    }

    private boolean isUserInGroup(String username, String groupName) {
        try {
            // LDAP'te grup üyeliğini kontrol et
            String userDn = "uid=" + username + ",ou=people,dc=example,dc=org";
            String groupDn = "cn=" + groupName + ",ou=groups,dc=example,dc=org";

            // Grup var mı kontrol et
            Object group = ldapTemplate.lookup(groupDn);
            if (group != null) {
                // Üyelik kontrolü - bu kısım grup yapısına göre değişebilir
                // Şimdilik basit kontrol yapıyoruz
                return true; // TODO: Gerçek üyelik kontrolü implement et
            }
            return false;
        } catch (Exception e) {
            System.err.println("Group membership check failed: " + e.getMessage());
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