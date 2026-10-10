package com.nerdlab.application.services;

import com.nerdlab.domain.User;
import com.nerdlab.domain.ports.UserRepositoryPort;
import com.nerdlab.infrastructure.adapters.in.web.dto.RegisterRequest;
import com.nerdlab.infrastructure.adapters.in.web.dto.UserSummaryResponse;
import com.nerdlab.infrastructure.adapters.in.web.exception.ResourceNotFoundException;
import com.nerdlab.infrastructure.adapters.in.web.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest(
                "Lucas Silva",
                "lucas_dev",
                "lucas@nerdlab.com",
                "SenhaForte123!"
        );
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso e senha codificada")
    void register_WhenValidRequest_ShouldCreateUser() {
        when(userRepositoryPort.existsByEmail("lucas@nerdlab.com")).thenReturn(false);
        when(userRepositoryPort.existsByUsername("lucas_dev")).thenReturn(false);
        when(passwordEncoder.encode("SenhaForte123!")).thenReturn("hash_seguro_bcrypt");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            u.setCreatedAt(LocalDateTime.now());
            return u;
        });

        UserSummaryResponse response = userService.register(validRequest);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Lucas Silva", response.name());
        assertEquals("lucas@nerdlab.com", response.email());
        assertEquals("ROLE_USER", response.role());
        verify(passwordEncoder).encode("SenhaForte123!");
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar UserAlreadyExistsException quando e-mail já existe")
    void register_WhenEmailAlreadyExists_ShouldThrowException() {
        when(userRepositoryPort.existsByEmail("lucas@nerdlab.com")).thenReturn(true);

        UserAlreadyExistsException ex = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(validRequest)
        );

        assertTrue(ex.getMessage().contains("já está cadastrado"));
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar perfil com sucesso quando usuário existe")
    void getProfile_WhenUserExists_ShouldReturnSummary() {
        User user = User.builder()
                .id(1L)
                .name("Admin")
                .username("admin")
                .email("admin@nerdlab.com")
                .role("ROLE_ADMIN")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepositoryPort.findByUsername("admin")).thenReturn(Optional.of(user));

        UserSummaryResponse summary = userService.getProfile("admin");

        assertNotNull(summary);
        assertEquals("admin@nerdlab.com", summary.email());
        assertEquals("ROLE_ADMIN", summary.role());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não for encontrado")
    void getProfile_WhenUserNotFound_ShouldThrowException() {
        when(userRepositoryPort.findByUsername("desconhecido")).thenReturn(Optional.empty());
        when(userRepositoryPort.findByEmail("desconhecido")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getProfile("desconhecido")
        );
    }
}
