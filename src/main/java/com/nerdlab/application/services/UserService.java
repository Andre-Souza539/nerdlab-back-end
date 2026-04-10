package com.nerdlab.application.services;

import com.nerdlab.domain.User;
import com.nerdlab.domain.ports.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepositoryPort.findByUsername(username);
    }

    public void updatePassword(String username, String newPassword) {
        User user = userRepositoryPort.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(false);
        userRepositoryPort.save(user);
    }

    public void createAdminIfNotExist(String email, String username, String initialPassword) {
        if (userRepositoryPort.findByUsername(username).isEmpty()) {
            User admin = User.builder()
                    .email(email)
                    .username(username)
                    .password(passwordEncoder.encode(initialPassword))
                    .passwordChangeRequired(true)
                    .build();
            userRepositoryPort.save(admin);
        }
    }
}
