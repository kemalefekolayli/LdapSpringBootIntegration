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
        ctx.setBase("cn=admin,dc=example,dc=org");
        ctx.setUserDn("LdapConfig");
        ctx.setPassword("adminpassword");
        return ctx;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }
}
