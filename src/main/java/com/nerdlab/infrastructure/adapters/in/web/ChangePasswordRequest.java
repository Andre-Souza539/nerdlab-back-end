package com.nerdlab.infrastructure.adapters.in.web;

public class ChangePasswordRequest {
    private String newPassword;

    public ChangePasswordRequest() {}

    public ChangePasswordRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
