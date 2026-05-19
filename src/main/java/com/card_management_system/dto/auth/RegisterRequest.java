package com.card_management_system.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Username shouldn't be empty")
    @Size(min = 4, max = 20, message = "Size of username should be between 4 and 20")
    private String username;

    @NotBlank(message = "Password shouldn't be empty")
    @Size(min = 6, message = "Password size should be at least 6")
    private String password;

    private String role;
    private String adminSecretKey;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAdminSecretKey() {
        return adminSecretKey;
    }

    public void setAdminSecretKey(String adminSecretKey) {
        this.adminSecretKey = adminSecretKey;
    }
}
