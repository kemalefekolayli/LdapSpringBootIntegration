package com.example.ldapspring.Authorization;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT related settings. Values can be updated at
 * runtime through the AdminController.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /** Secret key used for signing JWT tokens. */
    private String secret = "mySecretKey";

    /** Access token validity in milliseconds. */
    private long expiration = 86400000L; // 24 hours

    private final Refresh refresh = new Refresh();

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public Refresh getRefresh() {
        return refresh;
    }

    public static class Refresh {
        /** Refresh token validity in milliseconds. */
        private long expiration = 604800000L; // 7 days

        public long getExpiration() {
            return expiration;
        }

        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }
    }
}
