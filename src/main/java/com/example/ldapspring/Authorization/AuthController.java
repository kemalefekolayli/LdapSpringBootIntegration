package com.example.ldapspring.Authorization;

import com.example.ldapspring.entity.LdapUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request);

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request);

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token);

    @GetMapping("/me")
    public ResponseEntity<LdapUser> getCurrentUser(Authentication auth);
}