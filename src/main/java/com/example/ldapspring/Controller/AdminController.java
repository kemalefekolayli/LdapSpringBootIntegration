package com.example.ldapspring.Controller;

import com.example.ldapspring.Authorization.JwtConfig;
import com.example.ldapspring.MonitoringService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Administrative endpoints for managing runtime configuration. Access to this
 * controller is restricted to users with the ADMIN role via security
 * configuration and JWT based authentication.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JwtConfig jwtConfig;
    private final MonitoringService monitoringService;

    @GetMapping("/config/jwt")
    public Map<String, Object> getJwtConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("secret", jwtConfig.getSecret());
        result.put("expiration", jwtConfig.getExpiration());
        result.put("refreshExpiration", jwtConfig.getRefresh().getExpiration());
        return result;
    }

    @PostMapping("/config/jwt")
    public ResponseEntity<?> updateJwtConfig(@RequestBody JwtConfigRequest request) {
        if (request.getExpiration() != null) {
            jwtConfig.setExpiration(request.getExpiration());
        }
        if (request.getRefreshExpiration() != null) {
            jwtConfig.getRefresh().setExpiration(request.getRefreshExpiration());
        }
        monitoringService.logEvent("CONFIG_UPDATE", "JWT settings updated");
        return ResponseEntity.ok(getJwtConfig());
    }

    @Data
    public static class JwtConfigRequest {
        private Long expiration;
        private Long refreshExpiration;
    }
}
