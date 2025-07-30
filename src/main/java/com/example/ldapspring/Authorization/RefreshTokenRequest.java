
// RefreshTokenRequest.java
package com.example.ldapspring.Authorization;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    @NotEmpty(message = "Refresh token cannot be empty")
    private String refreshToken;
}