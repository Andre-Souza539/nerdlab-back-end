package com.nerdlab.infrastructure.adapters.in.web;

public class AuthResponse {
    private String token;
    private String username;
    private boolean passwordChangeRequired;

    public AuthResponse() {}

    public AuthResponse(String token, String username, boolean passwordChangeRequired) {
        this.token = token;
        this.username = username;
        this.passwordChangeRequired = passwordChangeRequired;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isPasswordChangeRequired() { return passwordChangeRequired; }
    public void setPasswordChangeRequired(boolean passwordChangeRequired) { this.passwordChangeRequired = passwordChangeRequired; }
}
