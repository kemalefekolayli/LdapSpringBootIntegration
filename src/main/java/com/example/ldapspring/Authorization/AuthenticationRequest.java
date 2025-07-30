package com.example.ldapspring.Authorization;

import jakarta.validation.constraints.NotEmpty;

public class AuthenticationRequest {
    @NotEmpty
    private String username;

    @NotEmpty
    private String password;
}