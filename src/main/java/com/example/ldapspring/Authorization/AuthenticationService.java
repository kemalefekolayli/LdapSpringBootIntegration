package com.example.ldapspring.Authorization;

import com.example.ldapspring.entity.LdapUser;
import com.example.ldapspring.entity.LdapUserRepository;
import com.example.ldapspring.service.ReadService;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
class AuthenticationService {

    private final LdapTemplate ldapTemplate;
    private final LdapUserRepository ldapUserRepository;
    private final ReadService readService;


    public boolean validateCredentials(String username, String password) {
        try {
            // LDAP bind operation
            // User DN oluştur: uid=username,ou=people,dc=example,dc=org
            // LdapTemplate ile bind test yap
            // Başarılı bind = valid credentials
            return ldapTemplate.authenticate(userDn, password);
        } catch (Exception e) {
            return false;
        }
    }

    public String generateJwtToken(LdapUser user, List<String> roles) {
        return Jwts.builder()
                .setSubject(user.getUid())
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
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
        if (!ldapUser.isPresent()) {
            throw new UserNotFoundException("User not found");
        }

        // 3. Get user roles/permissions (future: from PostgreSQL)
        List<String> roles = getUserRoles(username);

        // 4. Generate tokens
        String accessToken = generateJwtToken(ldapUser.get(), roles);
        String refreshToken = generateRefreshToken(username);

        return new AuthenticationResponse(accessToken, refreshToken, ldapUser.get());
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
        List<String> roles = getUserRoles(username);

        String newAccessToken = generateJwtToken(user.get(), roles);
        String newRefreshToken = generateRefreshToken(username);

        return new AuthenticationResponse(newAccessToken, newRefreshToken, user.get());
    }
}