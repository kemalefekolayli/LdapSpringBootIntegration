package com.example.ldapspring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple service that forwards important application events to a central
 * logging/monitoring endpoint. If no external endpoint is configured the
 * events are only written to the application logs.
 */
@Service
public class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);
    private final RestTemplate restTemplate;

    @Value("${monitoring.endpoint:}")
    private String monitoringEndpoint;

    public MonitoringService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public void logEvent(String type, String message) {
        log.info("[{}] {}", type, message);
        if (monitoringEndpoint != null && !monitoringEndpoint.isEmpty()) {
            try {
                Map<String, String> payload = new HashMap<>();
                payload.put("type", type);
                payload.put("message", message);
                restTemplate.postForEntity(monitoringEndpoint, payload, Void.class);
            } catch (Exception ex) {
                log.error("Failed to send monitoring event", ex);
            }
        }
    }

    public void logLoginFailure(String username) {
        logEvent("LOGIN_FAILURE", "Login failed for user: " + username);
    }

    public void logLdapError(String message, Exception ex) {
        logEvent("LDAP_ERROR", message + " - " + ex.getMessage());
    }
}
