package com.nerdlab.infrastructure.adapters.in.web;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String newPassword;
}
