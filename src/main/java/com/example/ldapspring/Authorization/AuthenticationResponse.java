package com.example.ldapspring.Authorization;

import com.example.ldapspring.entity.LdapUser;

import java.util.List;

public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private LdapUser user;
    private List<String> roles;
    private long expiresIn;
}