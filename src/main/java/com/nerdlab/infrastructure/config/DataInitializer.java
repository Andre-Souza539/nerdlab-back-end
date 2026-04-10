package com.nerdlab.infrastructure.config;

import com.nerdlab.application.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Value("${nerdlab.auth.admin.email}")
    private String adminEmail;

    @Value("${nerdlab.auth.admin.username}")
    private String adminUsername;

    @Value("${nerdlab.auth.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        userService.createAdminIfNotExist(adminEmail, adminUsername, adminPassword);
    }
}
