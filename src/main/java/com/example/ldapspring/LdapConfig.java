package com.example.ldapspring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class LdapConfig {

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource ctx = new LdapContextSource();
        ctx.setUrl("ldap://localhost:389");
        ctx.setBase("dc=example,dc=org");
        ctx.setUserDn("cn=admin,dc=example,dc=org");
        ctx.setPassword("admin");
        return ctx;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }
}
