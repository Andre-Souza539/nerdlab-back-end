package com.nerdlab.application.services;

import com.nerdlab.domain.User;
import com.nerdlab.domain.ports.UserRepositoryPort;
import com.nerdlab.infrastructure.adapters.in.web.dto.RegisterRequest;
import com.nerdlab.infrastructure.adapters.in.web.dto.UserSummaryResponse;
import com.nerdlab.infrastructure.adapters.in.web.exception.ResourceNotFoundException;
import com.nerdlab.infrastructure.adapters.in.web.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserSummaryResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepositoryPort.existsByEmail(email)) {
            throw new UserAlreadyExistsException("O e-mail '" + email + "' já está cadastrado.");
        }

        String username = request.username() != null && !request.username().isBlank() 
                ? request.username().trim() 
                : email.split("@")[0];

        if (userRepositoryPort.existsByUsername(username)) {
            username = username + "_" + System.currentTimeMillis() % 1000;
        }

        User newUser = User.builder()
                .name(request.name().trim())
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role("ROLE_USER")
                .passwordChangeRequired(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User saved = userRepositoryPort.save(newUser);
        return toSummary(saved);
    }

    public Optional<User> findByUsername(String username) {
        return userRepositoryPort.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepositoryPort.findByEmail(email);
    }

    public Optional<User> findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        Optional<User> byUsername = userRepositoryPort.findByUsername(identifier);
        if (byUsername.isPresent()) {
            return byUsername;
        }
        return userRepositoryPort.findByEmail(identifier.trim().toLowerCase());
    }

    public UserSummaryResponse getProfile(String identifier) {
        User user = findByIdentifier(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + identifier));
        return toSummary(user);
    }

    @Transactional
    public void updatePassword(String identifier, String newPassword) {
        User user = findByIdentifier(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + identifier));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepositoryPort.save(user);
    }

    @Transactional
    public void createAdminIfNotExist(String email, String username, String initialPassword) {
        if (userRepositoryPort.findByUsername(username).isEmpty() && userRepositoryPort.findByEmail(email).isEmpty()) {
            User admin = User.builder()
                    .name("Administrador")
                    .email(email)
                    .username(username)
                    .password(passwordEncoder.encode(initialPassword))
                    .role("ROLE_ADMIN")
                    .passwordChangeRequired(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepositoryPort.save(admin);
        }
    }

    public UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isPasswordChangeRequired(),
                user.getCreatedAt()
        );
    }
}
