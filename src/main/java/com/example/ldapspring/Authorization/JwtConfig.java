package com.example.ldapspring.Authorization;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class JwtConfig {
    @Value("${jwt.secret:mySecretKey}")
    String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 hours
    long jwtExpirationMs;

    @Value("${jwt.refresh.expiration:604800000}") // 7 days
    long refreshExpirationMs;
}