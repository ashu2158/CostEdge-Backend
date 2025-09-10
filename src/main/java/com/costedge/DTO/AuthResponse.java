package com.costedge.model.DTO;

public class AuthResponse {
    private String token;
    private String role;
    private String username;

    // Constructor
    public AuthResponse(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
    }

    // Getters
    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getUsername() { return username; }

    // (Optional) Setters if you need them
    public void setToken(String token) { this.token = token; }
    public void setRole(String role) { this.role = role; }
    public void setUsername(String username) { this.username = username; }
}
