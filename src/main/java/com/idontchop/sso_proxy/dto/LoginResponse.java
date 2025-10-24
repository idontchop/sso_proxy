package com.idontchop.sso_proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String sessionId;
    private String username;
    private String email;
    private String role;
    private boolean success = true;

    public LoginResponse(String sessionId, String username, String email, String role) {
        this.sessionId = sessionId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.success = true;
    }
}
