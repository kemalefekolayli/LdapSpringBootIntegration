
// AuthenticationResponse.java  
package com.example.ldapspring.Authorization;

import com.example.ldapspring.Entity.LdapUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private LdapUser user;
    private List<String> roles;
    private long expiresIn;

    public AuthenticationResponse(String accessToken, String refreshToken, LdapUser user, List<String> roles, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.user = user;
        this.roles = roles;
        this.expiresIn = expiresIn;
    }
}
