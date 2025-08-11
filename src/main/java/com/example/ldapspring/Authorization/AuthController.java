package com.example.ldapspring.Authorization;

import com.example.ldapspring.Entity.LdapUser;
import com.example.ldapspring.Service.ReadService;
import com.example.ldapspring.MonitoringService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ReadService readService;
    private final MonitoringService monitoringService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest request) {
        try {
            System.out.println("Login attempt for user: " + request.getUsername());

            AuthenticationResponse response = authenticationService.authenticateUser(
                    request.getUsername(),
                    request.getPassword()
            );

            System.out.println("Login successful for user: " + request.getUsername());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            monitoringService.logLoginFailure(request.getUsername());
            Map<String, String> error = new HashMap<>();
            error.put("error", "INVALID_CREDENTIALS");
            error.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (AuthenticationService.UserNotFoundException e) {
            monitoringService.logEvent("USER_NOT_FOUND", "User not found: " + request.getUsername());
            Map<String, String> error = new HashMap<>();
            error.put("error", "USER_NOT_FOUND");
            error.put("message", "User not found in LDAP directory");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            monitoringService.logEvent("AUTHENTICATION_ERROR", e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "AUTHENTICATION_ERROR");
            error.put("message", "Authentication failed");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            System.out.println("Token refresh attempt");

            AuthenticationResponse response = authenticationService.refreshToken(request.getRefreshToken());

            System.out.println("Token refresh successful");
            return ResponseEntity.ok(response);

        } catch (AuthenticationService.InvalidTokenException e) {
            monitoringService.logEvent("INVALID_REFRESH_TOKEN", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "INVALID_REFRESH_TOKEN");
            error.put("message", "Invalid or expired refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (AuthenticationService.UserNotFoundException e) {
            monitoringService.logEvent("USER_NOT_FOUND", "User not found during token refresh");
            Map<String, String> error = new HashMap<>();
            error.put("error", "USER_NOT_FOUND");
            error.put("message", "User no longer exists");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (Exception e) {
            monitoringService.logEvent("TOKEN_REFRESH_ERROR", e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "TOKEN_REFRESH_ERROR");
            error.put("message", "Token refresh failed");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // Token'ı extract et
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            if (token == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "MISSING_TOKEN");
                error.put("message", "Authorization token required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Token validation
            if (!authenticationService.validateToken(token)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "INVALID_TOKEN");
                error.put("message", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // TODO: Token'ı blacklist'e ekle (Redis, Database, vs.)
            // Şimdilik sadece success response dönüyoruz

            System.out.println("User logged out successfully");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            monitoringService.logEvent("LOGOUT_ERROR", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "LOGOUT_ERROR");
            error.put("message", "Logout failed");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // Token'ı extract et
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            if (token == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "MISSING_TOKEN");
                error.put("message", "Authorization token required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Token validation
            if (!authenticationService.validateToken(token)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "INVALID_TOKEN");
                error.put("message", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Username'i token'dan çıkar
            String username = authenticationService.extractUsernameFromToken(token);

            // User bilgilerini getir
            Optional<LdapUser> user = readService.getUserByUid(username);
            if (!user.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "USER_NOT_FOUND");
                error.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            return ResponseEntity.ok(user.get());

        } catch (Exception e) {
            monitoringService.logEvent("GET_USER_ERROR", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "GET_USER_ERROR");
            error.put("message", "Failed to get current user");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            if (token == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("valid", false);
                error.put("error", "Missing token");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            boolean isValid = authenticationService.validateToken(token);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);

            if (isValid) {
                String username = authenticationService.extractUsernameFromToken(token);
                response.put("username", username);
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}